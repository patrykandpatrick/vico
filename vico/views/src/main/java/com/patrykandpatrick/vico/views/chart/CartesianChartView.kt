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
import com.patrykandpatrick.vico.core.Defaults.CHART_HEIGHT
import com.patrykandpatrick.vico.core.Defaults.MAX_ZOOM
import com.patrykandpatrick.vico.core.Defaults.MIN_ZOOM
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.chartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.drawMarker
import com.patrykandpatrick.vico.core.chart.draw.getAutoZoom
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.MutableChartValues
import com.patrykandpatrick.vico.core.chart.values.toImmutable
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.context.MutableMeasureContext
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.extension.spToPx
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.MutableExtraStore
import com.patrykandpatrick.vico.core.scroll.ScrollHandler
import com.patrykandpatrick.vico.core.scroll.ScrollListener
import com.patrykandpatrick.vico.core.scroll.ScrollListenerHost
import com.patrykandpatrick.vico.core.util.Point
import com.patrykandpatrick.vico.core.util.RandomCartesianModelGenerator
import com.patrykandpatrick.vico.views.R
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
 * Displays a [CartesianChart].
 */
public open class CartesianChartView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr), ScrollListenerHost {
        private val contentBounds = RectF()

        private val scrollHandler = ScrollHandler()

        private val scroller = OverScroller(context)

        private val mutableChartValues = MutableChartValues()

        private val motionEventHandler =
            MotionEventHandler(
                scroller = scroller,
                scrollHandler = scrollHandler,
                density = resources.displayMetrics.density,
                onTouchPoint = ::handleTouchEvent,
                requestInvalidate = ::invalidate,
            )

        private val measureContext =
            MutableMeasureContext(
                canvasBounds = contentBounds,
                density = context.density,
                isLtr = context.isLtr,
                isHorizontalScrollEnabled = false,
                spToPx = context::spToPx,
                chartValues = ChartValues.Empty,
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

        private val themeHandler: ThemeHandler = ThemeHandler(context, attrs)

        protected var placeholder: View? = null

        /**
         * Houses scrolling-related settings.
         */
        public var chartScrollSpec: ChartScrollSpec by invalidatingObservable(ChartScrollSpec()) { newValue ->
            measureContext.isHorizontalScrollEnabled = newValue.isScrollEnabled
        }

        /**
         * Defines how the chart’s content is positioned horizontally.
         */
        public var horizontalLayout: HorizontalLayout by invalidatingObservable(
            themeHandler.horizontalLayout,
        ) { newValue ->
            measureContext.horizontalLayout = newValue
        }

        /**
         * Overrides the _x_ step (the difference between the _x_ values of neighboring major entries). If this is null,
         * the output of [CartesianChartModel.getXDeltaGcd] is used.
         */
        public var getXStep: ((CartesianChartModel) -> Float)? by invalidatingObservable(null)

        /**
         * Whether the pinch-to-zoom gesture is enabled.
         */
        public var isZoomEnabled: Boolean = true

        /**
         * Whether to display an animation when the chart is created. In this animation, the value of each chart entry
         * is animated from zero to the actual value.
         */
        public var runInitialAnimation: Boolean = true

        /**
         * The [CoroutineDispatcher] to be used for the handling of difference animations.
         */
        public var dispatcher: CoroutineDispatcher = Dispatchers.Default

        /**
         * The [CartesianChart] displayed by this [View].
         */
        public var chart: CartesianChart? by observable(null) { _, _, _ ->
            tryInvalidate(chart = chart, model = model, updateChartValues = true)
        }

        /**
         * The [CartesianChartModel].
         */
        public var model: CartesianChartModel? = null
            private set

        /**
         * Creates and updates the [CartesianChartModel].
         */
        public var modelProducer: CartesianChartModelProducer? = null
            set(value) {
                field?.unregisterFromUpdates(key = this)
                field = value
                if (ViewCompat.isAttachedToWindow(this)) registerForUpdates()
            }

        private fun registerForUpdates() {
            coroutineScope?.launch(dispatcher) {
                modelProducer?.registerForUpdates(
                    key = this@CartesianChartView,
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
                    prepareForTransformation = { model, extraStore, chartValues ->
                        chart?.prepareForTransformation(model, extraStore, chartValues)
                    },
                    transform = { extraStore, fraction -> chart?.transform(extraStore, fraction) },
                    extraStore = extraStore,
                    updateChartValues = { model ->
                        mutableChartValues.reset()
                        if (model != null) {
                            chart?.updateChartValues(mutableChartValues, model, getXStep?.invoke(model))
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
            coroutineScope = CoroutineScope(EmptyCoroutineContext)
            if (modelProducer?.isRegistered(key = this) != true) registerForUpdates()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            coroutineScope?.cancel()
            coroutineScope = null
            animator.cancel()
            isAnimationRunning = false
            modelProducer?.unregisterFromUpdates(key = this)
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
         * The color of elevation overlays, which are applied to [ShapeComponent]s that cast shadows.
         */
        public var elevationOverlayColor: Int = context.defaultColors.elevationOverlayColor.toInt()

        /**
         * Defines whether the content of the chart should be scaled up when the dimensions are such that, at a scale
         * factor of 1, an empty space would be visible near the end edge of the chart.
         */
        public var autoScaleUp: AutoScaleUp = AutoScaleUp.Full

        init {
            chart = themeHandler.chart
            chartScrollSpec = chartScrollSpec.copy(isScrollEnabled = themeHandler.isHorizontalScrollEnabled)
            isZoomEnabled = themeHandler.isChartZoomEnabled
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
                        typedArray.getFloat(
                            R.styleable.CartesianChartView_previewMinY,
                            RandomCartesianModelGenerator.defaultY.start,
                        )
                    val maxY =
                        typedArray.getFloat(
                            R.styleable.CartesianChartView_previewMaxY,
                            RandomCartesianModelGenerator.defaultY.endInclusive,
                        )
                    setModel(
                        RandomCartesianModelGenerator.getRandomModel(
                            typedArray.getInt(R.styleable.CartesianChartView_previewColumnSeriesCount, 1),
                            typedArray.getInt(R.styleable.CartesianChartView_previewLineSeriesCount, 1),
                            minX..maxX,
                            minY..maxY,
                        ),
                    )
                }
            }
        }

        /**
         * Sets the [CartesianChartModel].
         */
        public fun setModel(model: CartesianChartModel?) {
            setModel(model = model, updateChartValues = true)
        }

        private fun setModel(
            model: CartesianChartModel?,
            updateChartValues: Boolean,
        ) {
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

        protected fun tryInvalidate(
            chart: CartesianChart?,
            model: CartesianChartModel?,
            updateChartValues: Boolean,
        ) {
            if (chart == null || model == null) return
            if (updateChartValues) {
                mutableChartValues.reset()
                chart.updateChartValues(mutableChartValues, model, getXStep?.invoke(model))
                measureContext.chartValues = mutableChartValues.toImmutable()
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
            val scaleHandled =
                if (isZoomEnabled && event.pointerCount > 1 && chartScrollSpec.isScrollEnabled) {
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

        private fun handleZoom(
            focusX: Float,
            zoomChange: Float,
        ) {
            val chart = chart ?: return
            val newZoom = zoom * zoomChange
            if (newZoom !in MIN_ZOOM..MAX_ZOOM) return
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
                measureContext.reset()
                horizontalDimensions.clear()
                chart.prepare(measureContext, model, horizontalDimensions, contentBounds, marker)

                if (chart.bounds.isEmpty) return@withChartAndModel

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

                scrollHandler.maxValue =
                    measureContext.getMaxScrollDistance(
                        chartWidth = chart.bounds.width(),
                        horizontalDimensions = horizontalDimensions,
                        zoom = finalZoom,
                    )

                scrollHandler.handleInitialScroll(initialScroll = chartScrollSpec.initialScroll)

                val chartDrawContext =
                    chartDrawContext(
                        canvas = canvas,
                        elevationOverlayColor = elevationOverlayColor,
                        measureContext = measureContext,
                        markerTouchPoint = markerTouchPoint,
                        horizontalDimensions = horizontalDimensions,
                        chartBounds = chart.bounds,
                        horizontalScroll = scrollHandler.value,
                        zoom = finalZoom,
                    )

                chart.draw(chartDrawContext, model)

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
                                        transformModel(this@CartesianChartView, fraction)
                                        isAnimationFrameGenerationRunning = false
                                    }
                            }
                            fraction == 1f -> {
                                finalAnimationFrameJob =
                                    coroutineScope?.launch(dispatcher) {
                                        animationFrameJob?.cancelAndJoin()
                                        transformModel(this@CartesianChartView, fraction)
                                        isAnimationFrameGenerationRunning = false
                                    }
                            }
                        }
                    }
                }
            } else {
                finalAnimationFrameJob =
                    coroutineScope?.launch(dispatcher) {
                        transformModel(this@CartesianChartView, Animation.range.endInclusive)
                    }
            }
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            val width = widthMeasureSpec.specSize.coerceAtLeast(suggestedMinimumWidth)
            val defaultHeight = CHART_HEIGHT.dpInt + verticalPadding
            val height =
                when (heightMeasureSpec.specMode) {
                    MeasureSpec.EXACTLY -> heightMeasureSpec.specSize
                    MeasureSpec.AT_MOST -> defaultHeight.coerceAtMost(heightMeasureSpec.specSize)
                    else -> defaultHeight
                }.coerceAtLeast(suggestedMinimumHeight)
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
            )
            contentBounds
                .set(
                    left = paddingLeft,
                    top = paddingTop,
                    right = width - paddingRight,
                    bottom = height - paddingBottom,
                )
        }

        override fun addView(
            child: View,
            index: Int,
            params: ViewGroup.LayoutParams?,
        ) {
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
         * Updates the placeholder, which is shown when no [CartesianChartModel] is available.
         */
        public fun setPlaceholder(
            view: View?,
            params: LayoutParams? = null,
        ) {
            if (view === placeholder) return
            removeAllViews()
            if (view != null) addView(view, params)
            placeholder = view
            updatePlaceholderVisibility()
        }

        protected fun updatePlaceholderVisibility() {
            placeholder?.isVisible = model == null
        }

        private inline fun withChartAndModel(block: (chart: CartesianChart, model: CartesianChartModel) -> Unit) {
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
         * Invokes the provided function block, passing to it the current scroll amount and the maximum scroll amount,
         * and scrolls the chart by the number of pixels returned by the function block.
         */
        public fun scrollBy(getDelta: (value: Float, maxValue: Float) -> Float) {
            scrollHandler.handleScrollDelta(getDelta(scrollHandler.value, scrollHandler.maxValue))
        }

        /**
         * Invokes the provided function block, passing to it the current scroll amount and the maximum scroll amount,
         * and scrolls the chart by the number of pixels returned by the function block, using a [ValueAnimator].
         * Customize the animation with [setAnimatedScrollDuration] and [setAnimatedScrollInterpolator].
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
            // In this case, we can ignore this callback, as the layout direction will be determined when the
            // MeasureContext instance is created.
            measureContext?.isLtr = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
        }
    }
