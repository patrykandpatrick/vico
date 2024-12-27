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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.scale
import com.patrykandpatrick.vico.core.common.Defaults

/** Houses information on a [CartesianChart]’s zoom factor. Allows for zoom customization. */
public class VicoZoomState {
  private val initialZoom: Zoom
  private val minZoom: Zoom
  private val maxZoom: Zoom
  private var overridden = false
  private val _value: MutableFloatState
  private val _valueRange = mutableStateOf(0f..0f)
  internal val zoomEnabled: Boolean

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

  internal fun update(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    bounds: RectF,
  ) {
    val minValue = minZoom.getValue(context, layerDimensions, bounds)
    val maxValue = maxZoom.getValue(context, layerDimensions, bounds)
    valueRange = minValue..maxValue
    if (!overridden) value = initialZoom.getValue(context, layerDimensions, bounds)
    layerDimensions.scale(value)
  }

  internal fun zoom(factor: Float, centroidX: Float, scroll: Float, bounds: RectF): Scroll {
    overridden = true
    val oldValue = value
    value *= factor
    if (value == oldValue) return Scroll.Relative.pixels(0f)
    val transformationAxisX = scroll + centroidX - bounds.left
    val zoomedTransformationAxisX = transformationAxisX * (value / oldValue)
    return Scroll.Relative.pixels(zoomedTransformationAxisX - transformationAxisX)
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
