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
import java.math.RoundingMode
import java.text.DecimalFormat
import pl.patrykgoworowski.vico.core.axis.AxisPosition

/**
 * A subclass of [AxisValueFormatter] that formats values using a [DecimalFormat] with a given pattern.
 */
public class DecimalFormatAxisValueFormatter<Position : AxisPosition>(
    private val decimalFormat: DecimalFormat,
) : AxisValueFormatter<Position> {

    /**
     * Creates a [DecimalFormatAxisValueFormatter] using the default pattern.
     */
    public constructor() : this(DEF_FORMAT)

    /**
     * Creates a [DecimalFormatAxisValueFormatter] that will format values based on the given [pattern] and
     * [roundingMode].
     */
    public constructor(
        pattern: String,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
    ) : this(getDecimalFormat(pattern, roundingMode))

    override fun formatValue(
        value: Float,
        chartModel: ChartModel,
    ): String = decimalFormat.format(value)

    private companion object {
        private const val DEF_FORMAT = "#.##"

        private fun getDecimalFormat(
            pattern: String,
            roundingMode: RoundingMode,
        ): DecimalFormat =
            DecimalFormat(pattern).apply {
                this.roundingMode = roundingMode
            }
    }
}
