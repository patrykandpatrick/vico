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

package com.patrykandpatrick.vico.sample.previews.composables.composed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.sample.previews.annotation.ChartPreview
import com.patrykandpatrick.vico.sample.previews.resource.PreviewSurface
import com.patrykandpatrick.vico.sample.previews.resource.shortEntryModel

@Composable
@ChartPreview
public fun ComposableChartWithDecorations() {
    val lineChartDecorations = listOf(
        ThresholdLine(
            thresholdRange = 1f..2f,
            lineComponent = shapeComponent(color = Color(0x8A6666AA)),
        ),
    )
    val columnChartDecorations = listOf(
        ThresholdLine(
            thresholdRange = 4f..5f,
            lineComponent = shapeComponent(color = Color(0x8A66AA66)),
        ),
    )
    val composedChartDecorations = listOf(
        ThresholdLine(
            thresholdRange = 7f..8f,
            lineComponent = shapeComponent(color = Color(0x8AAA6666)),
        ),
    )

    PreviewSurface {
        val lineChart = lineChart(decorations = lineChartDecorations)
        val columnChart = columnChart(decorations = columnChartDecorations)
        val composedChart = remember {
            (lineChart + columnChart).apply {
                setDecorations(composedChartDecorations)
            }
        }
        Chart(
            chart = composedChart,
            model = remember { shortEntryModel + shortEntryModel },
        )
    }
}
