/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.view.chart

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.Interpolator
import android.widget.OverScroller
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlin.properties.Delegates.observable
import com.patrykandpatryk.vico.core.Animation
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.DEF_MAX_ZOOM
import com.patrykandpatryk.vico.core.DEF_MIN_ZOOM
import com.patrykandpatryk.vico.core.axis.AxisManager
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.AxisRenderer
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.draw.chartDrawContext
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartModelProducer
import com.patrykandpatryk.vico.core.extension.getClosestMarkerEntryModel
import com.patrykandpatryk.vico.core.extension.ifNotNull
import com.patrykandpatryk.vico.core.extension.set
import com.patrykandpatryk.vico.core.layout.VirtualLayout
import com.patrykandpatryk.vico.core.marker.Marker
import com.patrykandpatryk.vico.core.model.Point
import com.patrykandpatryk.vico.core.scroll.ScrollHandler
import com.patrykandpatryk.vico.view.extension.defaultColors
import com.patrykandpatryk.vico.view.extension.density
import com.patrykandpatryk.vico.view.extension.dpInt
import com.patrykandpatryk.vico.view.extension.fontScale
import com.patrykandpatryk.vico.view.extension.isLtr
import com.patrykandpatryk.vico.view.extension.measureDimension
import com.patrykandpatryk.vico.view.extension.specSize
import com.patrykandpatryk.vico.view.extension.verticalPadding
import com.patrykandpatryk.vico.view.gestures.ChartScaleGestureListener
import com.patrykandpatryk.vico.view.gestures.MotionEventHandler
import com.patrykandpatryk.vico.core.context.MutableMeasureContext
import com.patrykandpatryk.vico.view.theme.ThemeHandler

/**
 * The base for [View]s that display a chart. Subclasses define a [Model] implementation they can handle.
 */
public abstract class BaseChartView<Model : ChartEntryModel> internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    chartType: ThemeHandler.ChartType,
) : View(context, attrs, defStyleAttr) {

    private val contentBounds = RectF()
    private val scrollHandler = ScrollHandler()

    private val scroller = OverScroller(context)
    private val virtualLayout = VirtualLayout()
    private val motionEventHandler = MotionEventHandler(
        scroller = scroller,
        scrollHandler = scrollHandler,
        density = resources.displayMetrics.density,
        onTouchPoint = ::handleTouchEvent,
        requestInvalidate = ::invalidate
    )

    private val axisManager = AxisManager()
    private val measureContext = MutableMeasureContext(
        canvasBounds = contentBounds,
        density = context.density,
        fontScale = context.fontScale,
        isLtr = context.isLtr,
        isHorizontalScrollEnabled = false,
        horizontalScroll = scrollHandler.currentScroll,
        chartScale = 1f,
    )

    private val scaleGestureListener: ScaleGestureDetector.OnScaleGestureListener =
        ChartScaleGestureListener(
            getChartBounds = { chart?.bounds },
            onZoom = ::handleZoom,
        )
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    private val animator: ValueAnimator = ValueAnimator.ofFloat(
        Animation.range.start, Animation.range.endInclusive,
    ).apply {
        duration = Animation.DIFF_DURATION.toLong()
        interpolator = FastOutSlowInInterpolator()
        doOnStart { invalidate() }
        doOnEnd { progressModelOnAnimationProgress(Animation.range.endInclusive) }
    }

    private val updateListener: () -> Model? = {
        animator.start()
        model
    }

    private var markerTouchPoint: Point? = null

    internal val themeHandler: ThemeHandler = ThemeHandler(context, attrs, chartType)

    /**
     * The [AxisRenderer] for the start axis.
     */
    public var startAxis: AxisRenderer<AxisPosition.Vertical.Start>? by axisManager::startAxis

    /**
     * The [AxisRenderer] for the top axis.
     */
    public var topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? by axisManager::topAxis

    /**
     * The [AxisRenderer] for the end axis.
     */
    public var endAxis: AxisRenderer<AxisPosition.Vertical.End>? by axisManager::endAxis

    /**
     * The [AxisRenderer] for the bottom axis.
     */
    public var bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? by axisManager::bottomAxis

    /**
     * Whether the chart can be scrolled horizontally.
     */
    public var isHorizontalScrollEnabled: Boolean = false
        set(value) {
            field = value
            measureContext.isHorizontalScrollEnabled = value
        }

    /**
     * Whether the pinch-to-zoom gesture is enabled.
     */
    public var isZoomEnabled: Boolean = true

    /**
     * The chart displayed by this [View].
     */
    public var chart: Chart<Model>? by observable(null) { _, _, _ ->
        tryInvalidate(chart, model)
    }

    /**
     * The [Model] used in the [chart] to render the data.
     */
    public var model: Model? = null
        private set

    /**
     * A [ChartModelProducer] can provide the [Model] updates asynchronously.
     *
     * @see ChartModelProducer
     */
    public var entryProducer: ChartModelProducer<Model>? = null
        set(value) {
            field?.unregisterFromUpdates(key = this)
            field = value
            value?.registerForUpdates(key = this, updateListener = updateListener) { model ->
                setModel(model)
                postInvalidateOnAnimation()
            }
        }

    /**
     * The indication of certain entry appearing on physical touch of the [Chart].
     */
    public var marker: Marker? = null

    /**
     * The color of elevation overlays, which are applied to components that cast shadows in
     * [com.patrykandpatryk.vico.core.component.shape.ShapeComponent].
     */
    public var elevationOverlayColor: Int = context.defaultColors.elevationOverlayColor.toInt()

    init {
        startAxis = themeHandler.startAxis
        topAxis = themeHandler.topAxis
        endAxis = themeHandler.endAxis
        bottomAxis = themeHandler.bottomAxis
        isHorizontalScrollEnabled = themeHandler.isHorizontalScrollEnabled
        isZoomEnabled = themeHandler.isChartZoomEnabled
    }

    /**
     * Sets a [Model] used to render data of the chart.
     */
    public fun setModel(model: Model) {
        this.model = model
        tryInvalidate(chart, model)
    }

    private fun tryInvalidate(chart: Chart<Model>?, model: Model?) {
        if (chart != null && model != null) {
            measureContext.chartValuesManager.resetChartValues()
            chart.updateChartValues(measureContext.chartValuesManager, model)
            if (ViewCompat.isAttachedToWindow(this)) {
                invalidate()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleHandled =
            if (isZoomEnabled && event.pointerCount > 1) scaleGestureDetector.onTouchEvent(event) else false
        val touchHandled = motionEventHandler.handleMotionEvent(event)
        return (touchHandled || scaleHandled).also(parent::requestDisallowInterceptTouchEvent)
    }

    private fun handleZoom(focusX: Float, zoomChange: Float) {
        val chart = chart ?: return
        val newZoom = measureContext.chartScale * zoomChange
        if (newZoom !in DEF_MIN_ZOOM..DEF_MAX_ZOOM) return
        val transformationAxisX = scrollHandler.currentScroll + focusX - chart.bounds.left
        val zoomedTransformationAxisX = transformationAxisX * zoomChange
        measureContext.chartScale = newZoom
        scrollHandler.currentScroll += zoomedTransformationAxisX - transformationAxisX
        invalidate()
    }

    private fun handleTouchEvent(point: Point?) {
        markerTouchPoint = point
    }

    override fun dispatchDraw(canvas: Canvas): Unit = withChartAndModel { chart, model ->
        val drawContext = chartDrawContext(
            canvas = canvas,
            elevationOverlayColor = elevationOverlayColor,
            measureContext = measureContext,
            markerTouchPoint = markerTouchPoint,
            segmentProperties = chart.getSegmentProperties(measureContext, model),
            chartBounds = chart.bounds,
        )
        updateBounds(context = drawContext)
        motionEventHandler.isHorizontalScrollEnabled = isHorizontalScrollEnabled
        if (scroller.computeScrollOffset()) {
            scrollHandler.handleScroll(scroller.currX.toFloat())
            ViewCompat.postInvalidateOnAnimation(this)
        }
        measureContext.horizontalScroll = scrollHandler.currentScroll
        axisManager.drawBehindChart(drawContext)
        chart.draw(drawContext, model)
        axisManager.drawAboveChart(drawContext)

        ifNotNull(
            t1 = marker,
            t2 = markerTouchPoint?.let(chart.entryLocationMap::getClosestMarkerEntryModel),
        ) { marker, markerModel ->
            marker.draw(
                context = drawContext,
                bounds = chart.bounds,
                markedEntries = markerModel,
            )
        }

        scrollHandler.maxScrollDistance = drawContext.maxScrollDistance

        if (animator.isRunning) {
            progressModelOnAnimationProgress(animator.animatedValue as Float)
        }
    }

    private fun progressModelOnAnimationProgress(progress: Float) {
        entryProducer?.progressModel(this, progress)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> DefaultDimens.CHART_HEIGHT.dpInt + verticalPadding
            MeasureSpec.AT_MOST -> minOf(
                DefaultDimens.CHART_HEIGHT.dpInt + verticalPadding,
                heightMeasureSpec.specSize
            )
            else -> measureDimension(heightMeasureSpec.specSize, heightMeasureSpec)
        }
        setMeasuredDimension(width, height)

        contentBounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom
        )
    }

    private fun updateBounds(context: ChartDrawContext) = withChartAndModel { chart, _ ->
        measureContext.clearExtras()
        virtualLayout.setBounds(
            context = context,
            contentBounds = contentBounds,
            chart = chart,
            axisManager = axisManager,
            marker
        )
    }

    private inline fun withChartAndModel(block: (chart: Chart<Model>, model: Model) -> Unit) {
        val chart = chart ?: return
        val model = model ?: return
        block(chart, model)
    }

    /**
     * Sets a duration in milliseconds of the animation run on each [model] update.
     */
    public fun setDiffAnimationDuration(durationMillis: Long) {
        animator.duration = durationMillis
    }

    /**
     * Sets an interpolator used in the animation run on each [model] update.
     */
    public fun setDiffAnimationInterpolator(interpolator: Interpolator) {
        animator.interpolator = interpolator
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        measureContext.isLtr = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
    }
}
