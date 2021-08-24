package pl.patrykgoworowski.liftchart_view.motion_event

import android.annotation.SuppressLint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.OverScroller
import pl.patrykgoworowski.liftchart_common.extension.pointF
import pl.patrykgoworowski.liftchart_common.scroll.ScrollHandler
import pl.patrykgoworowski.liftchart_view.extension.fling
import kotlin.math.abs

open class MotionEventHandler(
    private val scroller: OverScroller,
    private val scrollHandler: ScrollHandler,
    density: Float,
    var isHorizontalScrollEnabled: Boolean = false,
    private val onTouchPoint: (PointF?) -> Unit,
    private val requestInvalidate: () -> Unit,
) {

    private val velocityUnits = (400 * density).toInt()
    private val dragThreshold = 8f * density
    private var initialX = -dragThreshold
    private var lastX = 0f
    private var currentX = 0f
    private var velocityTracker = VelocityTrackerHelper()
    private var lastEventPointerCount = 0

    fun handleTouchPoint(motionEvent: MotionEvent): Boolean {
        val ignoreEvent =
            motionEvent.pointerCount > 1 || lastEventPointerCount > motionEvent.pointerCount
        lastEventPointerCount = motionEvent.pointerCount

        return when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                scroller.abortAnimation()
                initialX = motionEvent.x
                onTouchPoint(motionEvent.pointF)
                lastX = initialX
                currentX = initialX
                velocityTracker.get().addMovement(motionEvent)
                requestInvalidate()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalScrollEnabled) {
                    currentX = motionEvent.x
                    if (abs(currentX - initialX) > dragThreshold && !ignoreEvent) {
                        velocityTracker.get().addMovement(motionEvent)
                        scrollHandler.handleScrollDelta(currentX - lastX)
                        onTouchPoint(null)
                        requestInvalidate()
                        initialX = -dragThreshold
                    }
                    lastX = motionEvent.x
                } else {
                    onTouchPoint(motionEvent.pointF)
                    requestInvalidate()
                }
                true
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                onTouchPoint(null)
                velocityTracker.get().apply {
                    computeCurrentVelocity(velocityUnits)
                    val currentX = scrollHandler.currentScroll.toInt()
                    scroller.fling(startX = currentX, velocityX = -xVelocity.toInt())
                    requestInvalidate()
                }
                velocityTracker.clear()
                true
            }
            else -> false
        }
    }

    private class VelocityTrackerHelper {

        private var tracker: VelocityTracker? = null

        @SuppressLint("Recycle")
        fun get(): VelocityTracker =
            tracker ?: VelocityTracker.obtain().also { tracker = it }

        fun clear() {
            tracker?.recycle()
            tracker = null
        }

    }

}