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

package com.patrykandpatrick.vico.compose.chart.scroll

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.core.extension.rangeWith
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import com.patrykandpatrick.vico.core.scroll.ScrollListener
import com.patrykandpatrick.vico.core.scroll.ScrollListenerHost
import kotlin.math.abs

/**
 * Houses information on a [Chart]’s scroll state. Allows for programmatic scrolling.
 */
public class ChartScrollState : ScrollableState, ScrollListenerHost {

    private val _value: MutableState<Float> = mutableStateOf(0f)
    private val _maxValue: MutableState<Float> = mutableStateOf(0f)
    private val scrollListeners: MutableSet<ScrollListener> = mutableSetOf()
    private var initialScrollHandled: Boolean = false

    /**
     * The current scroll amount (in pixels).
     */
    public var value: Float
        get() = _value.value
        private set(newValue) {
            val oldValue = value
            _value.value = newValue
            scrollListeners.forEach { scrollListener -> scrollListener.onValueChanged(oldValue, newValue) }
        }

    /**
     * The maximum scroll amount (in pixels).
     */
    public var maxValue: Float
        get() = _maxValue.value
        internal set(newMaxValue) {
            val oldMaxValue = maxValue
            _maxValue.value = newMaxValue
            if (abs(value) > abs(newMaxValue)) value = newMaxValue
            scrollListeners.forEach { scrollListener -> scrollListener.onMaxValueChanged(oldMaxValue, newMaxValue) }
        }

    private val scrollableState = ScrollableState { delta ->
        val unlimitedValue = value + delta
        val limitedValue = unlimitedValue.coerceIn(0f.rangeWith(maxValue))
        val consumedValue = limitedValue - value
        value += consumedValue

        val unconsumedScroll = delta - consumedValue
        if (unconsumedScroll != 0f) notifyUnconsumedScroll(unconsumedScroll)

        if (unlimitedValue != limitedValue) consumedValue else delta
    }

    override val isScrollInProgress: Boolean
        get() = scrollableState.isScrollInProgress

    override suspend fun scroll(scrollPriority: MutatePriority, block: suspend ScrollScope.() -> Unit) {
        scrollableState.scroll(scrollPriority, block)
    }

    override fun dispatchRawDelta(delta: Float): Float = scrollableState.dispatchRawDelta(delta)

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

    internal fun handleInitialScroll(initialScroll: InitialScroll) {
        if (initialScrollHandled) return
        value = when (initialScroll) {
            InitialScroll.Start -> 0f
            InitialScroll.End -> maxValue
        }
        initialScrollHandled = true
    }

    private fun notifyUnconsumedScroll(delta: Float) {
        scrollListeners.forEach { scrollListener -> scrollListener.onScrollNotConsumed(delta) }
    }
}

/**
 * Creates and remembers a [ChartScrollState] instance.
 */
@Composable
public fun rememberChartScrollState(): ChartScrollState = remember { ChartScrollState() }
