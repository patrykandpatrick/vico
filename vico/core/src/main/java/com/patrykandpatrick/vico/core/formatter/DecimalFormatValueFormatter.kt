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

import com.patrykandpatrick.vico.core.chart.values.ChartValues
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * A [ValueFormatter] implementation that formats values using a [DecimalFormat] with a given pattern.
 */
public open class DecimalFormatValueFormatter(private val decimalFormat: DecimalFormat) : ValueFormatter {
    /**
     * Creates a [DecimalFormatValueFormatter] using the default pattern.
     */
    public constructor() : this(DEF_FORMAT)

    /**
     * Creates a [DecimalFormatValueFormatter] that will format values based on the given [pattern] and [roundingMode].
     */
    public constructor(
        pattern: String,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
    ) : this(getDecimalFormat(pattern, roundingMode))

    override fun formatValue(
        value: Float,
        chartValues: ChartValues,
    ): String = decimalFormat.format(value)

    public companion object {
        /**
         * The default pattern for the [DecimalFormat].
         */
        public const val DEF_FORMAT: String = "#.##;−#.##"

        /**
         * Creates a [DecimalFormat] using the provided pattern and rounding mode.
         */
        public fun getDecimalFormat(
            pattern: String,
            roundingMode: RoundingMode,
        ): DecimalFormat =
            DecimalFormat(pattern).apply {
                this.roundingMode = roundingMode
            }
    }
}
