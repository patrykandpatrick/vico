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

package com.patrykandpatrick.vico.compose.cartesian

import android.graphics.RectF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.getDelta
import com.patrykandpatrick.vico.core.cartesian.getMaxScrollDistance
import com.patrykandpatrick.vico.core.common.rangeWith
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Houses information on a [CartesianChart]’s scroll value. Allows for scroll customization and
 * programmatic scrolling.
 */
public class VicoScrollState {
  private val initialScroll: Scroll.Absolute
  private val autoScroll: Scroll
  private val autoScrollCondition: AutoScrollCondition
  private val autoScrollAnimationSpec: AnimationSpec<Float>
  private val _value: MutableFloatState
  private val _maxValue = mutableFloatStateOf(0f)
  private var initialScrollHandled: Boolean
  private var context: CartesianMeasuringContext? = null
  private var horizontalDimensions: HorizontalDimensions? = null
  private var bounds: RectF? = null
  internal val scrollEnabled: Boolean
  internal val pointerXDeltas = MutableSharedFlow<Float>(extraBufferCapacity = 1)

  internal val scrollableState = ScrollableState { delta ->
    val oldValue = value
    value += delta
    val consumedValue = value - oldValue
    if (oldValue + delta == value) {
      delta
    } else {
      pointerXDeltas.tryEmit(consumedValue - delta)
      consumedValue
    }
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
    initialScroll: Scroll.Absolute,
    autoScroll: Scroll,
    autoScrollCondition: AutoScrollCondition,
    autoScrollAnimationSpec: AnimationSpec<Float>,
    value: Float,
    initialScrollHandled: Boolean,
  ) {
    this.scrollEnabled = scrollEnabled
    this.initialScroll = initialScroll
    this.autoScroll = autoScroll
    this.autoScrollCondition = autoScrollCondition
    this.autoScrollAnimationSpec = autoScrollAnimationSpec
    _value = mutableFloatStateOf(value)
    this.initialScrollHandled = initialScrollHandled
  }

  /**
   * Houses information on a [CartesianChart]’s scroll value. Allows for scroll customization and
   * programmatic scrolling.
   *
   * @param scrollEnabled whether scroll is enabled.
   * @param initialScroll represents the initial scroll value.
   * @param autoScroll represents the scroll value or delta for automatic scrolling.
   * @param autoScrollCondition defines when an automatic scroll should occur.
   * @param autoScrollAnimationSpec the [AnimationSpec] for automatic scrolling.
   */
  public constructor(
    scrollEnabled: Boolean,
    initialScroll: Scroll.Absolute,
    autoScroll: Scroll,
    autoScrollCondition: AutoScrollCondition,
    autoScrollAnimationSpec: AnimationSpec<Float>,
  ) : this(
    scrollEnabled = scrollEnabled,
    initialScroll = initialScroll,
    autoScroll = autoScroll,
    autoScrollCondition = autoScrollCondition,
    autoScrollAnimationSpec = autoScrollAnimationSpec,
    value = 0f,
    initialScrollHandled = false,
  )

  private inline fun withUpdated(
    block: (CartesianMeasuringContext, HorizontalDimensions, RectF) -> Unit
  ) {
    val context = this.context
    val horizontalDimensions = this.horizontalDimensions
    val bounds = this.bounds
    if (context != null && horizontalDimensions != null && bounds != null) {
      block(context, horizontalDimensions, bounds)
    }
  }

  internal fun update(
    context: CartesianMeasuringContext,
    bounds: RectF,
    horizontalDimensions: HorizontalDimensions,
  ) {
    this.context = context
    this.horizontalDimensions = horizontalDimensions
    this.bounds = bounds
    maxValue = context.getMaxScrollDistance(bounds.width(), horizontalDimensions)
    if (!initialScrollHandled) {
      value = initialScroll.getValue(context, horizontalDimensions, bounds, maxValue)
      initialScrollHandled = true
    }
  }

  internal suspend fun autoScroll(model: CartesianChartModel, oldModel: CartesianChartModel?) {
    if (!autoScrollCondition.shouldPerformAutoScroll(model, oldModel)) return
    if (scrollableState.isScrollInProgress)
      scrollableState.stopScroll(MutatePriority.PreventUserInput)
    animateScroll(autoScroll, autoScrollAnimationSpec)
  }

  internal fun clearUpdated() {
    context = null
    horizontalDimensions = null
    bounds = null
  }

  /** Triggers a scroll. */
  public suspend fun scroll(scroll: Scroll) {
    withUpdated { context, horizontalDimensions, bounds ->
      scrollableState.scrollBy(
        scroll.getDelta(context, horizontalDimensions, bounds, maxValue, value)
      )
    }
  }

  /** Triggers an animated scroll. */
  public suspend fun animateScroll(scroll: Scroll, animationSpec: AnimationSpec<Float> = spring()) {
    withUpdated { context, horizontalDimensions, bounds ->
      scrollableState.animateScrollBy(
        scroll.getDelta(context, horizontalDimensions, bounds, maxValue, value),
        animationSpec,
      )
    }
  }

  internal companion object {
    fun Saver(
      scrollEnabled: Boolean,
      initialScroll: Scroll.Absolute,
      autoScroll: Scroll,
      autoScrollCondition: AutoScrollCondition,
      autoScrollAnimationSpec: AnimationSpec<Float>,
    ) =
      Saver<VicoScrollState, Pair<Float, Boolean>>(
        save = { it.value to it.initialScrollHandled },
        restore = { (value, initialScrollHandled) ->
          VicoScrollState(
            scrollEnabled,
            initialScroll,
            autoScroll,
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
  initialScroll: Scroll.Absolute = Scroll.Absolute.Start,
  autoScroll: Scroll = initialScroll,
  autoScrollCondition: AutoScrollCondition = AutoScrollCondition.Never,
  autoScrollAnimationSpec: AnimationSpec<Float> = spring(),
): VicoScrollState =
  rememberSaveable(
    scrollEnabled,
    initialScroll,
    autoScroll,
    autoScrollCondition,
    autoScrollAnimationSpec,
    saver =
      remember(scrollEnabled, initialScroll, autoScrollCondition, autoScrollAnimationSpec) {
        VicoScrollState.Saver(
          scrollEnabled,
          initialScroll,
          autoScroll,
          autoScrollCondition,
          autoScrollAnimationSpec,
        )
      },
  ) {
    VicoScrollState(
      scrollEnabled,
      initialScroll,
      autoScroll,
      autoScrollCondition,
      autoScrollAnimationSpec,
    )
  }
