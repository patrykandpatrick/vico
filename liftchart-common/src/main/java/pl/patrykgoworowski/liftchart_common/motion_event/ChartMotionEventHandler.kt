package pl.patrykgoworowski.liftchart_common.motion_event

import android.graphics.PointF
import android.view.MotionEvent
import pl.patrykgoworowski.liftchart_common.extension.pointF

public open class ChartMotionEventHandler(
    private val onTouchPoint: (PointF?) -> Unit
) {

    public fun handleTouchPoint(motionEvent: MotionEvent): Boolean =
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchPoint(motionEvent.pointF)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                onTouchPoint(motionEvent.pointF)
                true
            }
            MotionEvent.ACTION_UP -> {
                onTouchPoint(null)
                true
            }
            else -> false
        }

}