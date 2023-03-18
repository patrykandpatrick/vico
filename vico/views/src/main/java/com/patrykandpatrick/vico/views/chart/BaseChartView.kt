/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.Interpolator
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.DEF_MAX_ZOOM
import com.patrykandpatrick.vico.core.DEF_MIN_ZOOM
import com.patrykandpatrick.vico.core.axis.AxisManager
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.draw.chartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.drawMarker
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.chart.edges.FadingEdges
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.MutableMeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.layout.VirtualLayout
import com.patrykandpatrick.vico.core.legend.Legend
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.model.Point
import com.patrykandpatrick.vico.core.scroll.ScrollHandler
import com.patrykandpatrick.vico.core.scroll.ScrollListener
import com.patrykandpatrick.vico.core.scroll.ScrollListenerHost
import com.patrykandpatrick.vico.views.extension.defaultColors
import com.patrykandpatrick.vico.views.extension.density
import com.patrykandpatrick.vico.views.extension.fontScale
import com.patrykandpatrick.vico.views.extension.getWidthAndHeight
import com.patrykandpatrick.vico.views.extension.isLtr
import com.patrykandpatrick.vico.views.gestures.ChartScaleGestureListener
import com.patrykandpatrick.vico.views.gestures.MotionEventHandler
import com.patrykandpatrick.vico.views.gestures.movedXDistance
import com.patrykandpatrick.vico.views.gestures.movedYDistance
import com.patrykandpatrick.vico.views.scroll.ChartScrollSpec
import com.patrykandpatrick.vico.views.scroll.copy
import com.patrykandpatrick.vico.views.theme.ThemeHandler
import kotlin.properties.Delegates.observable

/**
 * The base for [View]s that display a chart. Subclasses define a [Model] implementation they can handle.
 */
public abstract class BaseChartView<Model : ChartEntryModel> internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    chartType: ThemeHandler.ChartType,
) : View(context, attrs, defStyleAttr), ScrollListenerHost {

    private val contentBounds = RectF()

    private val scrollHandler = ScrollHandler()

    private val scroller = OverScroller(context)

    private val axisManager = AxisManager()

    private val virtualLayout = VirtualLayout(axisManager)

    private val motionEventHandler = MotionEventHandler(
        scroller = scroller,
        scrollHandler = scrollHandler,
        density = resources.displayMetrics.density,
        onTouchPoint = ::handleTouchEvent,
        requestInvalidate = ::invalidate,
    )

    private val measureContext = MutableMeasureContext(
        canvasBounds = contentBounds,
        density = context.density,
        fontScale = context.fontScale,
        isLtr = context.isLtr,
        isHorizontalScrollEnabled = false,
        chartScale = 1f,
    )

    private val scaleGestureListener: ScaleGestureDetector.OnScaleGestureListener =
        ChartScaleGestureListener(
            getChartBounds = { chart?.bounds },
            onZoom = ::handleZoom,
        )

    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    private val animator: ValueAnimator =
        ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
            duration = Animation.DIFF_DURATION.toLong()
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { progressModelOnAnimationProgress(it.animatedFraction) }
        }

    private val scrollValueAnimator: ValueAnimator =
        ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
            duration = Animation.ANIMATED_SCROLL_DURATION.toLong()
            interpolator = FastOutSlowInInterpolator()
        }

    private var markerTouchPoint: Point? = null

    private var wasMarkerVisible: Boolean = false

    private var scrollDirectionResolved = false

    private var lastMarkerEntryModels = emptyList<Marker.EntryModel>()

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
     * Houses scrolling-related settings.
     */
    public var chartScrollSpec: ChartScrollSpec<Model> by observable(ChartScrollSpec()) { _, _, newValue ->
        measureContext.isHorizontalScrollEnabled = newValue.isScrollEnabled
    }

    /**
     * Whether the chart can be scrolled horizontally.
     */
    @Deprecated(message = "`isHorizontalScrollEnabled` is deprecated. Use `chartScrollSpec` instead.")
    public var isHorizontalScrollEnabled: Boolean
        get() = chartScrollSpec.isScrollEnabled
        set(value) {
            chartScrollSpec = chartScrollSpec.copy(isScrollEnabled = value)
        }

    /**
     * Whether the pinch-to-zoom gesture is enabled.
     */
    public var isZoomEnabled: Boolean = true

    /**
     * Whether to display an animation when the chart is created. In this animation, the value of each chart entry is
     * animated from zero to the actual value.
     */
    public var runInitialAnimation: Boolean = true

    /**
     * The [Chart] displayed by this [View].
     */
    public var chart: Chart<Model>? by observable(null) { _, _, _ ->
        tryInvalidate(chart, model)
    }

    /**
     * The [Model] for this [BaseChartView]’s [Chart] instance ([chart]).
     */
    public var model: Model? = null
        private set

    /**
     * A [ChartModelProducer] can provide [Model] updates asynchronously.
     *
     * @see ChartModelProducer
     */
    public var entryProducer: ChartModelProducer<Model>? = null
        set(value) {
            field?.unregisterFromUpdates(key = this)
            field = value
            if (ViewCompat.isAttachedToWindow(this)) registerForUpdates()
        }

    private fun registerForUpdates() {
        entryProducer?.registerForUpdates(
            key = this,
            updateListener = {
                if (model != null || runInitialAnimation) {
                    handler.post(animator::start)
                } else {
                    progressModelOnAnimationProgress(progress = Animation.range.endInclusive)
                }
            },
            getOldModel = { model },
        ) { model ->
            setModel(model = model)
            postInvalidateOnAnimation()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (entryProducer?.isRegistered(key = this) != true) registerForUpdates()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        entryProducer?.unregisterFromUpdates(key = this)
    }

    /**
     * The [Marker] for this chart.
     */
    public var marker: Marker? = null

    /**
     * Allows for listening to [marker] visibility changes.
     */
    public var markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null

    /**
     * The legend for this chart.
     */
    public var legend: Legend? = null

    /**
     * The color of elevation overlays, which are applied to [ShapeComponent]s that cast shadows.
     */
    public var elevationOverlayColor: Int = context.defaultColors.elevationOverlayColor.toInt()

    /**
     * Applies a horizontal fade to the edges of the chart area for scrollable charts.
     */
    public var fadingEdges: FadingEdges? = null

    /**
     * Defines whether the content of a scrollable chart should be scaled up when the entry count and intrinsic segment
     * width are such that, at a scale factor of 1, an empty space would be visible near the end edge of the chart.
     */
    public var autoScaleUp: AutoScaleUp = AutoScaleUp.Full

    init {
        startAxis = themeHandler.startAxis
        topAxis = themeHandler.topAxis
        endAxis = themeHandler.endAxis
        bottomAxis = themeHandler.bottomAxis
        chartScrollSpec = chartScrollSpec.copy(isScrollEnabled = themeHandler.isHorizontalScrollEnabled)
        isZoomEnabled = themeHandler.isChartZoomEnabled
        fadingEdges = themeHandler.fadingEdges
    }

    /**
     * Sets the [Model] for this [BaseChartView]’s [Chart] instance ([chart]).
     */
    public fun setModel(model: Model) {
        val oldModel = this.model
        this.model = model
        tryInvalidate(chart = chart, model = model)
        if (ViewCompat.isAttachedToWindow(this) && oldModel?.id != model.id && isInEditMode.not()) {
            handler.post {
                chartScrollSpec.performAutoScroll(
                    model = model,
                    oldModel = oldModel,
                    scrollHandler = scrollHandler,
                )
            }
        }
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleHandled =
            if (isZoomEnabled && event.pointerCount > 1) scaleGestureDetector.onTouchEvent(event) else false
        val touchHandled = motionEventHandler.handleMotionEvent(event)

        if (scrollDirectionResolved.not() && event.historySize > 0) {
            scrollDirectionResolved = true
            parent.requestDisallowInterceptTouchEvent(
                event.movedXDistance > event.movedYDistance || event.pointerCount > 1,
            )
        } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            scrollDirectionResolved = false
        }

        return touchHandled || scaleHandled
    }

    private fun handleZoom(focusX: Float, zoomChange: Float) {
        val chart = chart ?: return
        val newZoom = measureContext.chartScale * zoomChange
        if (newZoom !in DEF_MIN_ZOOM..DEF_MAX_ZOOM) return
        val transformationAxisX = scrollHandler.value + focusX - chart.bounds.left
        val zoomedTransformationAxisX = transformationAxisX * zoomChange
        measureContext.chartScale = newZoom
        scrollHandler.value += zoomedTransformationAxisX - transformationAxisX
        invalidate()
    }

    private fun handleTouchEvent(point: Point?) {
        markerTouchPoint = point
    }

    override fun dispatchDraw(canvas: Canvas): Unit = withChartAndModel { chart, model ->
        val chartBounds = updateBounds(measureContext, chart, model)

        if (chartBounds.isEmpty) return@withChartAndModel

        motionEventHandler.isHorizontalScrollEnabled = chartScrollSpec.isScrollEnabled
        if (scroller.computeScrollOffset()) {
            scrollHandler.handleScroll(scroller.currX.toFloat())
            ViewCompat.postInvalidateOnAnimation(this)
        }

        val segmentProperties = chart.getSegmentProperties(measureContext, model)

        scrollHandler.maxValue = measureContext.getMaxScrollDistance(
            chartWidth = chart.bounds.width(),
            segmentProperties = segmentProperties,
        )

        scrollHandler.handleInitialScroll(initialScroll = chartScrollSpec.initialScroll)

        val chartDrawContext = chartDrawContext(
            canvas = canvas,
            elevationOverlayColor = elevationOverlayColor,
            measureContext = measureContext,
            markerTouchPoint = markerTouchPoint,
            segmentProperties = segmentProperties,
            chartBounds = chart.bounds,
            horizontalScroll = scrollHandler.value,
            autoScaleUp = autoScaleUp,
        )

        val count = if (fadingEdges != null) chartDrawContext.saveLayer() else -1

        axisManager.drawBehindChart(chartDrawContext)
        chart.drawScrollableContent(chartDrawContext, model)

        fadingEdges?.apply {
            applyFadingEdges(chartDrawContext, chart.bounds)
            chartDrawContext.restoreCanvasToCount(count)
        }

        axisManager.drawAboveChart(chartDrawContext)
        chart.drawNonScrollableContent(chartDrawContext, model)
        legend?.draw(chartDrawContext)

        marker?.also { marker ->
            chartDrawContext.drawMarker(
                marker = marker,
                markerTouchPoint = markerTouchPoint,
                chart = chart,
                markerVisibilityChangeListener = markerVisibilityChangeListener,
                wasMarkerVisible = wasMarkerVisible,
                setWasMarkerVisible = { wasMarkerVisible = it },
                lastMarkerEntryModels = lastMarkerEntryModels,
                onMarkerEntryModelsChange = { lastMarkerEntryModels = it },
            )
        }
        measureContext.clearExtras()
    }

    private fun progressModelOnAnimationProgress(progress: Float) {
        entryProducer?.progressModel(this, progress)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (width, height) = getWidthAndHeight(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(width, height)

        contentBounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom,
        )
    }

    private fun updateBounds(
        context: MeasureContext,
        chart: Chart<Model>,
        model: Model,
    ): RectF {
        measureContext.clearExtras()
        return virtualLayout.setBounds(
            context = measureContext,
            contentBounds = contentBounds,
            chart = chart,
            legend = legend,
            segmentProperties = chart.getSegmentProperties(context = context, model = model),
            marker,
        )
    }

    private inline fun withChartAndModel(block: (chart: Chart<Model>, model: Model) -> Unit) {
        val chart = chart ?: return
        val model = model ?: return
        block(chart, model)
    }

    /**
     * Sets the duration (in milliseconds) of difference animations.
     */
    public fun setDiffAnimationDuration(durationMillis: Long) {
        animator.duration = durationMillis
    }

    /**
     * Sets the [Interpolator] for difference animations.
     */
    public fun setDiffAnimationInterpolator(interpolator: Interpolator) {
        animator.interpolator = interpolator
    }

    /**
     * Sets the duration (in milliseconds) of animated scrolls ([animateScrollBy]).
     */
    public fun setAnimatedScrollDuration(durationMillis: Long) {
        scrollValueAnimator.duration = durationMillis
    }

    /**
     * Sets the [Interpolator] for animated scrolls ([animateScrollBy]).
     */
    public fun setAnimatedScrollInterpolator(interpolator: Interpolator) {
        scrollValueAnimator.interpolator = interpolator
    }

    public override fun registerScrollListener(scrollListener: ScrollListener) {
        scrollHandler.registerScrollListener(scrollListener)
    }

    public override fun removeScrollListener(scrollListener: ScrollListener) {
        scrollHandler.removeScrollListener(scrollListener)
    }

    /**
     * Invokes the provided function block, passing to it the current scroll amount and the maximum scroll amount, and
     * scrolls the chart by the number of pixels returned by the function block.
     */
    public fun scrollBy(getDelta: (value: Float, maxValue: Float) -> Float) {
        scrollHandler.handleScrollDelta(getDelta(scrollHandler.value, scrollHandler.maxValue))
    }

    /**
     * Invokes the provided function block, passing to it the current scroll amount and the maximum scroll amount, and
     * scrolls the chart by the number of pixels returned by the function block, using a [ValueAnimator]. Customize the
     * animation with [setAnimatedScrollDuration] and [setAnimatedScrollInterpolator].
     */
    public fun animateScrollBy(getDelta: (value: Float, maxValue: Float) -> Float) {
        val initialValue = scrollHandler.value
        val delta = getDelta(initialValue, scrollHandler.maxValue)
        with(scrollValueAnimator) {
            cancel()
            removeAllUpdateListeners()
            addUpdateListener { scrollHandler.handleScroll(initialValue + it.animatedFraction * delta) }
            start()
        }
    }

    @Suppress("UNNECESSARY_SAFE_CALL")
    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        // This function may be invoked inside of the View’s constructor, before the measureContext is initialized.
        // In this case, we can ignore this callback, as the layout direction will be determined when the MeasureContext
        // instance is created.
        measureContext?.isLtr = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
    }
}
