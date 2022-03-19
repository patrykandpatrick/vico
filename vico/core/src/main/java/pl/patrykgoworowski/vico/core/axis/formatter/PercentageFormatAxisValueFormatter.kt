/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
 * A subclass of [AxisValueFormatter] which converts y-axis values into percents.
 * It uses [DecimalFormat] to format values under the hood.
 *
 * @param pattern The pattern used by [DecimalFormat] to format percent values.
 */
public class PercentageFormatAxisValueFormatter<Position : AxisPosition.Vertical>(
    pattern: String,
) : AxisValueFormatter<Position> {

    private val decimalFormat = DecimalFormat(pattern)

    /**
     * Creates a [PercentageFormatAxisValueFormatter] using default percentage pattern.
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
