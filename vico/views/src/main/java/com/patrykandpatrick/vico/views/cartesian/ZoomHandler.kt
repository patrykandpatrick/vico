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

package com.patrykandpatrick.vico.views.cartesian

import android.graphics.RectF
import android.os.Bundle
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.scale
import com.patrykandpatrick.vico.core.common.Defaults

/**
 * Houses information on a [CartesianChart]’s zoom factor. Allows for zoom customization.
 *
 * @param zoomEnabled whether zoom is enabled.
 * @param initialZoom represents the initial zoom factor.
 * @param minZoom represents the minimum zoom factor.
 * @param maxZoom represents the maximum zoom factor.
 */
public class ZoomHandler(
  internal val zoomEnabled: Boolean = true,
  private val initialZoom: Zoom = Zoom.max(Zoom.fixed(), Zoom.Content),
  private val minZoom: Zoom = Zoom.Content,
  private val maxZoom: Zoom = Zoom.max(Zoom.fixed(Defaults.MAX_ZOOM), Zoom.Content),
) {
  private var overridden = false
  private val listeners = mutableSetOf<Listener>()

  /** The current zoom factor. */
  public var value: Float = 0f
    private set(newValue) {
      val oldValue = field
      field = newValue.coerceIn(valueRange)
      if (field != oldValue) listeners.forEach { it.onValueChanged(oldValue, field) }
    }

  /** The range of zoom factors. */
  public var valueRange: ClosedFloatingPointRange<Float> = 0f..0f
    private set(newValueRange) {
      val oldValueRange = field
      if (newValueRange == oldValueRange) return
      field = newValueRange
      listeners.forEach { it.onValueRangeChanged(oldValueRange, field) }
      value = value
    }

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
    if (value == oldValue) Scroll.Relative.pixels(0f)
    val transformationAxisX = scroll + centroidX - bounds.left
    val zoomedTransformationAxisX = transformationAxisX * (value / oldValue)
    return Scroll.Relative.pixels(zoomedTransformationAxisX - transformationAxisX)
  }

  internal fun saveInstanceState(bundle: Bundle) {
    bundle.putFloat(VALUE_KEY, value)
    bundle.putBoolean(OVERRIDDEN_KEY, overridden)
  }

  internal fun restoreInstanceState(bundle: Bundle) {
    value = bundle.getFloat(VALUE_KEY)
    overridden = bundle.getBoolean(OVERRIDDEN_KEY)
  }

  /** Adds the provided [Listener]. */
  public fun addListener(listener: Listener): Boolean {
    if (!listeners.add(listener)) return false
    listener.onValueChanged(value, value)
    listener.onValueRangeChanged(valueRange, valueRange)
    return true
  }

  /** Removes the provided [Listener]. */
  public fun removeListener(listener: Listener): Boolean = listeners.remove(listener)

  /** Facilitates listening for zoom events. */
  public interface Listener {
    /** Called when the zoom factor changes. */
    public fun onValueChanged(old: Float, new: Float) {}

    /** Called when the range of zoom factors changes. */
    public fun onValueRangeChanged(
      old: ClosedFloatingPointRange<Float>,
      new: ClosedFloatingPointRange<Float>,
    ) {}
  }

  internal companion object {
    private const val VALUE_KEY = "value"
    private const val OVERRIDDEN_KEY = "overridden"

    fun default(zoomEnabled: Boolean, scrollEnabled: Boolean) =
      ZoomHandler(
        zoomEnabled = zoomEnabled,
        initialZoom = if (scrollEnabled) Zoom.max(Zoom.fixed(), Zoom.Content) else Zoom.Content,
      )
  }
}
