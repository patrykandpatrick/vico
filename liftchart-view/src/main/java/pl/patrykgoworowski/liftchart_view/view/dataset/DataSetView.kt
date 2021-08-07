package pl.patrykgoworowski.liftchart_view.view.dataset

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import pl.patrykgoworowski.liftchart_common.axis.*
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.constants.DEF_CHART_WIDTH
import pl.patrykgoworowski.liftchart_common.data_set.renderer.MutableRendererViewState
import pl.patrykgoworowski.liftchart_common.extension.dpInt
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.scroll.ScrollHandler
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel
import pl.patrykgoworowski.liftchart_view.data_set.layout.ViewVirtualLayout
import pl.patrykgoworowski.liftchart_view.extension.*
import pl.patrykgoworowski.liftchart_view.motion_event.MotionEventHandler
import java.util.*
import kotlin.properties.Delegates.observable

class DataSetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val contentBounds = RectF()
    private val dataSetModel = MutableDataSetModel()
    private val scrollHandler = ScrollHandler(::handleHorizontalScroll)

    private val updateRequestListener: UpdateRequestListener = {
        if (ViewCompat.isAttachedToWindow(this@DataSetView)) {
            updateBounds()
            invalidate()
        }
    }

    private val scroller = OverScroller(context)
    private val virtualLayout = ViewVirtualLayout(isLTR)
    private val motionEventHandler = MotionEventHandler(
        scroller = scroller,
        scrollHandler = scrollHandler,
        density = resources.displayMetrics.density,
        onTouchPoint = ::handleTouchEvent,
        requestInvalidate = { invalidate() }
    )

    private val rendererViewState = MutableRendererViewState()

    var dataSet: DataSetRendererWithModel<*>? by observable(null) { _, oldValue, newValue ->
        oldValue?.removeListener(updateRequestListener)
        newValue?.addListener(updateRequestListener)
        updateBounds()
    }

    var axisManager = AxisManager()
    var marker: Marker? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (motionEventHandler.handleTouchPoint(event)) {
            parent.requestDisallowInterceptTouchEvent(dataSet?.isHorizontalScrollEnabled == true)
            true
        } else {
            parent.requestDisallowInterceptTouchEvent(false)
            super.onTouchEvent(event)
        }
    }

    private fun handleTouchEvent(pointF: PointF?) {
        rendererViewState.markerTouchPoint = pointF
    }

    private fun handleHorizontalScroll(scroll: Float) {
        rendererViewState.apply {
            horizontalScroll = scroll
            markerTouchPoint = null
        }
    }

    override fun onDraw(canvas: Canvas) {
        val dataSet = dataSet ?: return
        dataSet.setToAxisModel(dataSetModel)
        motionEventHandler.isHorizontalScrollEnabled = dataSet.isHorizontalScrollEnabled
        val segmentProperties = dataSet.getSegmentProperties()
        if (scroller.computeScrollOffset()) {
            scrollHandler.handleScroll(scroller.currX.toFloat())
            handleHorizontalScroll(scrollHandler.currentScroll)
            ViewCompat.postInvalidateOnAnimation(this@DataSetView)
        }

        axisManager.draw(
            canvas, dataSet.getEntriesModel(), dataSetModel, segmentProperties,
            rendererViewState
        )
        dataSet.draw(canvas, rendererViewState, marker)
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
        virtualLayout.setBounds(contentBounds, dataSet, dataSetModel, axisManager, marker)
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        val isLTR = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
        virtualLayout.isLTR = isLTR
    }
}