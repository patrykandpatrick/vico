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

package com.patrykandpatrick.vico.multiplatform.cartesian.data

import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.common.format

/** Formats values for display. */
public fun interface CartesianValueFormatter {
  /**
   * Formats [value]. [verticalAxisPosition] is the position of the [VerticalAxis] with which the
   * caller is associated.
   */
  public fun format(
    context: CartesianMeasuringContext,
    value: Double,
    verticalAxisPosition: Axis.Position.Vertical?,
  ): CharSequence

  /** Houses [CartesianValueFormatter] factory functions. */
  public companion object {
    private class Decimal(
      private val decimalCount: Int,
      private val decimalSeparator: String,
      private val thousandsSeparator: String,
      private val prefix: String,
      private val suffix: String,
    ) : CartesianValueFormatter {
      override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
      ): CharSequence =
        value.format(decimalCount, decimalSeparator, thousandsSeparator, prefix, suffix)

      override fun equals(other: Any?) =
        this === other || other is Decimal && decimalCount == other.decimalCount

      override fun hashCode() = decimalCount.hashCode()
    }

    private class YPercent(private val decimalCount: Int) : CartesianValueFormatter {
      override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
      ): CharSequence =
        (value / context.ranges.getYRange(verticalAxisPosition).maxY).format(
          decimalCount = decimalCount,
          suffix = "%",
        )

      override fun equals(other: Any?) =
        this === other || other is YPercent && decimalCount == other.decimalCount

      override fun hashCode() = decimalCount.hashCode()
    }

    /**
     * Formats values to include up to [decimalCount] decimal digits. Trailing zeros are skipped.
     */
    public fun decimal(
      decimalCount: Int = 2,
      decimalSeparator: String = ".",
      thousandsSeparator: String = "",
      prefix: String = "",
      suffix: String = "",
    ): CartesianValueFormatter =
      Decimal(decimalCount, decimalSeparator, thousandsSeparator, prefix, suffix)

    /**
     * Divides values by [CartesianChartRanges.YRange.maxY] and formats the resulting quotients to
     * include up to [decimalCount] decimal digits. Trailing zeros are skipped.
     */
    public fun yPercent(decimalCount: Int = 2): CartesianValueFormatter = YPercent(decimalCount)
  }
}

internal fun CartesianValueFormatter.formatForAxis(
  context: CartesianMeasuringContext,
  value: Double,
  verticalAxisPosition: Axis.Position.Vertical?,
): CharSequence =
  format(context, value, verticalAxisPosition).also {
    check(it.isNotEmpty()) {
      "`CartesianValueFormatter.format` returned an empty string. Use " +
        "`HorizontalAxis.ItemPlacer` and `VerticalAxis.ItemPlacer`, not empty strings, to " +
        "control which x and y values are labeled."
    }
  }
