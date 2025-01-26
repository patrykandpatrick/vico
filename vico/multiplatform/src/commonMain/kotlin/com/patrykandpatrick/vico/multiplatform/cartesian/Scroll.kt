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

import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.multiplatform.cartesian.Scroll.Absolute
import com.patrykandpatrick.vico.multiplatform.cartesian.Scroll.Relative
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerDimensions

/** Represents a [CartesianChart] scroll value or delta. */
public sealed interface Scroll {
  /** Represents a [CartesianChart] scroll value. */
  public fun interface Absolute : Scroll {
    /** Returns the scroll value. */
    public fun getValue(
      context: CartesianMeasuringContext,
      layerDimensions: CartesianLayerDimensions,
      bounds: Rect,
      maxValue: Float,
    ): Float

    /** Houses [Scroll.Absolute] singletons and factory functions. */
    public companion object {
      /** Corresponds to zero. */
      public val Start: Absolute = Absolute { _, _, _, _ -> 0f }

      /** Corresponds to the maximum scroll value. */
      public val End: Absolute = Absolute { _, _, _, maxValue -> maxValue }

      /** Uses a scroll value of the specified number of pixels. */
      public fun pixels(pixels: Float): Absolute = Absolute { _, _, _, _ -> pixels }

      /**
       * Scrolls to the specified _x_ coordinate, positioning it anywhere between the start edge
       * ([bias] = 0) and the end edge ([bias] = 1) of the [CartesianChart].
       */
      public fun x(x: Double, bias: Float = 0f): Absolute =
        Absolute { context, layerDimensions, bounds, _ ->
          layerDimensions.startPadding +
            ((x - context.ranges.minX) / context.ranges.xStep).toFloat() *
              layerDimensions.xSpacing - bias * bounds.width
        }
    }
  }

  /** Represents a [CartesianChart] scroll delta. */
  public fun interface Relative : Scroll {
    /** Returns the scroll delta. */
    public fun getDelta(
      context: CartesianMeasuringContext,
      layerDimensions: CartesianLayerDimensions,
      bounds: Rect,
      maxValue: Float,
    ): Float

    /** Houses [Scroll.Relative] factory functions. */
    public companion object {
      /** Scrolls by the specified number of pixels. */
      public fun pixels(pixels: Float): Relative = Relative { _, _, _, _ -> pixels }

      /** Scrolls by the specified number of _x_ units. */
      public fun x(x: Double): Relative = Relative { context, layerDimensions, _, _ ->
        (x / context.ranges.xStep).toFloat() * layerDimensions.xSpacing
      }
    }
  }
}

internal fun Scroll.getDelta(
  context: CartesianMeasuringContext,
  layerDimensions: CartesianLayerDimensions,
  bounds: Rect,
  maxValue: Float,
  value: Float,
): Float =
  when (this) {
    is Absolute -> getValue(context, layerDimensions, bounds, maxValue) - value
    is Relative -> getDelta(context, layerDimensions, bounds, maxValue)
  }
