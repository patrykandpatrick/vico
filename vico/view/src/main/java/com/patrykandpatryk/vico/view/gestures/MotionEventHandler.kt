/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatryk.vico.view.gestures

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.OverScroller
import com.patrykandpatryk.vico.core.marker.Marker
import com.patrykandpatryk.vico.core.model.Point
import com.patrykandpatryk.vico.core.scroll.ScrollHandler
import com.patrykandpatryk.vico.view.extension.fling
import com.patrykandpatryk.vico.view.extension.point
import kotlin.math.abs

/**
 * Handles [MotionEvent]s.
 *
 * @param density the pixel density.
 * @param isHorizontalScrollEnabled whether horizontal scrolling is enabled.
 */
public open class MotionEventHandler(
    private val scroller: OverScroller,
    private val scrollHandler: ScrollHandler,
    density: Float,
    public var isHorizontalScrollEnabled: Boolean = false,
    private val onTouchPoint: (Point?) -> Unit,
    private val requestInvalidate: () -> Unit,
) {

    private val velocityUnits = (VELOCITY_PIXELS * density).toInt()
    private val dragThreshold = DRAG_THRESHOLD_PIXELS * density
    private var initialX = -dragThreshold
    private var lastX = 0f
    private var currentX = 0f
    private var velocityTracker = VelocityTrackerHelper()
    private var lastEventPointerCount = 0
    private var totalDragAmount: Float = 0f

    /**
     * Called to handle a [MotionEvent], which may result in scroll, zoom, or the appearance of a [Marker].
     */
    public fun handleMotionEvent(motionEvent: MotionEvent): Boolean {
        val ignoreEvent =
            motionEvent.pointerCount > 1 || lastEventPointerCount > motionEvent.pointerCount
        lastEventPointerCount = motionEvent.pointerCount

        return when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                scroller.abortAnimation()
                initialX = motionEvent.x
                onTouchPoint(motionEvent.point)
                lastX = initialX
                currentX = initialX
                velocityTracker.get().addMovement(motionEvent)
                requestInvalidate()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                var scrollHandled = false
                if (isHorizontalScrollEnabled) {
                    currentX = motionEvent.x
                    totalDragAmount += abs(lastX - currentX)
                    val shouldPerformScroll = totalDragAmount > dragThreshold
                    if (shouldPerformScroll && !ignoreEvent) {
                        velocityTracker.get().addMovement(motionEvent)
                        scrollHandler.handleScrollDelta(currentX - lastX)
                        onTouchPoint(motionEvent.point)
                        requestInvalidate()
                        initialX = -dragThreshold
                    }
                    scrollHandled = shouldPerformScroll.not() || scrollHandler.canScrollBy(currentX - lastX)
                    lastX = motionEvent.x
                } else {
                    onTouchPoint(motionEvent.point)
                    requestInvalidate()
                }
                scrollHandled
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP,
            -> {
                totalDragAmount = 0f
                onTouchPoint(null)
                velocityTracker.get().apply {
                    computeCurrentVelocity(velocityUnits)
                    val currentX = scrollHandler.value.toInt()
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

    private companion object {
        const val VELOCITY_PIXELS = 400
        const val DRAG_THRESHOLD_PIXELS = 8
    }
}
