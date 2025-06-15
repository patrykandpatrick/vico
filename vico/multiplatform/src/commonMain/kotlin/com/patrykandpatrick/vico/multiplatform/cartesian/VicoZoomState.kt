/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.scale
import com.patrykandpatrick.vico.multiplatform.common.Defaults
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
  private val _pendingScroll = MutableSharedFlow<Scroll>()
  internal val pendingScroll = _pendingScroll.asSharedFlow()

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
    withUpdated { context, layerDimensions, bounds ->
      val newValue = zoom.getValue(context, layerDimensions, bounds)
      if (newValue != value) {
        zoom(newValue / value, context.canvasSize.center.x, scroll, bounds)
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
    valueRange = minValue..maxValue
    if (!overridden) value = initialZoom.getValue(context, layerDimensions, bounds)
    layerDimensions.scale(value)
  }

  internal suspend fun zoom(factor: Float, centroidX: Float, scroll: Float, bounds: Rect) {
    overridden = true
    val oldValue = value
    value *= factor
    if (value == oldValue) return
    val transformationAxisX = scroll + centroidX - bounds.left
    val zoomedTransformationAxisX = transformationAxisX * (value / oldValue)
    _pendingScroll.emit(Scroll.Relative.pixels(zoomedTransformationAxisX - transformationAxisX))
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
internal fun rememberDefaultVicoZoomState(scrollEnabled: Boolean) =
  rememberVicoZoomState(
    initialZoom =
      if (scrollEnabled) remember { Zoom.max(Zoom.fixed(), Zoom.Content) } else Zoom.Content
  )
