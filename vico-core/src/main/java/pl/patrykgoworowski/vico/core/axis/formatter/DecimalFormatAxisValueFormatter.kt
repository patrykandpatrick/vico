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

import pl.patrykgoworowski.vico.core.axis.model.DataSetModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import java.math.RoundingMode
import java.text.DecimalFormat

class DecimalFormatAxisValueFormatter(
    private val decimalFormat: DecimalFormat
) : AxisValueFormatter {

    constructor() : this(DEF_FORMAT)

    constructor(
        pattern: String,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
    ) : this(getDecimalFormat(pattern, roundingMode))

    override fun formatValue(
        value: Float,
        index: Int,
        model: EntryModel,
        dataSetModel: DataSetModel
    ): String = decimalFormat.format(value)

    companion object {
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
