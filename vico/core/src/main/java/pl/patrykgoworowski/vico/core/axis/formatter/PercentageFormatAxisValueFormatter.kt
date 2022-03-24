/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package pl.patrykgoworowski.vico.core.axis.formatter

import pl.patrykgoworowski.vico.core.axis.model.ChartModel
import java.text.DecimalFormat
import pl.patrykgoworowski.vico.core.axis.AxisPosition

/**
 * A subclass of [AxisValueFormatter] that converts y-axis values to percentages.
 * It uses [DecimalFormat] to format values.
 *
 * @param pattern the pattern used by [DecimalFormat] to format values as percentages.
 */
public class PercentageFormatAxisValueFormatter<Position : AxisPosition.Vertical>(
    pattern: String,
) : AxisValueFormatter<Position> {

    private val decimalFormat = DecimalFormat(pattern)

    /**
     * Creates a [PercentageFormatAxisValueFormatter] using the default percentage pattern.
     */
    public constructor() : this(DEF_PATTERN)

    override fun formatValue(
        value: Float,
        chartModel: ChartModel,
    ): String {
        val percentage = value / chartModel.maxY
        return decimalFormat.format(percentage)
    }

    private companion object {
        private const val DEF_PATTERN = "#.##%"
    }
}
