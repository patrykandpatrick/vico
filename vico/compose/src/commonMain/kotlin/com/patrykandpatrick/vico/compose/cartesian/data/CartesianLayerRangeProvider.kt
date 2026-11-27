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

package com.patrykandpatrick.vico.compose.cartesian.data

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import kotlin.math.*

/**
 * Defines a [CartesianLayer]’s _x_ and _y_ ranges. Each function receives the intrinsic range of
 * the [CartesianLayer]’s data. Implementations must be pure functions of their arguments and of the
 * values they capture, and they may be called off the main thread; to change the ranges, pass a new
 * [CartesianLayerRangeProvider].
 *
 * Define a custom implementation at the top level or [remember][androidx.compose.runtime.remember]
 * it. Because this interface has several functions, an implementation is an object, and an object
 * created inline in a composable function is a new instance on every recomposition—unlike a lambda
 * converted to a single-method interface, which Compose memoizes—so the chart re-derives its data
 * each time. For bounds that depend on data, use [fixed], whose instances are equal when their
 * bounds are.
 *
 * An empty [CartesianLayerModel] has no extents, so its [CartesianLayerRangeProvider] receives the
 * ranges already established by the chart’s other layers (non-empty ones first, then empty ones in
 * layer order)—the _x_-range and the _y_-range for its vertical axis—or zeros where there are none.
 * The ranges are thus as if the layer held a point within the existing span: [auto] next to an
 * [auto] layer changes nothing, `fixed(maxY = 100.0)` extends the _y_-range to 100, and, in a chart
 * whose layers are all empty, [auto] yields an _x_-range of 0 to 0 and a _y_-range of 0 to 1.
 *
 * The default _x_-step is the greatest common divisor of the non-empty layers’ _x_-value
 * differences, or 1 if there are no entries.
 */
@Immutable
public interface CartesianLayerRangeProvider {
  /** Returns the minimum _x_ value. */
  public fun getMinX(minX: Double, maxX: Double): Double = minX

  /** Returns the maximum _x_ value. */
  public fun getMaxX(minX: Double, maxX: Double): Double = maxX

  /** Returns the minimum _y_ value. */
  public fun getMinY(minY: Double, maxY: Double): Double = minY.coerceAtMost(0.0)

  /** Returns the maximum _y_ value. */
  public fun getMaxY(minY: Double, maxY: Double): Double =
    if (minY == 0.0 && maxY == 0.0) 1.0 else maxY.coerceAtLeast(0.0)

  public companion object {
    private object Auto : CartesianLayerRangeProvider {
      override fun getMinY(minY: Double, maxY: Double) =
        if (minY == 0.0 && maxY == 0.0 || minY >= 0.0) 0.0 else minY.round(maxY)

      override fun getMaxY(minY: Double, maxY: Double) =
        when {
          minY == 0.0 && maxY == 0.0 -> 1.0
          maxY <= 0.0 -> 0.0
          else -> maxY.round(minY)
        }

      private fun Double.round(other: Double): Double {
        val absoluteValue = abs(this)
        val base = 10.0.pow(floor(log10(max(absoluteValue, abs(other)))) - 1)
        return sign * ceil(absoluteValue / base) * base
      }
    }

    private data class Fixed(
      private val minX: Double? = null,
      private val maxX: Double? = null,
      private val minY: Double? = null,
      private val maxY: Double? = null,
    ) : CartesianLayerRangeProvider {
      override fun getMinX(minX: Double, maxX: Double) = this.minX ?: super.getMinX(minX, maxX)

      override fun getMaxX(minX: Double, maxX: Double) = this.maxX ?: super.getMaxX(minX, maxX)

      override fun getMinY(minY: Double, maxY: Double) = this.minY ?: super.getMinY(minY, maxY)

      override fun getMaxY(minY: Double, maxY: Double) = this.maxY ?: super.getMaxY(minY, maxY)
    }

    /** Keeps the intrinsic ranges. */
    public val Intrinsic: CartesianLayerRangeProvider =
      object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double) = minY

        override fun getMaxY(minY: Double, maxY: Double) = maxY
      }

    /** Uses dynamic rounding. */
    public fun auto(): CartesianLayerRangeProvider = Auto

    /** Overrides the defaults with the provided values. */
    public fun fixed(
      minX: Double? = null,
      maxX: Double? = null,
      minY: Double? = null,
      maxY: Double? = null,
    ): CartesianLayerRangeProvider = Fixed(minX, maxX, minY, maxY)
  }
}
