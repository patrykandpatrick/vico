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

import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import kotlin.math.max
import kotlin.math.min

/**
 * Represents a [CartesianChart]’s zoom factor.
 *
 * Implementations must be pure functions of their arguments and of the values they capture. It’s
 * called on the main thread, while drawing. In a composable function, a lambda converted to [Zoom]
 * is memoized while the values it captures are unchanged (compared via `equals` for stable types
 * and by identity otherwise), so it needs no explicit `remember`.
 */
public fun interface Zoom {
  /** Returns the zoom factor. */
  public fun getValue(
    context: CartesianMeasuringContext,
    layerDimensions: CartesianLayerDimensions,
    bounds: Rect,
  ): Float

  /** Houses [Zoom] singletons and factory functions. */
  public companion object {
    /** Ensures all of the [CartesianChart]’s content is visible. */
    public val Content: Zoom = Zoom { context, layerDimensions, bounds ->
      val scalableContentWidth = layerDimensions.getScalableContentWidth(context)
      if (scalableContentWidth == 0f) {
        1f
      } else {
        (bounds.width - layerDimensions.unscalablePadding) / scalableContentWidth
      }
    }

    /** Uses a zoom factor of [value]. */
    public fun fixed(value: Float = 1f): Zoom = Fixed(value)

    /** Ensures the specified number of _x_ units is visible. */
    public fun x(x: Double): Zoom = X(x)

    /** Uses the smaller of [a]’s zoom factor and [b]’s zoom factor. */
    public fun min(a: Zoom, b: Zoom): Zoom = Min(a, b)

    /** Uses the greater of [a]’s zoom factor and [b]’s zoom factor. */
    public fun max(a: Zoom, b: Zoom): Zoom = Max(a, b)
  }
}

private data class Fixed(private val value: Float) : Zoom {
  override fun getValue(
    context: CartesianMeasuringContext,
    layerDimensions: CartesianLayerDimensions,
    bounds: Rect,
  ) = value
}

private data class X(private val x: Double) : Zoom {
  override fun getValue(
    context: CartesianMeasuringContext,
    layerDimensions: CartesianLayerDimensions,
    bounds: Rect,
  ) = bounds.width * (context.ranges.xStep / x).toFloat() / layerDimensions.xSpacing
}

private data class Min(private val a: Zoom, private val b: Zoom) : Zoom {
  override fun getValue(
    context: CartesianMeasuringContext,
    layerDimensions: CartesianLayerDimensions,
    bounds: Rect,
  ) =
    min(a.getValue(context, layerDimensions, bounds), b.getValue(context, layerDimensions, bounds))
}

private data class Max(private val a: Zoom, private val b: Zoom) : Zoom {
  override fun getValue(
    context: CartesianMeasuringContext,
    layerDimensions: CartesianLayerDimensions,
    bounds: Rect,
  ) =
    max(a.getValue(context, layerDimensions, bounds), b.getValue(context, layerDimensions, bounds))
}
