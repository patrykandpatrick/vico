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

package com.patrykandpatrick.vico.core.marker

import android.text.Spannable
import android.text.style.ForegroundColorSpan
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.extension.appendCompat
import com.patrykandpatrick.vico.core.extension.sumOf
import com.patrykandpatrick.vico.core.extension.transformToSpannable
import com.patrykandpatrick.vico.core.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.model.LineCartesianLayerModel
import java.text.DecimalFormat

/**
 * The default [MarkerLabelFormatter]. The _y_ values are formatted via [decimalFormat] and, if [colorCode] is `true`,
 * color-coded.
 */
public open class DefaultMarkerLabelFormatter(
    private val decimalFormat: DecimalFormat = defaultDecimalFormat,
    private val colorCode: Boolean = true,
) : MarkerLabelFormatter {
    /** The default [MarkerLabelFormatter]. [colorCode] specifies whether to color-code the _y_ values. */
    @Deprecated(
        "Use the primary constructor, which has a `decimalFormat` parameter. (If you’re using named arguments, " +
            "ignore this warning. The deprecated constructor is more specific, but the primary one matches and will " +
            "be used once the deprecated one has been removed.)",
    )
    public constructor(colorCode: Boolean) : this(defaultDecimalFormat, colorCode)

    override fun getLabel(
        markedEntries: List<Marker.EntryModel>,
        chartValues: ChartValues,
    ): CharSequence =
        markedEntries.transformToSpannable(
            prefix =
                if (markedEntries.size > 1) decimalFormat.format(markedEntries.sumOf { it.entry.y }) + " (" else "",
            postfix = if (markedEntries.size > 1) ")" else "",
            separator = "; ",
        ) { model ->
            if (colorCode) {
                appendCompat(
                    decimalFormat.format(model.entry.y),
                    ForegroundColorSpan(model.color),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            } else {
                append(decimalFormat.format(model.entry.y))
            }
        }

    protected val CartesianLayerModel.Entry.y: Float
        get() =
            when (this) {
                is ColumnCartesianLayerModel.Entry -> y
                is LineCartesianLayerModel.Entry -> y
                else -> throw IllegalArgumentException("Unexpected `CartesianLayerModel.Entry` implementation.")
            }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is DefaultMarkerLabelFormatter && decimalFormat == other.decimalFormat && colorCode == other.colorCode

    override fun hashCode(): Int = 31 * decimalFormat.hashCode() + colorCode.hashCode()

    private companion object {
        val defaultDecimalFormat = DecimalFormat("#.##;−#.##")
    }
}
