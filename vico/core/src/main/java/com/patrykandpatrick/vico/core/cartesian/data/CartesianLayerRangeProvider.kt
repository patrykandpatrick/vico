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

package com.patrykandpatrick.vico.core.cartesian.data

import androidx.compose.runtime.Immutable
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sign

/** Defines a [CartesianLayer]’s _x_ and _y_ ranges. */
@Immutable
public interface CartesianLayerRangeProvider {
  /** Returns the minimum _x_ value. */
  public fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = minX

  /** Returns the maximum _x_ value. */
  public fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore): Double = maxX

  /** Returns the minimum _y_ value. */
  public fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
    minY.coerceAtMost(0.0)

  /** Returns the maximum _y_ value. */
  public fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double =
    if (minY == 0.0 && maxY == 0.0) 1.0 else maxY.coerceAtLeast(0.0)

  public companion object {
    private object Auto : CartesianLayerRangeProvider {
      override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        if (minY == 0.0 && maxY == 0.0 || minY >= 0.0) 0.0 else minY.round(maxY)

      override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
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
      override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore) =
        this.minX ?: super.getMinX(minX, maxX, extraStore)

      override fun getMaxX(minX: Double, maxX: Double, extraStore: ExtraStore) =
        this.maxX ?: super.getMaxX(minX, maxX, extraStore)

      override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        this.minY ?: super.getMinY(minY, maxY, extraStore)

      override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        this.maxY ?: super.getMaxY(minY, maxY, extraStore)
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
