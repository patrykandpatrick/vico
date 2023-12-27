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

package com.patrykandpatrick.vico.core.marker

import android.text.Spannable
import android.text.style.ForegroundColorSpan
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.extension.appendCompat
import com.patrykandpatrick.vico.core.extension.sumOf
import com.patrykandpatrick.vico.core.extension.transformToSpannable
import com.patrykandpatrick.vico.core.model.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.core.model.CartesianLayerModel
import com.patrykandpatrick.vico.core.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.model.LineCartesianLayerModel

/**
 * The default label formatter used for markers. [colorCode] specifies whether to color-code the _y_ values in the
 * labels.
 *
 * @see MarkerLabelFormatter
 */
public class DefaultMarkerLabelFormatter(private val colorCode: Boolean = true) : MarkerLabelFormatter {
    override fun getLabel(
        markedEntries: List<Marker.EntryModel>,
        chartValues: ChartValues,
    ): CharSequence =
        markedEntries.transformToSpannable(
            prefix = if (markedEntries.size > 1) PATTERN.format(markedEntries.sumOf { it.entry.y }) + " (" else "",
            postfix = if (markedEntries.size > 1) ")" else "",
            separator = "; ",
        ) { model ->
            if (colorCode) {
                appendCompat(
                    PATTERN.format(model.entry.y),
                    ForegroundColorSpan(model.color),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            } else {
                append(PATTERN.format(model.entry.y))
            }
        }

    private val CartesianLayerModel.Entry.y
        get() =
            when (this) {
                is ColumnCartesianLayerModel.Entry -> y
                is LineCartesianLayerModel.Entry -> y
                is CandlestickCartesianLayerModel.Entry -> high
                else -> throw IllegalArgumentException("Unexpected `CartesianLayerModel.Entry` implementation.")
            }

    private companion object {
        const val PATTERN = "%.02f"
    }
}
