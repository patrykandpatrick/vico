/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.view.chart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import kotlin.properties.Delegates.observable
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.MAX_ZOOM
import pl.patrykgoworowski.vico.core.MIN_ZOOM
import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.AxisRenderer
import pl.patrykgoworowski.vico.core.axis.model.MutableChartModel
import pl.patrykgoworowski.vico.core.chart.Chart
import pl.patrykgoworowski.vico.core.chart.draw.chartDrawContext
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.extension.getClosestMarkerEntryModel
import pl.patrykgoworowski.vico.core.extension.ifNotNull
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.layout.VirtualLayout
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.model.Point
import pl.patrykgoworowski.vico.core.scroll.ScrollHandler
import pl.patrykgoworowski.vico.view.extension.colors
import pl.patrykgoworowski.vico.view.extension.density
import pl.patrykgoworowski.vico.view.extension.dpInt
import pl.patrykgoworowski.vico.view.extension.fontScale
import pl.patrykgoworowski.vico.view.extension.isLtr
import pl.patrykgoworowski.vico.view.extension.measureDimension
import pl.patrykgoworowski.vico.view.extension.specSize
import pl.patrykgoworowski.vico.view.extension.verticalPadding
import pl.patrykgoworowski.vico.view.gestures.ChartScaleGestureListener
import pl.patrykgoworowski.vico.view.gestures.MotionEventHandler
import pl.patrykgoworowski.vico.view.layout.MutableMeasureContext
import pl.patrykgoworowski.vico.view.theme.ThemeHandler

public abstract class BaseChartView<Model : ChartEntryModel> internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    chartType: ThemeHandler.ChartType,
) : View(context, attrs, defStyleAttr) {

    public constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : this(context, attrs, defStyleAttr, ThemeHandler.ChartType.Unknown)

    private val contentBounds = RectF()
    private val chartModel = MutableChartModel()
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
        chartModel = chartModel,
    )

    private val scaleGestureListener: ScaleGestureDetector.OnScaleGestureListener =
        ChartScaleGestureListener(
            getChartBounds = { chart?.bounds },
            onZoom = ::handleZoom,
        )
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    private var markerTouchPoint: Point? = null

    internal val themeHandler: ThemeHandler = ThemeHandler(context, attrs, chartType)

    public var startAxis: AxisRenderer<AxisPosition.Vertical.Start>? by axisManager::startAxis
    public var topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? by axisManager::topAxis
    public var endAxis: AxisRenderer<AxisPosition.Vertical.End>? by axisManager::endAxis
    public var bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? by axisManager::bottomAxis

    public var isHorizontalScrollEnabled: Boolean = false
        set(value) {
            field = value
            measureContext.isHorizontalScrollEnabled = value
        }

    public var isZoomEnabled: Boolean = true

    public var chart: Chart<Model>? by observable(null) { _, _, _ ->
        tryInvalidate(chart, model)
    }

    public var model: Model? = null
        private set

    /**
     * The indication of certain entry appearing on physical touch of the [Chart].
     */
    public var marker: Marker? = null

    init {
        startAxis = themeHandler.startAxis
        topAxis = themeHandler.topAxis
        endAxis = themeHandler.endAxis
        bottomAxis = themeHandler.bottomAxis
        isHorizontalScrollEnabled = themeHandler.isHorizontalScrollEnabled
        isZoomEnabled = themeHandler.isChartZoomEnabled
    }

    public fun setModel(model: Model) {
        this.model = model
        tryInvalidate(chart, model)
    }

    private fun tryInvalidate(chart: Chart<Model>?, model: Model?) {
        if (chart != null && model != null) {
            chart.setToAxisModel(chartModel, model)
            if (ViewCompat.isAttachedToWindow(this)) {
                invalidate()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleHandled =
            if (isZoomEnabled && event.pointerCount > 1) scaleGestureDetector.onTouchEvent(event)
            else false
        val touchHandled = motionEventHandler.handleTouchPoint(event)
        return if (touchHandled || scaleHandled) {
            parent.requestDisallowInterceptTouchEvent(event.action == MotionEvent.ACTION_MOVE)
            true
        } else {
            parent.requestDisallowInterceptTouchEvent(false)
            super.onTouchEvent(event)
        }
    }

    private fun handleZoom(focusX: Float, zoomChange: Float) {
        val chart = chart ?: return
        val newZoom = measureContext.chartScale * zoomChange
        if (newZoom !in MIN_ZOOM..MAX_ZOOM) return
        val transformationAxisX = scrollHandler.currentScroll + focusX - chart.bounds.left
        val zoomedTransformationAxisX = transformationAxisX * zoomChange
        measureContext.chartScale = newZoom
        scrollHandler.currentScroll += zoomedTransformationAxisX - transformationAxisX
        invalidate()
    }

    private fun handleTouchEvent(point: Point?) {
        markerTouchPoint = point
    }

    override fun onDraw(canvas: Canvas): Unit = withChartAndModel { chart, model ->
        updateBounds()
        motionEventHandler.isHorizontalScrollEnabled = isHorizontalScrollEnabled
        if (scroller.computeScrollOffset()) {
            scrollHandler.handleScroll(scroller.currX.toFloat())
            ViewCompat.postInvalidateOnAnimation(this)
        }
        measureContext.horizontalScroll = scrollHandler.currentScroll
        val drawContext = chartDrawContext(
            canvas = canvas,
            colors = context.colors,
            measureContext = measureContext,
            markerTouchPoint = markerTouchPoint,
            segmentProperties = chart.getSegmentProperties(measureContext, model),
            chartModel = chartModel,
            chartBounds = chart.bounds,
        )
        axisManager.drawBehindChart(drawContext)
        chart.draw(drawContext, model)
        axisManager.drawAboveChart(drawContext)

        ifNotNull(
            t1 = marker,
            t2 = markerTouchPoint?.let(chart.entryLocationMap::getClosestMarkerEntryModel)
        ) { marker, markerModel ->
            marker.draw(
                context = drawContext,
                bounds = chart.bounds,
                markedEntries = markerModel,
            )
        }

        updateMaxScrollDistance(
            segmentWidth = drawContext.segmentProperties.segmentWidth,
            drawnEntryCount = model.getDrawnEntryCount(),
            chartBounds = chart.bounds.width(),
        )
    }

    private fun updateMaxScrollDistance(segmentWidth: Float, drawnEntryCount: Int, chartBounds: Float) {
        scrollHandler.maxScrollDistance = segmentWidth * drawnEntryCount - chartBounds
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> Dimens.CHART_HEIGHT.dpInt + verticalPadding
            MeasureSpec.AT_MOST -> minOf(
                Dimens.CHART_HEIGHT.dpInt + verticalPadding,
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

    private fun updateBounds() = withChartAndModel { chart, _ ->
        measureContext.clearExtras()
        virtualLayout.setBounds(
            context = measureContext,
            contentBounds = contentBounds,
            chart = chart,
            chartModel = chartModel,
            axisManager = axisManager,
            marker
        )
    }

    private inline fun withChartAndModel(block: (chart: Chart<Model>, model: Model) -> Unit) {
        val chart = chart ?: return
        val model = model ?: return
        block(chart, model)
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        measureContext.isLtr = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
    }
}
