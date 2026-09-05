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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.layer.copyScaled
import com.patrykandpatrick.vico.compose.cartesian.layer.scale
import com.patrykandpatrick.vico.compose.common.Defaults
import com.patrykandpatrick.vico.compose.common.getStart

/** Houses information on a [CartesianChart]’s zoom factor. Allows for zoom customization. */
public class VicoZoomState {
  private val initialZoom: Zoom
  private val minZoom: Zoom
  private val maxZoom: Zoom
  private var overridden = false
  private val _value: MutableFloatState
  private val _valueRange = mutableStateOf(0f..0f)
  internal val zoomEnabled: Boolean
  private var context: CartesianMeasuringContext? = null
  private var layerDimensions: MutableCartesianLayerDimensions? = null
  private var bounds: Rect? = null
  private var scrollState: VicoScrollState? = null
  /**
   * The zoom factor by which [layerDimensions] is currently scaled. [update] rescales
   * [layerDimensions] to match [value], but that happens once per draw, while any number of zooms
   * can occur in between, so [value] alone doesn’t describe [layerDimensions]’ current scale.
   */
  private var layerDimensionsZoom = 0f
  private val zoomMutatorMutex = MutatorMutex()

  /** The current zoom factor. */
  public var value: Float
    get() = _value.floatValue
    private set(newValue) {
      _value.floatValue = newValue.coerceIn(valueRange)
    }

  /** The range of zoom factors. */
  public var valueRange: ClosedFloatingPointRange<Float>
    get() = _valueRange.value
    private set(newValueRange) {
      if (newValueRange == valueRange) return
      _valueRange.value = newValueRange
      value = value
    }

  internal constructor(
    zoomEnabled: Boolean,
    initialZoom: Zoom,
    minZoom: Zoom,
    maxZoom: Zoom,
    value: Float,
    overridden: Boolean,
  ) {
    this.zoomEnabled = zoomEnabled
    this.initialZoom = initialZoom
    this.minZoom = minZoom
    this.maxZoom = maxZoom
    _value = mutableFloatStateOf(value)
    this.overridden = overridden
  }

  /**
   * Houses information on a [CartesianChart]’s zoom factor. Allows for zoom customization.
   *
   * @param zoomEnabled whether zoom is enabled.
   * @param initialZoom represents the initial zoom factor.
   * @param minZoom represents the minimum zoom factor.
   * @param maxZoom represents the maximum zoom factor.
   */
  public constructor(
    zoomEnabled: Boolean,
    initialZoom: Zoom,
    minZoom: Zoom,
    maxZoom: Zoom,
  ) : this(
    zoomEnabled = zoomEnabled,
    initialZoom = initialZoom,
    minZoom = minZoom,
    maxZoom = maxZoom,
    value = 0f,
    overridden = false,
  )

  /** Triggers a zoom. */
  public suspend fun zoom(zoom: Zoom) {
    zoomMutatorMutex.mutate {
      withUpdated { context, layerDimensions, bounds ->
        val unscaled = unscale(layerDimensions)
        val newValue = zoom.getValue(context, unscaled, bounds)
        if (newValue != value) rawZoom(newValue / value, getCentroidX(context, bounds, bias = 0.5f))
      }
    }
  }

  /**
   * Triggers an animated zoom.
   *
   * @param bias the zoom anchor’s horizontal position within the [CartesianChart] bounds, from 0
   *   (the start edge) to 1 (the end edge).
   */
  public suspend fun animateZoom(
    zoom: Zoom,
    animationSpec: AnimationSpec<Float> = spring(),
    bias: Float = 0.5f,
  ) {
    zoomMutatorMutex.mutate {
      withUpdated { context, layerDimensions, bounds ->
        val unscaled = unscale(layerDimensions)
        val target = zoom.getValue(context, unscaled, bounds).coerceIn(valueRange)
        if (target == value) return@withUpdated
        val centroidX = getCentroidX(context, bounds, bias)
        val anim = TargetBasedAnimation(animationSpec, Float.VectorConverter, value, target)
        val durationNanos = anim.durationNanos
        if (durationNanos == 0L) {
          rawZoom(target / value, centroidX)
          return@withUpdated
        }
        var startNanos = -1L
        var playTime = 0L
        while (playTime < durationNanos) {
          withFrameNanos { frameNanos ->
            if (startNanos < 0L) startNanos = frameNanos
            playTime = frameNanos - startNanos
          }
          val current = anim.getValueFromNanos(playTime).coerceIn(valueRange)
          val factor = if (value != 0f) current / value else 1f
          rawZoom(factor, centroidX)
        }
        val finalFactor = if (value != 0f) target / value else 1f
        rawZoom(finalFactor, centroidX)
      }
    }
  }

  /**
   * Returns the _x_ coordinate of the zoom anchor at [bias], which runs from 0 at the [bounds]’
   * start edge to 1 at their end edge. [bounds] are the [CartesianChart]’s layer bounds, which are
   * inset from the canvas by the axes, so the canvas’s coordinates can’t stand in for theirs.
   */
  private fun getCentroidX(context: CartesianMeasuringContext, bounds: Rect, bias: Float): Float =
    bounds.getStart(context.isLtr) + context.layoutDirectionMultiplier * bias * bounds.width

  private fun unscale(
    layerDimensions: MutableCartesianLayerDimensions
  ): MutableCartesianLayerDimensions =
    if (layerDimensionsZoom != 0f) {
      layerDimensions.copyScaled(1f / layerDimensionsZoom)
    } else {
      layerDimensions
    }

  private inline fun withUpdated(
    block: (CartesianMeasuringContext, MutableCartesianLayerDimensions, Rect) -> Unit
  ) {
    val context = this.context
    val layerDimensions = this.layerDimensions
    val bounds = this.bounds
    if (context != null && layerDimensions != null && bounds != null) {
      block(context, layerDimensions, bounds)
    }
  }

  /**
   * Sets the [VicoScrollState] whose scroll value anchors zooms, or `null` when there is none.
   *
   * This is deliberately separate from [update], which runs in the draw pass: a host can replace
   * its [VicoScrollState] without a draw following—it returns early before [update] when the chart
   * has no area—and a zoom in that window would otherwise anchor against, and write to, the state
   * that was discarded.
   */
  internal fun setScrollState(scrollState: VicoScrollState?) {
    this.scrollState = scrollState
  }

  internal fun update(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    bounds: Rect,
  ) {
    this.context = context
    this.layerDimensions = layerDimensions
    this.bounds = bounds

    val minValue = minZoom.getValue(context, layerDimensions, bounds)
    val maxValue = maxZoom.getValue(context, layerDimensions, bounds)
    require(maxValue >= minValue) {
      "The zoom factor produced by `maxZoom` ($maxValue) must be no smaller than that produced " +
        "by `minZoom` ($minValue)."
    }
    valueRange = minValue..maxValue
    if (!overridden) value = initialZoom.getValue(context, layerDimensions, bounds)
    layerDimensions.scale(value)
    layerDimensionsZoom = value
  }

  internal fun clearUpdated() {
    context = null
    layerDimensions = null
    bounds = null
    layerDimensionsZoom = 0f
  }

  internal suspend fun zoom(factor: Float, centroidX: Float) {
    zoomMutatorMutex.mutate { rawZoom(factor, centroidX) }
  }

  /**
   * Updates [value] and, to keep the content under the zoom anchor in place, the scroll value.
   *
   * This is deliberately non-suspending, and it applies both updates itself rather than handing the
   * scroll compensation off to a collector. A zoom factor is only meaningful together with the
   * scroll value that anchors it, so the two must be applied in the same dispatch: if the scroll
   * compensation were applied later, a frame could render the new zoom factor with the old scroll
   * value, scaling the content around the wrong anchor and correcting it a frame later—a visible
   * sideways jump, most noticeable on the last, largest step of a fast pinch. Suspending here would
   * also mean the compensation could be canceled after [value] had already been updated (by
   * [zoomMutatorMutex], when the next zoom event arrives mid-gesture), permanently mis-anchoring
   * the content: each step's compensation is an absolute scroll value derived from the current one,
   * so a step that never lands isn't recovered by the steps that follow.
   */
  private fun rawZoom(factor: Float, centroidX: Float) {
    withUpdated { context, layerDimensions, bounds ->
      val oldValue = value
      value *= factor
      // Only now, once the zoom has actually moved. `overridden` permanently stops `update` from
      // re-applying `initialZoom`, and the `Saver` persists it, so a zoom that changes nothing—a
      // pinch that's already clamped at `minZoom` or `maxZoom`—must not set it.
      if (value == oldValue) return@withUpdated
      overridden = true
      if (layerDimensionsZoom == 0f) return@withUpdated
      val scrollState = this.scrollState ?: return@withUpdated
      val scroll = scrollState.value
      // Scale `layerDimensions` from the zoom factor they’re currently scaled by, not from
      // `oldValue`. The two diverge as soon as a second zoom occurs before the next draw, and
      // scaling by the step ratio would then describe `layerDimensionsZoom * factor` rather than
      // `value`, understating the maximum scroll value and clamping the compensation away.
      val maxScrollDistance =
        context.getMaxScrollDistance(
          bounds.width,
          layerDimensions.copyScaled(value / layerDimensionsZoom),
        )
      // The anchor’s distance from the content’s start edge, in scalable content pixels. Scroll
      // values are signed by the layout direction—`layoutDirectionMultiplier * scroll` is the
      // distance from the content’s start edge to the bounds’ start edge—and the anchor’s offset
      // from that edge runs the same way, so the two are converted together. Unlike the dimensions
      // above, the anchor scales by the step ratio, converting `scroll` from `oldValue`’s pixels to
      // `value`’s.
      val transformationAxisX =
        context.layoutDirectionMultiplier * (scroll + centroidX - bounds.getStart(context.isLtr)) -
          layerDimensions.unscalableStartPadding
      val zoomedTransformationAxisX = transformationAxisX * (value / oldValue)
      scrollState.applyZoomScroll(
        value =
          scroll +
            context.layoutDirectionMultiplier * (zoomedTransformationAxisX - transformationAxisX),
        maxValue = maxScrollDistance,
      )
    }
  }

  internal companion object {
    fun Saver(zoomEnabled: Boolean, initialZoom: Zoom, minZoom: Zoom, maxZoom: Zoom) =
      Saver<VicoZoomState, Pair<Float, Boolean>>(
        save = { it.value to it.overridden },
        restore = { (value, overridden) ->
          VicoZoomState(zoomEnabled, initialZoom, minZoom, maxZoom, value, overridden)
        },
      )
  }
}

/** Creates and remembers a [VicoZoomState] instance. */
@Composable
public fun rememberVicoZoomState(
  zoomEnabled: Boolean = true,
  initialZoom: Zoom = remember { Zoom.max(Zoom.fixed(), Zoom.Content) },
  minZoom: Zoom = Zoom.Content,
  maxZoom: Zoom = remember { Zoom.max(Zoom.fixed(Defaults.MAX_ZOOM), Zoom.Content) },
): VicoZoomState =
  rememberSaveable(
    zoomEnabled,
    initialZoom,
    minZoom,
    maxZoom,
    saver =
      remember(zoomEnabled, initialZoom, minZoom, maxZoom) {
        VicoZoomState.Saver(zoomEnabled, initialZoom, minZoom, maxZoom)
      },
  ) {
    VicoZoomState(zoomEnabled, initialZoom, minZoom, maxZoom)
  }

@Composable
internal fun rememberDefaultVicoZoomState(scrollEnabled: Boolean): VicoZoomState {
  val initialZoom = if (scrollEnabled) Zoom.max(Zoom.fixed(), Zoom.Content) else Zoom.Content
  val minZoom = Zoom.Content
  val maxZoom = Zoom.max(Zoom.fixed(Defaults.MAX_ZOOM), Zoom.Content)
  return rememberSaveable(
    saver = remember { VicoZoomState.Saver(true, initialZoom, minZoom, maxZoom) }
  ) {
    VicoZoomState(
      zoomEnabled = true,
      initialZoom = initialZoom,
      minZoom = minZoom,
      maxZoom = maxZoom,
    )
  }
}
