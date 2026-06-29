/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

import androidx.compose.animation.core.*
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.common.rangeWith
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

/**
 * Houses information on a [CartesianChart]’s scroll value. Allows for scroll customization and
 * programmatic scrolling.
 */
public class VicoScrollState {
  private val initialScroll: Scroll.Absolute
  private val autoScroll: Scroll
  private val autoScrollCondition: AutoScrollCondition
  private val autoScrollAnimationSpec: AnimationSpec<Float>
  internal val snapAnimationSpec: AnimationSpec<Float>
  private val dataUpdateScrollAnchor: DataUpdateScrollAnchor
  private val _value: MutableFloatState
  private val _maxValue = mutableFloatStateOf(0f)
  private var initialScrollHandled: Boolean
  private var context: CartesianMeasuringContext? = null
  private var layerDimensions: CartesianLayerDimensions? = null
  private var bounds: Rect? = null
  internal var scrollEnabled by mutableStateOf(true)
  internal val xSnapStep: Double?
  internal val consumedXDeltas = MutableSharedFlow<Float>(extraBufferCapacity = 1)
  internal val unconsumedXDeltas = MutableSharedFlow<Float>(extraBufferCapacity = 1)

  // Previous-measurement layout data, used by `DataUpdateScrollAnchor.VisibleXRange` to keep the
  // same `x` coordinates visible when the dataset changes.
  private var previousMeasurement: Measurement? = null

  internal val scrollableState = ScrollableState { delta ->
    val oldValue = value
    value += delta
    val consumedValue = value - oldValue
    if (oldValue + delta == value) {
      delta
    } else {
      unconsumedXDeltas.tryEmit(consumedValue - delta)
      consumedValue
    }
  }

  private val isScrollInProgress = snapshotFlow { scrollableState.isScrollInProgress }

  /** The current scroll value (in pixels). */
  public var value: Float
    get() = _value.floatValue
    private set(newValue) {
      val oldValue = value
      _value.floatValue = newValue.coerceIn(0f.rangeWith(maxValue))
      if (value != oldValue) consumedXDeltas.tryEmit(oldValue - value)
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
    xSnapStep: Double?,
    snapAnimationSpec: AnimationSpec<Float>,
    dataUpdateScrollAnchor: DataUpdateScrollAnchor,
    value: Float,
    initialScrollHandled: Boolean,
  ) {
    this.scrollEnabled = scrollEnabled
    this.initialScroll = initialScroll
    this.autoScroll = autoScroll
    this.autoScrollCondition = autoScrollCondition
    this.autoScrollAnimationSpec = autoScrollAnimationSpec
    this.xSnapStep = xSnapStep
    this.snapAnimationSpec = snapAnimationSpec
    this.dataUpdateScrollAnchor = dataUpdateScrollAnchor
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
   * @param xSnapStep if not null, the scroll will snap to multiples of this _x_-axis step after the
   *   user stops scrolling.
   * @param snapAnimationSpec the [AnimationSpec] for snap scrolling.
   * @param dataUpdateScrollAnchor defines what happens to the scroll value when the data changes.
   */
  public constructor(
    scrollEnabled: Boolean,
    initialScroll: Scroll.Absolute,
    autoScroll: Scroll,
    autoScrollCondition: AutoScrollCondition,
    autoScrollAnimationSpec: AnimationSpec<Float>,
    xSnapStep: Double?,
    snapAnimationSpec: AnimationSpec<Float>,
    dataUpdateScrollAnchor: DataUpdateScrollAnchor = DataUpdateScrollAnchor.Start,
  ) : this(
    scrollEnabled = scrollEnabled,
    initialScroll = initialScroll,
    autoScroll = autoScroll,
    autoScrollCondition = autoScrollCondition,
    autoScrollAnimationSpec = autoScrollAnimationSpec,
    xSnapStep = xSnapStep,
    snapAnimationSpec = snapAnimationSpec,
    dataUpdateScrollAnchor = dataUpdateScrollAnchor,
    value = 0f,
    initialScrollHandled = false,
  )

  private inline fun withUpdated(
    block: (CartesianMeasuringContext, CartesianLayerDimensions, Rect) -> Unit
  ) {
    val context = this.context
    val layerDimensions = this.layerDimensions
    val bounds = this.bounds
    if (context != null && layerDimensions != null && bounds != null) {
      block(context, layerDimensions, bounds)
    }
  }

  internal fun update(
    context: CartesianMeasuringContext,
    bounds: Rect,
    layerDimensions: CartesianLayerDimensions,
  ) {
    this.context = context
    this.layerDimensions = layerDimensions
    this.bounds = bounds
    maxValue = context.getMaxScrollDistance(bounds.width, layerDimensions)
    val ranges = context.ranges
    val previous = previousMeasurement
    if (!initialScrollHandled) {
      value = initialScroll.getValue(context, layerDimensions, bounds, maxValue)
      initialScrollHandled = true
    } else if (
      dataUpdateScrollAnchor == DataUpdateScrollAnchor.VisibleXRange &&
        previous != null &&
        previous.xSpacing != 0f &&
        ranges.xStep != 0.0 &&
        (ranges.minX != previous.minX || ranges.xStep != previous.xStep)
    ) {
      // The `x` range changed (data was added or removed). Reposition so that the `x` coordinate
      // that was at the chart’s start edge stays there, rather than keeping the raw pixel scroll
      // value—which would make the chart jump when, e.g., older points are prepended. This runs
      // during measurement, so the corrected value is used by the same frame that draws the new
      // data (no flicker).
      val startEdgeX =
        previous.minX + (value - previous.startPadding) / previous.xSpacing * previous.xStep
      value =
        layerDimensions.startPadding +
          ((startEdgeX - ranges.minX) / ranges.xStep).toFloat() * layerDimensions.xSpacing
    }
    previousMeasurement =
      Measurement(
        minX = ranges.minX,
        xStep = ranges.xStep,
        xSpacing = layerDimensions.xSpacing,
        startPadding = layerDimensions.startPadding,
      )
  }

  internal suspend fun autoScroll(model: CartesianChartModel, oldModel: CartesianChartModel?) {
    if (!autoScrollCondition.shouldScroll(oldModel, model)) return
    if (scrollableState.isScrollInProgress)
      scrollableState.stopScroll(MutatePriority.PreventUserInput)
    animateScroll(autoScroll, autoScrollAnimationSpec)
  }

  internal fun clearUpdated() {
    context = null
    layerDimensions = null
    bounds = null
  }

  /** Triggers a scroll. */
  public suspend fun scroll(scroll: Scroll) {
    isScrollInProgress.first { !it }
    withUpdated { context, layerDimensions, bounds ->
      scrollableState.scrollBy(scroll.getDelta(context, layerDimensions, bounds, maxValue, value))
    }
  }

  internal suspend fun scroll(scroll: Scroll, maxScroll: Float) {
    isScrollInProgress.first { !it }
    maxValue = maxScroll
    withUpdated { context, layerDimensions, bounds ->
      scrollableState.scrollBy(scroll.getDelta(context, layerDimensions, bounds, maxValue, value))
    }
  }

  /** Triggers an animated scroll. */
  public suspend fun animateScroll(scroll: Scroll, animationSpec: AnimationSpec<Float> = spring()) {
    withUpdated { context, layerDimensions, bounds ->
      val delta = scroll.getDelta(context, layerDimensions, bounds, maxValue, value)
      val duration =
        animationSpec
          .vectorize(Float.VectorConverter)
          .getDurationNanos(
            initialValue = AnimationVector(value),
            targetValue = AnimationVector(value + delta),
            initialVelocity = AnimationVector(0f),
          )
      if (duration == 0L) {
        scrollableState.scrollBy(delta)
      } else {
        scrollableState.animateScrollBy(delta, animationSpec)
      }
    }
  }

  internal fun getSnapDelta(targetValue: Float = value): Float? {
    val context = this.context ?: return null
    val layerDimensions = this.layerDimensions ?: return null
    return getSnapDelta(
      value = value,
      maxValue = maxValue,
      xSnapStep = xSnapStep,
      xStep = context.ranges.xStep,
      xSpacing = layerDimensions.xSpacing,
      startPadding = layerDimensions.startPadding,
      targetValue = targetValue,
    )
  }

  internal suspend fun performSnap(
    targetValue: Float = value,
    initialVelocity: Float = 0f,
    animationSpec: AnimationSpec<Float> = snapAnimationSpec,
  ) {
    val delta = getSnapDelta(targetValue) ?: return
    scrollableState.stopScroll(MutatePriority.PreventUserInput)
    isScrollInProgress.first { !it }
    scrollableState.scroll(MutatePriority.PreventUserInput) {
      var previousValue = 0f
      animate(
        initialValue = 0f,
        targetValue = delta,
        initialVelocity = initialVelocity,
        animationSpec = animationSpec,
      ) { value, _ ->
        scrollBy(value - previousValue)
        previousValue = value
      }
    }
  }

  internal companion object {
    fun Saver(
      scrollEnabled: Boolean,
      initialScroll: Scroll.Absolute,
      autoScroll: Scroll,
      autoScrollCondition: AutoScrollCondition,
      autoScrollAnimationSpec: AnimationSpec<Float>,
      xSnapStep: Double?,
      snapAnimationSpec: AnimationSpec<Float>,
      dataUpdateScrollAnchor: DataUpdateScrollAnchor,
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
            xSnapStep,
            snapAnimationSpec,
            dataUpdateScrollAnchor,
            value,
            initialScrollHandled,
          )
        },
      )
  }
}

/**
 * Creates and remembers a [VicoScrollState] instance.
 *
 * @param xSnapStep if not null, the scroll will snap to multiples of this _x_-axis step after the
 *   user stops scrolling.
 * @param snapAnimationSpec the [AnimationSpec] for snap scrolling.
 * @param dataUpdateScrollAnchor defines what happens to the scroll value when the data changes.
 */
@Composable
public fun rememberVicoScrollState(
  scrollEnabled: Boolean = true,
  initialScroll: Scroll.Absolute = Scroll.Absolute.Start,
  autoScroll: Scroll = initialScroll,
  autoScrollCondition: AutoScrollCondition = AutoScrollCondition.Never,
  autoScrollAnimationSpec: AnimationSpec<Float> = spring(),
  xSnapStep: Double? = null,
  snapAnimationSpec: AnimationSpec<Float> = spring(),
  dataUpdateScrollAnchor: DataUpdateScrollAnchor = DataUpdateScrollAnchor.Start,
): VicoScrollState {
  val state =
    rememberSaveable(
      initialScroll,
      autoScroll,
      autoScrollCondition,
      autoScrollAnimationSpec,
      xSnapStep,
      snapAnimationSpec,
      dataUpdateScrollAnchor,
      saver =
        remember(
          scrollEnabled,
          initialScroll,
          autoScrollCondition,
          autoScrollAnimationSpec,
          xSnapStep,
          snapAnimationSpec,
          dataUpdateScrollAnchor,
        ) {
          VicoScrollState.Saver(
            scrollEnabled,
            initialScroll,
            autoScroll,
            autoScrollCondition,
            autoScrollAnimationSpec,
            xSnapStep,
            snapAnimationSpec,
            dataUpdateScrollAnchor,
          )
        },
    ) {
      VicoScrollState(
        scrollEnabled,
        initialScroll,
        autoScroll,
        autoScrollCondition,
        autoScrollAnimationSpec,
        xSnapStep,
        snapAnimationSpec,
        dataUpdateScrollAnchor,
      )
    }
  SideEffect { state.scrollEnabled = scrollEnabled }
  return state
}
