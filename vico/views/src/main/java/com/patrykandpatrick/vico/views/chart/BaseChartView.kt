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

package com.patrykandpatrick.vico.views.chart

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.DEF_MAX_ZOOM
import com.patrykandpatrick.vico.core.DEF_MIN_ZOOM
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisManager
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.chartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.drawMarker
import com.patrykandpatrick.vico.core.chart.draw.getAutoZoom
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.chart.edges.FadingEdges
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.chart.values.toChartValuesProvider
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.context.MutableMeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.diff.MutableExtraStore
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.spToPx
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
import com.patrykandpatrick.vico.views.extension.dpInt
import com.patrykandpatrick.vico.views.extension.isLtr
import com.patrykandpatrick.vico.views.extension.specMode
import com.patrykandpatrick.vico.views.extension.specSize
import com.patrykandpatrick.vico.views.extension.start
import com.patrykandpatrick.vico.views.extension.verticalPadding
import com.patrykandpatrick.vico.views.gestures.ChartScaleGestureListener
import com.patrykandpatrick.vico.views.gestures.MotionEventHandler
import com.patrykandpatrick.vico.views.gestures.movedXDistance
import com.patrykandpatrick.vico.views.gestures.movedYDistance
import com.patrykandpatrick.vico.views.scroll.ChartScrollSpec
import com.patrykandpatrick.vico.views.scroll.copy
import com.patrykandpatrick.vico.views.theme.ThemeHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.Delegates.observable
import kotlin.properties.ReadWriteProperty

/**
 * The base for [View]s that display a chart. Subclasses define a [Model] implementation they can handle.
 */
public abstract class BaseChartView<Model : ChartEntryModel> internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    chartType: ThemeHandler.ChartType,
) : FrameLayout(context, attrs, defStyleAttr), ScrollListenerHost {

    private val contentBounds = RectF()

    private val scrollHandler = ScrollHandler()

    private val scroller = OverScroller(context)

    private val axisManager = AxisManager()

    private val virtualLayout = VirtualLayout(axisManager)

    private val chartValuesManager = ChartValuesManager()

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
        isLtr = context.isLtr,
        isHorizontalScrollEnabled = false,
        spToPx = context::spToPx,
        chartValuesProvider = ChartValuesProvider.Empty,
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
        }

    private val scrollValueAnimator: ValueAnimator =
        ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
            duration = Animation.ANIMATED_SCROLL_DURATION.toLong()
            interpolator = FastOutSlowInInterpolator()
        }

    private val extraStore = MutableExtraStore()

    private var coroutineScope: CoroutineScope? = null

    private var animationFrameJob: Job? = null

    private var finalAnimationFrameJob: Job? = null

    private var isAnimationRunning = false

    private var isAnimationFrameGenerationRunning: Boolean = false

    private var markerTouchPoint: Point? = null

    private var wasMarkerVisible: Boolean = false

    private var scrollDirectionResolved = false

    private var lastMarkerEntryModels = emptyList<Marker.EntryModel>()

    private var zoom = 0f

    private var wasZoomOverridden = false

    private var horizontalDimensions = MutableHorizontalDimensions()

    internal val themeHandler: ThemeHandler = ThemeHandler(context, attrs, chartType)

    protected var placeholder: View? = null

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
    public var chartScrollSpec: ChartScrollSpec<Model> by invalidatingObservable(ChartScrollSpec()) { newValue ->
        measureContext.isHorizontalScrollEnabled = newValue.isScrollEnabled
    }

    /**
     * Defines how the chart’s content is positioned horizontally.
     */
    public var horizontalLayout: HorizontalLayout by invalidatingObservable(themeHandler.horizontalLayout) { newValue ->
        measureContext.horizontalLayout = newValue
    }

    /**
     * Overrides the _x_ step (the difference between the _x_ values of neighboring major entries). If this is null, the
     * default _x_ step ([ChartEntryModel.xGcd]) is used.
     */
    public var getXStep: ((Model) -> Float)? by invalidatingObservable(null)

    /**
     * Whether the chart can be scrolled horizontally.
     */
    @Deprecated(
        message = "`isHorizontalScrollEnabled` is deprecated. Use `chartScrollSpec` instead.",
        level = DeprecationLevel.ERROR,
    )
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
     * The [CoroutineDispatcher] to be used for the handling of difference animations.
     */
    public var dispatcher: CoroutineDispatcher = Dispatchers.Default

    /**
     * The [Chart] displayed by this [View].
     */
    public var chart: Chart<Model>? by observable(null) { _, _, _ ->
        tryInvalidate(chart = chart, model = model, updateChartValues = true)
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
        coroutineScope?.launch(dispatcher) {
            entryProducer?.registerForUpdates(
                key = this@BaseChartView,
                cancelAnimation = {
                    handler?.post(animator::cancel)
                    runBlocking {
                        animationFrameJob?.cancelAndJoin()
                        finalAnimationFrameJob?.cancelAndJoin()
                    }
                    isAnimationRunning = false
                    isAnimationFrameGenerationRunning = false
                },
                startAnimation = ::startAnimation,
                getOldModel = { model },
                modelTransformerProvider = chart?.modelTransformerProvider,
                extraStore = extraStore,
                updateChartValues = { model ->
                    chartValuesManager.resetChartValues()
                    if (model != null) {
                        chart?.updateChartValues(chartValuesManager, model, getXStep?.invoke(model))
                        chartValuesManager.toChartValuesProvider()
                    } else {
                        ChartValuesProvider.Empty
                    }
                },
            ) { model, chartValuesProvider ->
                post {
                    setModel(model = model, updateChartValues = false)
                    measureContext.chartValuesProvider = chartValuesProvider
                    postInvalidateOnAnimation()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        coroutineScope = CoroutineScope(EmptyCoroutineContext)
        if (entryProducer?.isRegistered(key = this) != true) registerForUpdates()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope?.cancel()
        coroutineScope = null
        animator.cancel()
        isAnimationRunning = false
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
    public var fadingEdges: FadingEdges? by invalidatingObservable(themeHandler.fadingEdges)

    /**
     * Defines whether the content of the chart should be scaled up when the dimensions are such that, at a scale factor
     * of 1, an empty space would be visible near the end edge of the chart.
     */
    public var autoScaleUp: AutoScaleUp = AutoScaleUp.Full

    init {
        startAxis = themeHandler.startAxis
        topAxis = themeHandler.topAxis
        endAxis = themeHandler.endAxis
        bottomAxis = themeHandler.bottomAxis
        chartScrollSpec = chartScrollSpec.copy(isScrollEnabled = themeHandler.isHorizontalScrollEnabled)
        isZoomEnabled = themeHandler.isChartZoomEnabled
    }

    /**
     * Sets the [Model] for this [BaseChartView]’s [Chart] instance ([chart]).
     */
    public fun setModel(model: Model?) {
        setModel(model = model, updateChartValues = true)
    }

    private fun setModel(model: Model?, updateChartValues: Boolean) {
        val oldModel = this.model
        this.model = model
        updatePlaceholderVisibility()
        tryInvalidate(chart, model, updateChartValues)
        if (model != null && oldModel?.id != model.id && isInEditMode.not()) {
            handler?.post {
                chartScrollSpec.performAutoScroll(
                    model = model,
                    oldModel = oldModel,
                    scrollHandler = scrollHandler,
                )
            }
        }
    }

    protected fun tryInvalidate(chart: Chart<Model>?, model: Model?, updateChartValues: Boolean) {
        if (chart == null || model == null) return
        if (updateChartValues) {
            chartValuesManager.resetChartValues()
            chart.updateChartValues(chartValuesManager, model, getXStep?.invoke(model))
            measureContext.chartValuesProvider = chartValuesManager.toChartValuesProvider()
        }
        if (ViewCompat.isAttachedToWindow(this)) invalidate()
    }

    protected inline fun <T> invalidatingObservable(
        initialValue: T,
        crossinline onChange: (T) -> Unit = {},
    ): ReadWriteProperty<Any?, T> {
        onChange(initialValue)
        return observable(initialValue) { _, _, newValue ->
            tryInvalidate(chart = chart, model = model, updateChartValues = false)
            onChange(newValue)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleHandled = if (isZoomEnabled && event.pointerCount > 1 && chartScrollSpec.isScrollEnabled) {
            scaleGestureDetector.onTouchEvent(event)
        } else {
            false
        }
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
        val newZoom = zoom * zoomChange
        if (newZoom !in DEF_MIN_ZOOM..DEF_MAX_ZOOM) return
        val transformationAxisX = scrollHandler.value + focusX - chart.bounds.left
        val zoomedTransformationAxisX = transformationAxisX * zoomChange
        zoom = newZoom
        scrollHandler.handleScrollDelta(transformationAxisX - zoomedTransformationAxisX)
        handleTouchEvent(null)
        wasZoomOverridden = true
        invalidate()
    }

    private fun handleTouchEvent(point: Point?) {
        markerTouchPoint = point
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        withChartAndModel { chart, model ->
            measureContext.clearExtras()
            horizontalDimensions.clear()
            chart.updateHorizontalDimensions(measureContext, horizontalDimensions, model)

            startAxis?.updateHorizontalDimensions(measureContext, horizontalDimensions)
            topAxis?.updateHorizontalDimensions(measureContext, horizontalDimensions)
            endAxis?.updateHorizontalDimensions(measureContext, horizontalDimensions)
            bottomAxis?.updateHorizontalDimensions(measureContext, horizontalDimensions)

            if (
                virtualLayout
                    .setBounds(
                        context = measureContext,
                        contentBounds = contentBounds,
                        chart = chart,
                        legend = legend,
                        horizontalDimensions = horizontalDimensions,
                        marker,
                    )
                    .isEmpty
            ) {
                return@withChartAndModel
            }

            motionEventHandler.isHorizontalScrollEnabled = chartScrollSpec.isScrollEnabled
            if (scroller.computeScrollOffset()) {
                scrollHandler.handleScroll(scroller.currX.toFloat())
                ViewCompat.postInvalidateOnAnimation(this)
            }

            var finalZoom = zoom

            if (!wasZoomOverridden || !chartScrollSpec.isScrollEnabled) {
                finalZoom = measureContext.getAutoZoom(horizontalDimensions, chart.bounds, autoScaleUp)
                if (chartScrollSpec.isScrollEnabled) zoom = finalZoom
            }

            scrollHandler.maxValue = measureContext.getMaxScrollDistance(
                chartWidth = chart.bounds.width(),
                horizontalDimensions = horizontalDimensions,
                zoom = finalZoom,
            )

            scrollHandler.handleInitialScroll(initialScroll = chartScrollSpec.initialScroll)

            val chartDrawContext = chartDrawContext(
                canvas = canvas,
                elevationOverlayColor = elevationOverlayColor,
                measureContext = measureContext,
                markerTouchPoint = markerTouchPoint,
                horizontalDimensions = horizontalDimensions,
                chartBounds = chart.bounds,
                horizontalScroll = scrollHandler.value,
                zoom = finalZoom,
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
            measureContext.reset()
        }
    }

    private fun startAnimation(transformModel: suspend (key: Any, fraction: Float) -> Unit) {
        if (model != null || runInitialAnimation) {
            handler?.post {
                isAnimationRunning = true
                animator.start { fraction ->
                    when {
                        !isAnimationRunning -> return@start
                        !isAnimationFrameGenerationRunning -> {
                            isAnimationFrameGenerationRunning = true
                            animationFrameJob =
                                coroutineScope?.launch(dispatcher) {
                                    transformModel(this@BaseChartView, fraction)
                                    isAnimationFrameGenerationRunning = false
                                }
                        }

                        fraction == 1f -> {
                            finalAnimationFrameJob =
                                coroutineScope?.launch(dispatcher) {
                                    animationFrameJob?.cancelAndJoin()
                                    transformModel(this@BaseChartView, fraction)
                                    isAnimationFrameGenerationRunning = false
                                }
                        }
                    }
                }
            }
        } else {
            finalAnimationFrameJob =
                coroutineScope?.launch(dispatcher) {
                    transformModel(this@BaseChartView, Animation.range.endInclusive)
                }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = widthMeasureSpec.specSize.coerceAtLeast(suggestedMinimumWidth)
        val defaultHeight = DefaultDimens.CHART_HEIGHT.dpInt + verticalPadding
        val height = when (heightMeasureSpec.specMode) {
            MeasureSpec.EXACTLY -> heightMeasureSpec.specSize
            MeasureSpec.AT_MOST -> defaultHeight.coerceAtMost(heightMeasureSpec.specSize)
            else -> defaultHeight
        }.coerceAtLeast(suggestedMinimumHeight)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
        contentBounds
            .set(left = paddingLeft, top = paddingTop, right = width - paddingRight, bottom = height - paddingBottom)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        check(childCount == 0) { "Only one placeholder can be added." }
        super.addView(child, index, params)
        placeholder = child
        updatePlaceholderVisibility()
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        placeholder = null
    }

    /**
     * Updates the placeholder, which is shown when no [ChartEntryModel] is available.
     */
    public fun setPlaceholder(view: View?, params: LayoutParams? = null) {
        if (view === placeholder) return
        removeAllViews()
        if (view != null) addView(view, params)
        placeholder = view
        updatePlaceholderVisibility()
    }

    protected fun updatePlaceholderVisibility() {
        placeholder?.isVisible = model == null
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
