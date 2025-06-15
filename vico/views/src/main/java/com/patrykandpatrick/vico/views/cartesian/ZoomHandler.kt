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
  private var context: CartesianMeasuringContext? = null
  private var layerDimensions: MutableCartesianLayerDimensions? = null
  private var bounds: RectF? = null
  private var scroll = 0f
  private var pendingScroll = mutableListOf<Scroll>()
  internal var invalidate: (() -> Unit)? = null

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

  /** Triggers a zoom. */
  public fun zoom(zoom: Zoom) {
    withUpdated { context, layerDimensions, bounds ->
      val newValue = zoom.getValue(context, layerDimensions, bounds)
      if (newValue != value) {
        zoom(newValue / value, context.canvasBounds.centerX(), scroll, bounds)
      }
    }
  }

  private inline fun withUpdated(
    block: (CartesianMeasuringContext, MutableCartesianLayerDimensions, RectF) -> Unit
  ) {
    val context = this.context
    val layerDimensions = this.layerDimensions
    val bounds = this.bounds
    if (context != null && layerDimensions != null && bounds != null) {
      block(context, layerDimensions, bounds)
    }
  }

  internal inline fun consumePendingScroll(scroll: (Scroll) -> Unit) {
    val iterator = pendingScroll.iterator()
    while (iterator.hasNext()) {
      scroll(iterator.next())
      iterator.remove()
    }
  }

  internal fun update(
    context: CartesianMeasuringContext,
    layerDimensions: MutableCartesianLayerDimensions,
    bounds: RectF,
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

  internal fun zoom(factor: Float, centroidX: Float, scroll: Float, bounds: RectF) {
    overridden = true
    val oldValue = value
    value *= factor
    if (value == oldValue) return
    val transformationAxisX = scroll + centroidX - bounds.left
    val zoomedTransformationAxisX = transformationAxisX * (value / oldValue)
    pendingScroll.add(Scroll.Relative.pixels(zoomedTransformationAxisX - transformationAxisX))
    invalidate?.invoke()
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

  internal fun clearUpdated() {
    context = null
    layerDimensions = null
    bounds = null
    invalidate = null
  }

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
