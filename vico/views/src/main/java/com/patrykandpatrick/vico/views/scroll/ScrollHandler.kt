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

package com.patrykandpatrick.vico.views.scroll

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.RectF
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import com.patrykandpatrick.vico.core.Animation
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.rangeWith
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.scroll.AutoScrollCondition
import com.patrykandpatrick.vico.core.scroll.Scroll

/**
 * Houses information on a [CartesianChart]’s scroll value. Allows for scroll customization and programmatic scrolling.
 *
 * @property scrollEnabled whether scrolling is enabled.
 * @property initialScroll represents the initial scroll value.
 * @property autoScrollCondition defines when an automatic scroll should be performed.
 * @property autoScrollInterpolator the [TimeInterpolator] for automatic scrolling.
 * @property autoScrollDuration the animation duration for automatic scrolling.
 * @property animatedScrollInterpolator the [TimeInterpolator] for animated programmatic scrolling.
 * @property animatedScrollDuration the animation duration for animated programmatic scrolling.
 */
public class ScrollHandler(
    internal val scrollEnabled: Boolean = true,
    private val initialScroll: Scroll = Scroll.Start,
    private val autoScrollCondition: AutoScrollCondition = AutoScrollCondition.Never,
    private val autoScrollInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    private val autoScrollDuration: Long = Animation.DIFF_DURATION.toLong(),
    private val animatedScrollInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    private val animatedScrollDuration: Long = Animation.DIFF_DURATION.toLong(),
) {
    private var initialScrollHandled = false
    private val scrollListeners = mutableSetOf<Listener>()
    internal var postInvalidate: (() -> Unit)? = null
    internal var postInvalidateOnAnimation: (() -> Unit)? = null

    private val autoScrollAnimator =
        ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
            duration = autoScrollDuration
            interpolator = autoScrollInterpolator
        }

    private val animatedScrollAnimator =
        ValueAnimator.ofFloat(Animation.range.start, Animation.range.endInclusive).apply {
            duration = animatedScrollDuration
            interpolator = animatedScrollInterpolator
        }

    /** The current scroll value (in pixels). */
    public var value: Float = 0f
        private set(newValue) {
            val oldValue = field
            field = newValue.coerceIn(0f.rangeWith(maxValue))
            if (field != oldValue) scrollListeners.forEach { it.onValueChanged(oldValue, field) }
        }

    /** The maximum scroll value (in pixels). */
    public var maxValue: Float = 0f
        private set(newMaxValue) {
            val oldMaxValue = field
            if (newMaxValue == oldMaxValue) return
            field = newMaxValue
            scrollListeners.forEach { it.onMaxValueChanged(oldMaxValue, field) }
            value = value
        }

    internal fun update(
        context: MeasureContext,
        bounds: RectF,
        horizontalDimensions: HorizontalDimensions,
        zoom: Float,
    ) {
        maxValue = context.getMaxScrollDistance(bounds.width(), horizontalDimensions, zoom)
    }

    internal fun canScroll(delta: Float) = delta > 0 && value < maxValue || delta == 0f || delta < 0 && value > 0

    internal fun handleInitialScroll() {
        if (initialScrollHandled) return
        value =
            when (initialScroll) {
                Scroll.Start -> 0f
                Scroll.End -> maxValue
            }
        initialScrollHandled = true
    }

    internal fun autoScroll(
        model: CartesianChartModel,
        oldModel: CartesianChartModel?,
    ) {
        if (!autoScrollCondition.shouldPerformAutoScroll(model, oldModel)) return
        with(autoScrollAnimator) {
            cancel()
            removeAllUpdateListeners()
            addUpdateListener { animator ->
                scrollTo(
                    when (initialScroll) {
                        Scroll.Start -> (1 - animator.animatedFraction) * value
                        Scroll.End -> value + animator.animatedFraction * (maxValue - value)
                    },
                )
                postInvalidateOnAnimation?.invoke()
            }
            start()
        }
    }

    internal fun saveInstanceState(bundle: Bundle) {
        bundle.putFloat(VALUE_KEY, value)
        bundle.putBoolean(INITIAL_SCROLL_HANDLED_KEY, initialScrollHandled)
    }

    internal fun restoreInstanceState(bundle: Bundle) {
        value = bundle.getFloat(VALUE_KEY)
        initialScrollHandled = bundle.getBoolean(INITIAL_SCROLL_HANDLED_KEY)
    }

    /** Scrolls by [delta]. */
    public fun scrollBy(delta: Float): Float {
        val oldValue = value
        value += delta
        postInvalidate?.invoke()
        return value - oldValue
    }

    /** Scrolls to [value]. */
    public fun scrollTo(value: Float): Float = scrollBy(value - this.value)

    /** Scrolls by [delta] with an animation. */
    public fun animateScrollBy(delta: Float): Float {
        val oldValue = value
        val limitedDelta = delta.coerceIn((-value).rangeWith(maxValue - value))
        with(animatedScrollAnimator) {
            cancel()
            removeAllUpdateListeners()
            addUpdateListener { animator ->
                scrollTo(oldValue + animator.animatedFraction * limitedDelta)
                postInvalidateOnAnimation?.invoke()
            }
            start()
        }
        return limitedDelta
    }

    /** Scrolls to [value] with an animation. */
    public fun animateScrollTo(value: Float): Float = animateScrollBy(value - this.value)

    /** Adds the provided [Listener]. */
    public fun addListener(listener: Listener): Boolean {
        if (!scrollListeners.add(listener)) return false
        listener.onValueChanged(value, value)
        listener.onMaxValueChanged(maxValue, maxValue)
        return true
    }

    /** Removes the provided [Listener]. */
    public fun removeListener(listener: Listener): Boolean = scrollListeners.remove(listener)

    /** Facilitates listening for scroll events. */
    public interface Listener {
        /** Called when the scroll value changes. */
        public fun onValueChanged(
            oldValue: Float,
            newValue: Float,
        ) {}

        /** Called when the maximum scroll value changes. */
        public fun onMaxValueChanged(
            oldMaxValue: Float,
            newMaxValue: Float,
        ) {}
    }

    private companion object {
        const val VALUE_KEY = "value"
        const val INITIAL_SCROLL_HANDLED_KEY = "initialScrollHandled"
    }
}
