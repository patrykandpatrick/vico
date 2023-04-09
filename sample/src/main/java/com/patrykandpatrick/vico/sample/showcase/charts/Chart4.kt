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

package com.patrykandpatrick.vico.sample.showcase.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.topAxis
import com.patrykandpatrick.vico.compose.axis.vertical.endAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.copy
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.databinding.Chart4Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart4(uiSystem: UISystem, chartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart4(chartEntryModelProducer)
        UISystem.Views -> ViewChart4(chartEntryModelProducer)
    }
}

@Composable
private fun ComposeChart4(chartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>) {
    ProvideChartStyle(rememberChartStyle(columnChartColors, lineChartColors)) {
        val defaultColumns = currentChartStyle.columnChart.columns
        val defaultLines = currentChartStyle.lineChart.lines
        val columnChart = columnChart(
            remember(defaultColumns) {
                defaultColumns.map { defaultColumn ->
                    LineComponent(
                        defaultColumn.color,
                        defaultColumn.thicknessDp,
                        Shapes.roundedCornerShape(columnCornerRadius),
                    )
                }
            },
        )
        val lineChart = lineChart(
            remember(defaultLines) {
                defaultLines.map { defaultLine -> defaultLine.copy(pointConnector = pointConnector) }
            },
        )
        Chart(
            chart = remember(columnChart, lineChart) { columnChart + lineChart },
            chartModelProducer = chartEntryModelProducer,
            topAxis = topAxis(),
            endAxis = endAxis(),
            marker = rememberMarker(),
        )
    }
}

@Composable
private fun ViewChart4(chartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>) {
    val marker = rememberMarker()
    AndroidViewBinding(Chart4Binding::inflate) {
        chartView.entryProducer = chartEntryModelProducer
        chartView.marker = marker
    }
}

private const val COLOR_1_CODE = 0xff916cda
private const val COLOR_2_CODE = 0xffd877d8
private const val COLOR_3_CODE = 0xfff094bb
private const val COLOR_4_CODE = 0xfffdc8c4

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val color3 = Color(COLOR_3_CODE)
private val color4 = Color(COLOR_4_CODE)
private val columnChartColors = listOf(color1, color2, color3)
private val lineChartColors = listOf(color4)
private val columnCornerRadius = 2.dp
private val pointConnector = DefaultPointConnector(cubicStrength = 0f)
