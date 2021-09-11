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

package pl.patrykgoworowski.vico.view.gestures

import android.annotation.SuppressLint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.OverScroller
import pl.patrykgoworowski.vico.core.extension.pointF
import pl.patrykgoworowski.vico.core.scroll.ScrollHandler
import pl.patrykgoworowski.vico.view.extension.fling
import kotlin.math.abs

public open class MotionEventHandler(
    private val scroller: OverScroller,
    private val scrollHandler: ScrollHandler,
    density: Float,
    var isHorizontalScrollEnabled: Boolean = false,
    private val onTouchPoint: (PointF?) -> Unit,
    private val requestInvalidate: () -> Unit,
) {

    private val velocityUnits = (VELOCITY_PIXELS * density).toInt()
    private val dragThreshold = DRAG_THRESHOLD_PIXELS * density
    private var initialX = -dragThreshold
    private var lastX = 0f
    private var currentX = 0f
    private var velocityTracker = VelocityTrackerHelper()
    private var lastEventPointerCount = 0

    public fun handleTouchPoint(motionEvent: MotionEvent): Boolean {
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

    companion object {
        private const val VELOCITY_PIXELS = 400
        private const val DRAG_THRESHOLD_PIXELS = 8
    }
}
