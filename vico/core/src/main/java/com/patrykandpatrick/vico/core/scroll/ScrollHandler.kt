/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.scroll

import com.patrykandpatrick.vico.core.extension.rangeWith
import kotlin.properties.Delegates

/**
 * Handles scroll events.
 *
 * @param initialMaxValue the initial maximum scroll amount.
 */
public class ScrollHandler(initialMaxValue: Float = 0f) : ScrollListenerHost {

    /**
     * Handles scroll events.
     *
     * @param setScrollAmount called when the scroll amount changes.
     * @param maxScrollDistance the initial maximum scroll amount.
     */
    @Deprecated(
        message = """Use the primary constructor. `initialMaxValue` replaces `maxScrollDistance`, and you can register a
            `ScrollListener` instead of using `setScrollAmount`.""",
    )
    public constructor(
        setScrollAmount: (Float) -> Unit = {},
        maxScrollDistance: Float = 0f,
    ) : this(maxScrollDistance) {
        registerScrollListener(
            object : ScrollListener {
                override fun onValueChanged(oldValue: Float, newValue: Float) {
                    setScrollAmount(newValue)
                }
            },
        )
    }

    private var initialScrollHandled: Boolean = false
    private val scrollListeners: MutableSet<ScrollListener> = mutableSetOf()

    /**
     * The current scroll amount (in pixels).
     */
    public var value: Float by Delegates.observable(0f) { _, oldValue, newValue ->
        scrollListeners.forEach { scrollListener -> scrollListener.onValueChanged(oldValue, newValue) }
    }

    /**
     * The maximum scroll amount (in pixels).
     */
    public var maxValue: Float by Delegates.observable(initialMaxValue) { _, oldMaxValue, newMaxValue ->
        scrollListeners.forEach { scrollListener -> scrollListener.onMaxValueChanged(oldMaxValue, newMaxValue) }
    }

    /**
     * The current scroll amount (in pixels).
     */
    @Deprecated("Use the `value` field instead.")
    public var currentScroll: Float
        get() = value
        set(newCurrentScroll) { value = newCurrentScroll }

    /**
     * The maximum scroll amount (in pixels).
     */
    @Deprecated("Use the `maxValue` field instead.")
    public var maxScrollDistance: Float
        get() = maxValue
        set(newMaxScrollDistance) { maxValue = newMaxScrollDistance }

    private fun getClampedScroll(scroll: Float): Float = scroll.coerceIn(0f.rangeWith(maxValue))

    /**
     * Increments [value] by [delta] if the result of the operation belongs to the interval [0, [maxValue]].
     */
    public fun handleScrollDelta(delta: Float): Float {
        val previousScroll = value
        value = getClampedScroll(value - delta)
        val unconsumedScroll = previousScroll - value - delta
        if (unconsumedScroll != 0f) notifyUnconsumedScroll(delta)
        return previousScroll - value
    }

    /**
     * Checks whether a scroll by the given [delta] value is possible.
     */
    public fun canScrollBy(delta: Float): Boolean = delta == 0f || value - getClampedScroll(value - delta) != 0f

    /**
     * Updates [value] to [targetScroll], which is restricted to the interval [0, [maxValue]].
     */
    public fun handleScroll(targetScroll: Float): Float = handleScrollDelta(value - targetScroll)

    /**
     * Updates [value] to match the provided [InitialScroll].
     */
    public fun handleInitialScroll(initialScroll: InitialScroll) {
        if (initialScrollHandled) return
        value = when (initialScroll) {
            InitialScroll.Start -> 0f
            InitialScroll.End -> maxValue
        }
        initialScrollHandled = true
    }

    public override fun registerScrollListener(scrollListener: ScrollListener) {
        with(scrollListener) {
            if (scrollListeners.add(this).not()) return@with
            onValueChanged(oldValue = value, newValue = value)
            onMaxValueChanged(oldMaxValue = maxValue, newMaxValue = maxValue)
        }
    }

    public override fun removeScrollListener(scrollListener: ScrollListener) {
        scrollListeners.remove(scrollListener)
    }

    private fun notifyUnconsumedScroll(delta: Float) {
        scrollListeners.forEach { scrollListener -> scrollListener.onScrollNotConsumed(delta) }
    }
}
