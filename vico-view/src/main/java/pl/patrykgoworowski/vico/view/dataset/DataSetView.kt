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

package pl.patrykgoworowski.vico.view.dataset

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import pl.patrykgoworowski.vico.core.MAX_ZOOM
import pl.patrykgoworowski.vico.core.MIN_ZOOM
import pl.patrykgoworowski.vico.core.axis.AxisManager
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.AxisRenderer
import pl.patrykgoworowski.vico.core.axis.model.MutableDataSetModel
import pl.patrykgoworowski.vico.core.constants.DEF_CHART_WIDTH
import pl.patrykgoworowski.vico.core.dataset.draw.chartDrawContext
import pl.patrykgoworowski.vico.core.extension.set
import pl.patrykgoworowski.vico.core.layout.VirtualLayout
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.scroll.ScrollHandler
import pl.patrykgoworowski.vico.view.common.UpdateRequestListener
import pl.patrykgoworowski.vico.view.dataset.common.DataSetWithModel
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
import kotlin.properties.Delegates.observable

class DataSetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val contentBounds = RectF()
    private val dataSetModel = MutableDataSetModel()
    private val scrollHandler = ScrollHandler()

    private val updateRequestListener: UpdateRequestListener = {
        if (ViewCompat.isAttachedToWindow(this@DataSetView)) {
            updateBounds()
            invalidate()
        }
    }

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
        density = context.density,
        fontScale = context.fontScale,
        isLtr = context.isLtr
    )

    private val scaleGestureListener: ScaleGestureDetector.OnScaleGestureListener =
        ChartScaleGestureListener(
            getChartBounds = { dataSet?.bounds },
            onZoom = this::handleZoom,
        )
    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    private var markerTouchPoint: PointF? = null

    public var startAxis: AxisRenderer<AxisPosition.Vertical.Start>? by axisManager::startAxis
    public var topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? by axisManager::topAxis
    public var endAxis: AxisRenderer<AxisPosition.Vertical.End>? by axisManager::endAxis
    public var bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? by axisManager::bottomAxis

    public var isZoomEnabled = true

    var dataSet: DataSetWithModel<*>? by observable(null) { _, oldValue, newValue ->
        oldValue?.removeListener(updateRequestListener)
        newValue?.addListener(updateRequestListener)
        updateBounds()
    }

    var marker: Marker? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleHandled = if (isZoomEnabled) scaleGestureDetector.onTouchEvent(event) else false
        val touchHandled = motionEventHandler.handleTouchPoint(event)
        return if (scaleHandled || touchHandled) {
            parent.requestDisallowInterceptTouchEvent(dataSet?.isHorizontalScrollEnabled == true)
            true
        } else {
            parent.requestDisallowInterceptTouchEvent(false)
            super.onTouchEvent(event)
        }
    }

    private fun handleZoom(focusX: Float, zoomChange: Float) {
        val dataSet = dataSet ?: return
        val newZoom = (dataSet.zoom ?: 1f) * zoomChange
        if (newZoom !in MIN_ZOOM..MAX_ZOOM) return
        val centerX = scrollHandler.currentScroll + focusX - dataSet.bounds.left
        val zoomedCenterX = centerX * zoomChange
        dataSet.zoom = newZoom
        scrollHandler.currentScroll += zoomedCenterX - centerX
        invalidate()
    }

    private fun handleTouchEvent(pointF: PointF?) {
        markerTouchPoint = pointF
    }

    override fun onDraw(canvas: Canvas) {
        val dataSet = dataSet ?: return
        dataSet.setToAxisModel(dataSetModel)
        motionEventHandler.isHorizontalScrollEnabled = dataSet.isHorizontalScrollEnabled
        if (scroller.computeScrollOffset()) {
            scrollHandler.handleScroll(scroller.currX.toFloat())
            ViewCompat.postInvalidateOnAnimation(this@DataSetView)
        }
        val drawContext = chartDrawContext(
            canvas = canvas,
            measureContext = measureContext,
            horizontalScroll = scrollHandler.currentScroll,
            markerTouchPoint = markerTouchPoint,
            segmentProperties = dataSet.getSegmentProperties(measureContext),
            dataSetModel = dataSetModel,
        )
        axisManager.drawBehindDataSet(drawContext)
        dataSet.draw(drawContext, marker)
        axisManager.drawAboveDataSet(drawContext)
        scrollHandler.maxScrollDistance = dataSet.maxScrollAmount
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> DEF_CHART_WIDTH.dpInt + verticalPadding
            MeasureSpec.AT_MOST -> minOf(
                DEF_CHART_WIDTH.dpInt + verticalPadding,
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
        updateBounds()
    }

    private fun updateBounds() {
        val dataSet = dataSet ?: return
        dataSet.setToAxisModel(dataSetModel)
        virtualLayout.setBounds(
            context = measureContext,
            contentBounds = contentBounds,
            dataSet = dataSet,
            dataSetModel = dataSetModel,
            axisManager = axisManager,
            marker
        )
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        measureContext.isLtr = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
    }
}
