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

import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import java.text.DecimalFormat

/** Formats values for display. */
public fun interface CartesianValueFormatter {
  /**
   * Formats [value]. [chartValues] houses the [CartesianChart]’s [CartesianChartModel] and _x_ and
   * _y_ ranges. [verticalAxisPosition] is the position of the [VerticalAxis] with which the caller
   * is associated. Pass this to [ChartValues.getYRange].
   */
  public fun format(
    value: Double,
    chartValues: ChartValues,
    verticalAxisPosition: Axis.Position.Vertical?,
  ): CharSequence

  /** Houses [CartesianValueFormatter] factory functions. */
  public companion object {
    private class Decimal(private val decimalFormat: DecimalFormat) : CartesianValueFormatter {
      override fun format(
        value: Double,
        chartValues: ChartValues,
        verticalAxisPosition: Axis.Position.Vertical?,
      ): CharSequence = decimalFormat.format(value)

      override fun equals(other: Any?) =
        this === other || other is Decimal && decimalFormat == other.decimalFormat

      override fun hashCode() = decimalFormat.hashCode()
    }

    private class YPercent(private val decimalFormat: DecimalFormat) : CartesianValueFormatter {
      override fun format(
        value: Double,
        chartValues: ChartValues,
        verticalAxisPosition: Axis.Position.Vertical?,
      ): CharSequence =
        decimalFormat.format(value / chartValues.getYRange(verticalAxisPosition).maxY)

      override fun equals(other: Any?) =
        this === other || other is YPercent && decimalFormat == other.decimalFormat

      override fun hashCode() = decimalFormat.hashCode()
    }

    /** Formats values via [decimalFormat]. */
    public fun decimal(
      decimalFormat: DecimalFormat = DecimalFormat("#.##;−#.##")
    ): CartesianValueFormatter = Decimal(decimalFormat)

    /**
     * Divides values by [ChartValues.YRange.maxY] and formats the resulting quotients via
     * [decimalFormat].
     */
    public fun yPercent(
      decimalFormat: DecimalFormat = DecimalFormat("#.##%;−#.##%")
    ): CartesianValueFormatter = YPercent(decimalFormat)
  }
}
