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

import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import java.text.DecimalFormat

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
    private class Decimal(private val decimalFormat: DecimalFormat) : CartesianValueFormatter {
      override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
      ): CharSequence = decimalFormat.format(value)

      override fun equals(other: Any?) =
        this === other || other is Decimal && decimalFormat == other.decimalFormat

      override fun hashCode() = decimalFormat.hashCode()
    }

    private class YPercent(private val decimalFormat: DecimalFormat) : CartesianValueFormatter {
      override fun format(
        context: CartesianMeasuringContext,
        value: Double,
        verticalAxisPosition: Axis.Position.Vertical?,
      ): CharSequence =
        decimalFormat.format(value / context.ranges.getYRange(verticalAxisPosition).maxY)

      override fun equals(other: Any?) =
        this === other || other is YPercent && decimalFormat == other.decimalFormat

      override fun hashCode() = decimalFormat.hashCode()
    }

    /** Formats values via [decimalFormat]. */
    public fun decimal(
      decimalFormat: DecimalFormat = DecimalFormat("#.##;−#.##")
    ): CartesianValueFormatter = Decimal(decimalFormat)

    /**
     * Divides values by [CartesianChartRanges.YRange.maxY] and formats the resulting quotients via
     * [decimalFormat].
     */
    public fun yPercent(
      decimalFormat: DecimalFormat = DecimalFormat("#.##%;−#.##%")
    ): CartesianValueFormatter = YPercent(decimalFormat)
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
