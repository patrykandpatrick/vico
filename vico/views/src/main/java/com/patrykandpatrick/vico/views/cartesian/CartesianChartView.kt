/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.views.cartesian

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.MutableCartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.ChartValues
import com.patrykandpatrick.vico.core.cartesian.data.MutableChartValues
import com.patrykandpatrick.vico.core.cartesian.data.RandomCartesianModelGenerator
import com.patrykandpatrick.vico.core.cartesian.data.toImmutable
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.NEW_PRODUCER_ERROR_MESSAGE
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.spToPx
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.common.BaseChartView
import com.patrykandpatrick.vico.views.common.density
import com.patrykandpatrick.vico.views.common.dpInt
import com.patrykandpatrick.vico.views.common.gesture.ChartScaleGestureListener
import com.patrykandpatrick.vico.views.common.gesture.MotionEventHandler
import com.patrykandpatrick.vico.views.common.isAttachedToWindowCompat
import com.patrykandpatrick.vico.views.common.isLtr
import com.patrykandpatrick.vico.views.common.theme.ThemeHandler
import kotlin.math.abs
import kotlin.properties.Delegates.observable
import kotlin.properties.ReadWriteProperty
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/** Displays a [CartesianChart]. */
public open class CartesianChartView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  BaseChartView<CartesianChartModel>(context, attrs, defStyleAttr) {
  private val themeHandler: ThemeHandler = ThemeHandler(context, attrs)

  private val scroller = OverScroller(context)

  private val mutableChartValues = MutableChartValues()

  override val measureContext: MutableCartesianMeasureContext =
    MutableCartesianMeasureContext(
      canvasBounds = canvasBounds,
      density = context.density,
      isLtr = context.isLtr,
      scrollEnabled = false,
      zoomEnabled = false,
      horizontalLayout = themeHandler.chart?.horizontalLayout ?: HorizontalLayout.Segmented,
      chartValues = ChartValues.Empty,
      spToPx = context::spToPx,
    )

  private val scaleGestureListener: ScaleGestureDetector.OnScaleGestureListener =
    ChartScaleGestureListener(getLayerBounds = { chart?.layerBounds }, onZoom = ::handleZoom)

  private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

  private var markerTouchPoint: Point? = null

  private var scrollDirectionResolved = false

  private var horizontalDimensions = MutableHorizontalDimensions()

  /**
   * Houses information on the [CartesianChart]’s scroll value. Allows for scroll customization and
   * programmatic scrolling.
   */
  public var scrollHandler: ScrollHandler by
    invalidatingObservable(ScrollHandler(themeHandler.scrollEnabled)) { oldValue, newValue ->
      oldValue?.clearUpdated()
      newValue.postInvalidate = ::postInvalidate
      newValue.postInvalidateOnAnimation = ::postInvalidateOnAnimation
      measureContext.scrollEnabled = newValue.scrollEnabled
      measureContext.zoomEnabled = measureContext.zoomEnabled && newValue.scrollEnabled
    }

  /** Houses information on the [CartesianChart]’s zoom factor. Allows for zoom customization. */
  public var zoomHandler: ZoomHandler by
    invalidatingObservable(
      ZoomHandler.default(themeHandler.isChartZoomEnabled, scrollHandler.scrollEnabled)
    ) { _, newValue ->
      measureContext.zoomEnabled = newValue.zoomEnabled && measureContext.scrollEnabled
    }

  private val motionEventHandler =
    MotionEventHandler(
      scroller = scroller,
      density = resources.displayMetrics.density,
      onTouchPoint = ::handleTouchEvent,
      requestInvalidate = ::invalidate,
    )

  /** The [CartesianChart] displayed by this [View]. */
  public var chart: CartesianChart? by
    observable(themeHandler.chart) { _, _, newValue ->
      if (newValue != null) measureContext.horizontalLayout = newValue.horizontalLayout
      tryInvalidate(chart = newValue, model = model, updateChartValues = true)
    }

  /** Creates and updates the [CartesianChartModel]. */
  public var modelProducer: CartesianChartModelProducer? = null
    set(value) {
      if (field === value) return
      check(field == null) { NEW_PRODUCER_ERROR_MESSAGE }
      field = value
      if (isAttachedToWindowCompat) registerForUpdates()
    }

  private fun registerForUpdates() {
    coroutineScope?.launch {
      modelProducer?.registerForUpdates(
        key = this@CartesianChartView,
        cancelAnimation = {
          handler?.post(animator::cancel)
          animationFrameJob?.cancelAndJoin()
          finalAnimationFrameJob?.cancelAndJoin()
          isAnimationRunning = false
          isAnimationFrameGenerationRunning = false
        },
        startAnimation = ::startAnimation,
        prepareForTransformation = { model, extraStore, chartValues ->
          chart?.prepareForTransformation(model, extraStore, chartValues)
        },
        transform = { extraStore, fraction -> chart?.transform(extraStore, fraction) },
        extraStore = extraStore,
        updateChartValues = { model ->
          mutableChartValues.reset()
          if (model != null) {
            chart?.updateChartValues(mutableChartValues, model)
            mutableChartValues.toImmutable()
          } else {
            ChartValues.Empty
          }
        },
      ) { model, chartValues ->
        post {
          setModel(model = model, updateChartValues = false)
          measureContext.chartValues = chartValues
          postInvalidateOnAnimation()
        }
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (modelProducer?.isRegistered(key = this) != true) registerForUpdates()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    modelProducer?.unregisterFromUpdates(key = this)
    scrollHandler.clearUpdated()
  }

  init {
    if (isInEditMode && attrs != null) {
      context.obtainStyledAttributes(attrs, R.styleable.CartesianChartView, defStyleAttr, 0).use {
        typedArray ->
        val minX =
          typedArray.getInteger(
            R.styleable.CartesianChartView_previewMinX,
            RandomCartesianModelGenerator.defaultX.first,
          )
        val maxX =
          typedArray.getInteger(
            R.styleable.CartesianChartView_previewMaxX,
            RandomCartesianModelGenerator.defaultX.last,
          )
        val minY =
          if (typedArray.hasValue(R.styleable.CartesianChartView_previewMinY)) {
            typedArray.getFloat(R.styleable.CartesianChartView_previewMinY, 0f).toDouble()
          } else {
            RandomCartesianModelGenerator.defaultY.start
          }
        val maxY =
          if (typedArray.hasValue(R.styleable.CartesianChartView_previewMaxY)) {
            typedArray.getFloat(R.styleable.CartesianChartView_previewMaxY, 0f).toDouble()
          } else {
            RandomCartesianModelGenerator.defaultY.endInclusive
          }
        setModel(
          RandomCartesianModelGenerator.getRandomModel(
            typedArray.getInt(R.styleable.CartesianChartView_previewColumnSeriesCount, 1),
            typedArray.getInt(R.styleable.CartesianChartView_previewLineSeriesCount, 1),
            minX..maxX,
            minY..maxY,
          )
        )
      }
    }
  }

  /** Sets the [CartesianChartModel]. */
  public fun setModel(model: CartesianChartModel?) {
    setModel(model = model, updateChartValues = true)
  }

  override fun shouldShowPlaceholder(): Boolean = model == null

  private fun setModel(model: CartesianChartModel?, updateChartValues: Boolean) {
    val oldModel = this.model
    this.model = model
    updatePlaceholderVisibility()
    tryInvalidate(chart, model, updateChartValues)
    if (model != null && oldModel?.id != model.id && isInEditMode.not()) {
      handler?.post { scrollHandler.autoScroll(model, oldModel) }
    }
  }

  protected fun tryInvalidate(
    chart: CartesianChart?,
    model: CartesianChartModel?,
    updateChartValues: Boolean,
  ) {
    if (chart == null || model == null) return
    if (updateChartValues) {
      mutableChartValues.reset()
      chart.updateChartValues(mutableChartValues, model)
      measureContext.chartValues = mutableChartValues.toImmutable()
    }
    if (isAttachedToWindowCompat) invalidate()
  }

  protected inline fun <T> invalidatingObservable(
    initialValue: T,
    crossinline onChange: (T?, T) -> Unit = { _, _ -> },
  ): ReadWriteProperty<Any?, T> {
    onChange(null, initialValue)
    return observable(initialValue) { _, oldValue, newValue ->
      tryInvalidate(chart = chart, model = model, updateChartValues = false)
      onChange(oldValue, newValue)
    }
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val superHandled = super.onTouchEvent(event)
    if (!isEnabled) return superHandled
    val scaleHandled =
      if (zoomHandler.zoomEnabled && event.pointerCount > 1 && scrollHandler.scrollEnabled) {
        scaleGestureDetector.onTouchEvent(event)
      } else {
        false
      }
    val touchHandled = motionEventHandler.handleMotionEvent(event, scrollHandler)

    if (scrollDirectionResolved.not() && event.historySize > 0) {
      scrollDirectionResolved = true
      parent.requestDisallowInterceptTouchEvent(
        event.movedXDistance > event.movedYDistance || event.pointerCount > 1
      )
    } else if (
      event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL
    ) {
      scrollDirectionResolved = false
    }

    return touchHandled || scaleHandled || superHandled
  }

  private fun handleZoom(focusX: Float, zoomChange: Float) {
    val chart = chart ?: return
    scrollHandler.scroll(
      zoomHandler.zoom(zoomChange, focusX, scrollHandler.value, chart.layerBounds)
    )
    handleTouchEvent(null)
    invalidate()
  }

  private fun handleTouchEvent(point: Point?) {
    markerTouchPoint = point
  }

  override fun dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(canvas)

    withChartAndModel { chart, model ->
      measureContext.reset()
      horizontalDimensions.clear()
      chart.prepare(measureContext, model, horizontalDimensions, canvasBounds)

      if (chart.layerBounds.isEmpty) return@withChartAndModel

      motionEventHandler.scrollEnabled = scrollHandler.scrollEnabled
      if (scroller.computeScrollOffset()) {
        scrollHandler.scroll(Scroll.Absolute.pixels(scroller.currX.toFloat()))
        postInvalidateOnAnimation()
      }

      zoomHandler.update(measureContext, horizontalDimensions, chart.layerBounds)
      scrollHandler.update(measureContext, chart.layerBounds, horizontalDimensions)

      val cartesianDrawContext =
        CartesianDrawContext(
          canvas = canvas,
          measureContext = measureContext,
          markerTouchPoint = markerTouchPoint,
          horizontalDimensions = horizontalDimensions,
          layerBounds = chart.layerBounds,
          scroll = scrollHandler.value,
          zoom = zoomHandler.value,
        )

      chart.draw(cartesianDrawContext, model, markerTouchPoint)
      measureContext.reset()
    }
  }

  override fun getChartDesiredHeight(widthMeasureSpec: Int, heightMeasureSpec: Int): Int =
    Defaults.CHART_HEIGHT.dpInt

  private inline fun withChartAndModel(
    block: (chart: CartesianChart, model: CartesianChartModel) -> Unit
  ) {
    val chart = chart ?: return
    val model = model ?: return
    block(chart, model)
  }
}

internal val MotionEvent.movedXDistance: Float
  get() = if (historySize > 0) abs(x - getHistoricalX(historySize - 1)) else 0f

internal val MotionEvent.movedYDistance: Float
  get() = if (historySize > 0) abs(y - getHistoricalY(historySize - 1)) else 0f
