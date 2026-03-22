/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.pie

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.common.Animation
import com.patrykandpatrick.vico.views.common.ChartView
import com.patrykandpatrick.vico.views.common.Defaults
import com.patrykandpatrick.vico.views.common.NEW_PIE_PRODUCER_ERROR_MESSAGE
import com.patrykandpatrick.vico.views.common.dpInt
import com.patrykandpatrick.vico.views.common.theme.use
import com.patrykandpatrick.vico.views.pie.data.PieChartModel
import com.patrykandpatrick.vico.views.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.views.pie.data.RandomPieModelGenerator
import kotlin.properties.Delegates.observable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/** Displays a [PieChart]. */
public open class PieChartView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  ChartView<PieChartModel>(context, attrs, defStyleAttr) {
  private val pieChartStyleHandler: PieChartStyleHandler =
    PieChartStyleHandler(context = context, attrs = attrs, defStyleAttr = defStyleAttr)

  /** The [PieChart] displayed by this [View]. */
  public var chart: PieChart by
    observable(
      PieChart(
        sliceProvider = PieChart.SliceProvider.series(pieChartStyleHandler.slices),
        spacingDp = pieChartStyleHandler.sliceSpacing,
        outerSize = pieChartStyleHandler.outerSize,
        innerSize = pieChartStyleHandler.innerSize,
        startAngle = pieChartStyleHandler.startAngle,
      )
    ) { _, _, _ ->
      if (isAttachedToWindow) invalidate()
    }

  private var _model: PieChartModel? = null

  override var model: PieChartModel?
    get() = _model
    set(value) {
      updateModel(value)
    }

  /** Creates and updates [PieChartModel] instances. */
  public var modelProducer: PieChartModelProducer? = null
    set(value) {
      if (field === value) return
      check(field == null) { NEW_PIE_PRODUCER_ERROR_MESSAGE }
      field = value
      if (isAttachedToWindow) registerForUpdates()
    }

  init {
    animator.duration = Animation.PIE_DIFF_DURATION.toLong()
    if (isInEditMode) {
      context.obtainStyledAttributes(attrs, R.styleable.PieChartView, defStyleAttr, 0).use {
        typedArray ->
        model =
          RandomPieModelGenerator.getRandomModel(
            sliceRange =
              typedArray.getInt(R.styleable.PieChartView_previewMinSliceCount, 2)..typedArray
                  .getInt(R.styleable.PieChartView_previewMaxSliceCount, 8),
            valueRange =
              typedArray.getInt(R.styleable.PieChartView_previewMinValue, 1)..typedArray.getInt(
                  R.styleable.PieChartView_previewMaxValue,
                  8,
                ),
          )
      }
    }
  }

  private fun updateModel(model: PieChartModel?) {
    _model = model
    updatePlaceholderVisibility()
    if (isAttachedToWindow) invalidate()
  }

  override fun shouldShowPlaceholder(): Boolean = model == null

  private fun registerForUpdates() {
    coroutineScope?.launch {
      modelProducer?.registerForUpdates(
        key = this@PieChartView,
        cancelAnimation = {
          handler?.post(animator::cancel)
          animationFrameJob?.cancelAndJoin()
          finalAnimationFrameJob?.cancelAndJoin()
          isAnimationRunning = false
          isAnimationFrameGenerationRunning = false
        },
        startAnimation = ::startAnimation,
        prepareForTransformation = { model, extraStore ->
          chart.prepareForTransformation(model, extraStore)
        },
        transform = { extraStore, fraction -> chart.transform(extraStore, fraction) },
        hostExtraStore = extraStore,
      ) { model ->
        post {
          updateModel(model)
          postInvalidateOnAnimation()
        }
      }
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (modelProducer?.isRegistered(this) != true) registerForUpdates()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    modelProducer?.unregisterFromUpdates(this)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    chart.setBounds(offset.x, offset.y, offset.x + canvasSize.width, offset.y + canvasSize.height)
  }

  override fun dispatchDraw(canvas: Canvas) {
    super.dispatchDraw(canvas)
    val model = model ?: return
    val drawingContext =
      PieChartDrawingContext(pieChartMeasuringContext(model), canvas, chart.bounds)
    chart.draw(drawingContext, model)
  }

  private fun pieChartMeasuringContext(model: PieChartModel): PieChartMeasuringContext =
    object :
      PieChartMeasuringContext,
      com.patrykandpatrick.vico.views.common.MeasuringContext by measuringContext {
      override val model: PieChartModel = model
    }

  override fun getChartDesiredHeight(widthMeasureSpec: Int, heightMeasureSpec: Int): Int =
    Defaults.CHART_HEIGHT.dpInt
}
