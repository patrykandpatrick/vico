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

package com.patrykandpatrick.vico.sample.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.databinding.Chart5Binding
import com.patrykandpatrick.vico.sample.util.rememberChartStyle
import com.patrykandpatrick.vico.sample.util.rememberMarker

@Composable
internal fun ComposeChart5(chartEntryModelProducer: ChartEntryModelProducer, modifier: Modifier = Modifier) {
    ProvideChartStyle(rememberChartStyle(entityColors)) {
        Chart(
            chart = columnChart(mergeMode = ColumnChart.MergeMode.Stack),
            chartModelProducer = chartEntryModelProducer,
            modifier = modifier,
            startAxis = startAxis(
                maxLabelCount = START_AXIS_LABEL_COUNT,
                labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES,
            ),
            bottomAxis = bottomAxis(labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES),
            marker = rememberMarker(),
        )
    }
}

@Composable
internal fun ViewChart5(chartEntryModelProducer: ChartEntryModelProducer, modifier: Modifier = Modifier) {
    val marker = rememberMarker()
    AndroidViewBinding(Chart5Binding::inflate, modifier) {
        (chartView.chart as ColumnChart).mergeMode = ColumnChart.MergeMode.Stack
        chartView.entryProducer = chartEntryModelProducer
        (chartView.startAxis as VerticalAxis).maxLabelCount = START_AXIS_LABEL_COUNT
        chartView.marker = marker
    }
}

private const val COLOR_1_CODE = 0xff6438a7
private const val COLOR_2_CODE = 0xff3490de
private const val COLOR_3_CODE = 0xff73e8dc
private const val START_AXIS_LABEL_COUNT = 2
private const val AXIS_LABEL_ROTATION_DEGREES = 45f

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val color3 = Color(COLOR_3_CODE)
private val entityColors = listOf(color1, color2, color3)
