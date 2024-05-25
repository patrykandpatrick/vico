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

import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.ceil
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.floor
import com.patrykandpatrick.vico.core.common.round
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sign

/** Overrides a [CartesianLayer]’s _x_ and _y_ ranges. */
public interface AxisValueOverrider {
  /** Returns the minimum _x_ value. */
  public fun getMinX(minX: Float, maxX: Float, extraStore: ExtraStore): Float = minX

  /** Returns the maximum _x_ value. */
  public fun getMaxX(minX: Float, maxX: Float, extraStore: ExtraStore): Float = maxX

  /** Returns the minimum _y_ value. */
  public fun getMinY(minY: Float, maxY: Float, extraStore: ExtraStore): Float =
    minY.coerceAtMost(0f)

  /** Returns the maximum _y_ value. */
  public fun getMaxY(minY: Float, maxY: Float, extraStore: ExtraStore): Float =
    if (minY == 0f && maxY == 0f) 1f else maxY.coerceAtLeast(0f)

  public companion object {
    /** Uses dynamic rounding. */
    public fun auto(): AxisValueOverrider =
      object : AxisValueOverrider {
        override fun getMinY(minY: Float, maxY: Float, extraStore: ExtraStore) =
          if (minY == 0f && maxY == 0f || minY >= 0f) 0f else minY.round(maxY)

        override fun getMaxY(minY: Float, maxY: Float, extraStore: ExtraStore) =
          when {
            minY == 0f && maxY == 0f -> 1f
            maxY <= 0f -> 0f
            else -> maxY.round(minY)
          }

        private fun Float.round(other: Float): Float {
          val absoluteValue = abs(this)
          val base = 10f.pow(log10(max(absoluteValue, abs(other))).floor - 1)
          return sign * (absoluteValue / base).ceil * base
        }
      }

    /** Overrides the defaults with the provided values. */
    public fun fixed(
      minX: Float? = null,
      maxX: Float? = null,
      minY: Float? = null,
      maxY: Float? = null,
    ): AxisValueOverrider {
      val newMinX = minX
      val newMaxX = maxX
      val newMinY = minY
      val newMaxY = maxY
      return object : AxisValueOverrider {
        override fun getMinX(minX: Float, maxX: Float, extraStore: ExtraStore) =
          newMinX ?: super.getMinX(minX, maxX, extraStore)

        override fun getMaxX(minX: Float, maxX: Float, extraStore: ExtraStore) =
          newMaxX ?: super.getMaxX(minX, maxX, extraStore)

        override fun getMinY(minY: Float, maxY: Float, extraStore: ExtraStore) =
          newMinY ?: super.getMinY(minY, maxY, extraStore)

        override fun getMaxY(minY: Float, maxY: Float, extraStore: ExtraStore) =
          newMaxY ?: super.getMaxY(getMinY(minY, maxY, extraStore), maxY, extraStore)
      }
    }

    /**
     * Sets the maximum _y_ value to [yFraction] times the default. Sets the minimum _y_ value to
     * the default minus the difference between the new maximum _y_ value and the default maximum
     * _y_ value.
     */
    public fun adaptiveYValues(yFraction: Float, round: Boolean = false): AxisValueOverrider =
      object : AxisValueOverrider {
        init {
          require(yFraction > 0f)
        }

        override fun getMinY(minY: Float, maxY: Float, extraStore: ExtraStore): Float {
          val difference = abs(getMaxY(minY, maxY, extraStore) - maxY)
          return (minY - difference).maybeRound().coerceAtLeast(0f)
        }

        override fun getMaxY(minY: Float, maxY: Float, extraStore: ExtraStore): Float =
          if (minY == 0f && maxY == 0f) 1f else (yFraction * maxY).maybeRound()

        private fun Float.maybeRound() = if (round) this.round else this
      }
  }
}
