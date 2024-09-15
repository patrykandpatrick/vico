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

package com.patrykandpatrick.vico.core.cartesian

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

/** Represents a [CartesianChart]’s zoom factor. */
public fun interface Zoom {
  /** Returns the zoom factor. */
  public fun getValue(
    context: CartesianMeasuringContext,
    horizontalDimensions: HorizontalDimensions,
    bounds: RectF,
  ): Float

  /** Houses [Zoom] singletons and factory functions. */
  public companion object {
    /** Ensures all of the [CartesianChart]’s content is visible. */
    public val Content: Zoom = Zoom { context, horizontalDimensions, bounds ->
      val scalableContentWidth = horizontalDimensions.getScalableContentWidth(context)
      if (scalableContentWidth == 0f) {
        1f
      } else {
        (bounds.width() - horizontalDimensions.unscalablePadding) / scalableContentWidth
      }
    }

    /** Uses a zoom factor of [value]. */
    public fun static(value: Float = 1f): Zoom = Zoom { _, _, _ -> value }

    /** Ensures the specified number of _x_ units is visible. */
    public fun x(x: Double): Zoom = Zoom { context, horizontalDimensions, bounds ->
      bounds.width() * (context.ranges.xStep / x).toFloat() / horizontalDimensions.xSpacing
    }

    /** Uses the smaller of [a]’s zoom factor and [b]’s zoom factor. */
    public fun min(a: Zoom, b: Zoom): Zoom = Zoom { context, horizontalDimensions, bounds ->
      min(
        a.getValue(context, horizontalDimensions, bounds),
        b.getValue(context, horizontalDimensions, bounds),
      )
    }

    /** Uses the greater of [a]’s zoom factor and [b]’s zoom factor. */
    public fun max(a: Zoom, b: Zoom): Zoom = Zoom { context, horizontalDimensions, bounds ->
      max(
        a.getValue(context, horizontalDimensions, bounds),
        b.getValue(context, horizontalDimensions, bounds),
      )
    }
  }
}
