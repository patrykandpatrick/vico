/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.formatter

import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import java.text.DecimalFormat

/**
 * A [ValueFormatter] implementation that converts y-axis values to percentages.
 * It uses [DecimalFormat] to format values.
 *
 * @param pattern the pattern used by [DecimalFormat] to format values as percentages.
 */
public open class PercentageFormatValueFormatter(pattern: String) : ValueFormatter {
    private val decimalFormat = DecimalFormat(pattern)

    /**
     * Creates a [PercentageFormatValueFormatter] using the default percentage pattern.
     */
    public constructor() : this(DEF_PATTERN)

    override fun formatValue(
        value: Float,
        chartValues: ChartValues,
        verticalAxisPosition: AxisPosition.Vertical?,
    ): String {
        val percentage = value / chartValues.getYRange(verticalAxisPosition).maxY
        return decimalFormat.format(percentage)
    }

    public companion object {
        /**
         * The default percentage pattern.
         */
        public const val DEF_PATTERN: String = "#.##%;−#.##%"
    }
}
