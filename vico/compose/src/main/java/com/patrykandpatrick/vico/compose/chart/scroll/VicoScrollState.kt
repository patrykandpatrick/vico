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

package com.patrykandpatrick.vico.compose.chart.scroll

import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.rangeWith
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.scroll.AutoScrollCondition
import com.patrykandpatrick.vico.core.scroll.Scroll
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Houses information on a [CartesianChart]’s scroll value. Allows for scroll customization and programmatic scrolling.
 */
public class VicoScrollState : ScrollableState {
    private val initialScroll: Scroll
    private val autoScrollCondition: AutoScrollCondition
    private val autoScrollAnimationSpec: AnimationSpec<Float>
    private val _value: MutableFloatState
    private val _maxValue = mutableFloatStateOf(0f)
    private var initialScrollHandled: Boolean
    internal val scrollEnabled: Boolean
    internal val pointerXDeltas = MutableSharedFlow<Float>(extraBufferCapacity = 1)

    private val scrollableState =
        ScrollableState { delta ->
            val oldValue = value
            value += delta
            val consumedValue = value - oldValue
            if (consumedValue != delta) pointerXDeltas.tryEmit(consumedValue - delta)
            delta
        }

    /** The current scroll value (in pixels). */
    public var value: Float
        get() = _value.floatValue
        private set(newValue) {
            val oldValue = value
            _value.floatValue = newValue.coerceIn(0f.rangeWith(maxValue))
            if (value != oldValue) pointerXDeltas.tryEmit(oldValue - value)
        }

    /** The maximum scroll value (in pixels). */
    public var maxValue: Float
        get() = _maxValue.floatValue
        internal set(newMaxValue) {
            if (newMaxValue == maxValue) return
            _maxValue.floatValue = newMaxValue
            value = value
        }

    internal constructor(
        scrollEnabled: Boolean,
        initialScroll: Scroll,
        autoScrollCondition: AutoScrollCondition,
        autoScrollAnimationSpec: AnimationSpec<Float>,
        value: Float,
        initialScrollHandled: Boolean,
    ) {
        this.scrollEnabled = scrollEnabled
        this.initialScroll = initialScroll
        this.autoScrollCondition = autoScrollCondition
        this.autoScrollAnimationSpec = autoScrollAnimationSpec
        _value = mutableFloatStateOf(value)
        this.initialScrollHandled = initialScrollHandled
    }

    /**
     * Houses information on a [CartesianChart]’s scroll value. Allows for scroll customization and programmatic
     * scrolling.
     *
     * @param scrollEnabled whether scrolling is enabled.
     * @param initialScroll represents the initial scroll value.
     * @param autoScrollCondition defines when an automatic scroll should occur.
     * @param autoScrollAnimationSpec the [AnimationSpec] for automatic scrolling.
     */
    public constructor(
        scrollEnabled: Boolean,
        initialScroll: Scroll,
        autoScrollCondition: AutoScrollCondition,
        autoScrollAnimationSpec: AnimationSpec<Float>,
    ) : this(
        scrollEnabled = scrollEnabled,
        initialScroll = initialScroll,
        autoScrollCondition = autoScrollCondition,
        autoScrollAnimationSpec = autoScrollAnimationSpec,
        value = 0f,
        initialScrollHandled = false,
    )

    internal fun update(
        context: MeasureContext,
        bounds: RectF,
        horizontalDimensions: HorizontalDimensions,
        zoom: Float,
    ) {
        maxValue = context.getMaxScrollDistance(bounds.width(), horizontalDimensions, zoom)
    }

    internal suspend fun autoScroll(
        model: CartesianChartModel,
        oldModel: CartesianChartModel?,
    ) {
        if (!autoScrollCondition.shouldPerformAutoScroll(model, oldModel)) return
        if (isScrollInProgress) stopScroll(MutatePriority.PreventUserInput)
        animateScrollBy(
            value =
                when (initialScroll) {
                    Scroll.Start -> -value
                    Scroll.End -> maxValue - value
                },
            animationSpec = autoScrollAnimationSpec,
        )
    }

    internal fun handleInitialScroll() {
        if (initialScrollHandled) return
        value =
            when (initialScroll) {
                Scroll.Start -> 0f
                Scroll.End -> maxValue
            }
        initialScrollHandled = true
    }

    override val isScrollInProgress: Boolean get() = scrollableState.isScrollInProgress

    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ) {
        scrollableState.scroll(scrollPriority, block)
    }

    override fun dispatchRawDelta(delta: Float): Float = scrollableState.dispatchRawDelta(delta)

    internal companion object {
        fun Saver(
            scrollEnabled: Boolean,
            initialScroll: Scroll,
            autoScrollCondition: AutoScrollCondition,
            autoScrollAnimationSpec: AnimationSpec<Float>,
        ) = Saver<VicoScrollState, Pair<Float, Boolean>>(
            save = { it.value to it.initialScrollHandled },
            restore = { (value, initialScrollHandled) ->
                VicoScrollState(
                    scrollEnabled,
                    initialScroll,
                    autoScrollCondition,
                    autoScrollAnimationSpec,
                    value,
                    initialScrollHandled,
                )
            },
        )
    }
}

/** Creates and remembers a [VicoScrollState] instance. */
@Composable
public fun rememberVicoScrollState(
    scrollEnabled: Boolean = true,
    initialScroll: Scroll = Scroll.Start,
    autoScrollCondition: AutoScrollCondition = AutoScrollCondition.Never,
    autoScrollAnimationSpec: AnimationSpec<Float> = spring(),
): VicoScrollState =
    rememberSaveable(
        scrollEnabled,
        initialScroll,
        autoScrollCondition,
        autoScrollAnimationSpec,
        saver =
            remember(scrollEnabled, initialScroll, autoScrollCondition, autoScrollAnimationSpec) {
                VicoScrollState.Saver(scrollEnabled, initialScroll, autoScrollCondition, autoScrollAnimationSpec)
            },
    ) {
        VicoScrollState(scrollEnabled, initialScroll, autoScrollCondition, autoScrollAnimationSpec)
    }
