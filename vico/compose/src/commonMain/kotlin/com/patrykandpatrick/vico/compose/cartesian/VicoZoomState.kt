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
import androidx.compose.ui.geometry.center
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.layer.copyScaled
import com.patrykandpatrick.vico.compose.cartesian.layer.scale
import com.patrykandpatrick.vico.compose.common.Defaults
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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
  private var scroll = 0f
  private val _pendingScroll = MutableSharedFlow<Pair<Scroll, Float>>()
  internal val pendingScroll = _pendingScroll.asSharedFlow()
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
        val unscaled = if (value != 0f) layerDimensions.copyScaled(1f / value) else layerDimensions
        val newValue = zoom.getValue(context, unscaled, bounds)
        if (newValue != value) {
          rawZoom(newValue / value, context.canvasSize.center.x) { scroll }
        }
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
        val unscaled = if (value != 0f) layerDimensions.copyScaled(1f / value) else layerDimensions
        val target = zoom.getValue(context, unscaled, bounds).coerceIn(valueRange)
        if (target == value) return@withUpdated
        val centroidX = bounds.left + bias * bounds.width
        val anim = TargetBasedAnimation(animationSpec, Float.VectorConverter, value, target)
        val durationNanos = anim.durationNanos
        if (durationNanos == 0L) {
          rawZoom(target / value, centroidX) { scroll }
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
          rawZoom(factor, centroidX) { scroll }
        }
        val finalFactor = if (value != 0f) target / value else 1f
        rawZoom(finalFactor, centroidX) { scroll }
      }
    }
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

  internal fun update(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    bounds: Rect,
    scroll: Float,
  ) {
    this.context = context
    this.layerDimensions = layerDimensions
    this.bounds = bounds
    this.scroll = scroll

    val minValue = minZoom.getValue(context, layerDimensions, bounds)
    val maxValue = maxZoom.getValue(context, layerDimensions, bounds)
    require(maxValue >= minValue) {
      "The zoom factor produced by `maxZoom` ($maxValue) must be no smaller than that produced " +
        "by `minZoom` ($minValue)."
    }
    valueRange = minValue..maxValue
    if (!overridden) value = initialZoom.getValue(context, layerDimensions, bounds)
    layerDimensions.scale(value)
  }

  internal suspend fun zoom(factor: Float, centroidX: Float, scroll: () -> Float) {
    zoomMutatorMutex.mutate { rawZoom(factor, centroidX, scroll) }
  }

  private suspend fun rawZoom(factor: Float, centroidX: Float, scroll: () -> Float) {
    withUpdated { context, layerDimensions, bounds ->
      overridden = true
      val oldValue = value
      value *= factor
      if (value == oldValue) return@withUpdated
      val scroll = scroll()
      val maxScrollDistance =
        context.getMaxScrollDistance(bounds.width, layerDimensions.copyScaled(value / oldValue))
      val transformationAxisX =
        scroll + centroidX - bounds.left - layerDimensions.unscalableStartPadding
      val zoomedTransformationAxisX = transformationAxisX * (value / oldValue)
      _pendingScroll.emit(
        Scroll.Absolute.pixels(scroll + zoomedTransformationAxisX - transformationAxisX) to
          maxScrollDistance
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
