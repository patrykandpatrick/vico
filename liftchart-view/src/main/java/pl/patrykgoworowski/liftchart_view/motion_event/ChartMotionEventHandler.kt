package pl.patrykgoworowski.liftchart_view.motion_event

import android.graphics.PointF
import android.view.MotionEvent
import pl.patrykgoworowski.liftchart_common.extension.pointF

public open class ChartMotionEventHandler(
    private var isHorizontalScrollEnabled: Boolean = false,
    private val onTouchPoint: (PointF?) -> Unit,
    private val onHorizontalScroll: (Float) -> Unit,
) {

    private var lastTouch = PointF(0f, 0f)
    private var currentTouch = PointF(0f, 0f)

    public fun handleTouchPoint(motionEvent: MotionEvent): Boolean =
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchPoint(lastTouch)
                lastTouch = motionEvent.pointF
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalScrollEnabled) {
                    currentTouch = motionEvent.pointF
                    onHorizontalScroll(currentTouch.x - lastTouch.x)
                    lastTouch = motionEvent.pointF
                } else {
                    onTouchPoint(motionEvent.pointF)
                }
                true
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                onTouchPoint(null)
                true
            }
            else -> false
        }

}