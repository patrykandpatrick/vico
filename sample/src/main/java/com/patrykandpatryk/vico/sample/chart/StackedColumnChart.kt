/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatryk.vico.sample.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.style.ChartStyle
import com.patrykandpatryk.vico.compose.style.ProvideChartStyle
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.chart.column.ColumnChart
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.databinding.StackedColumnChartBinding
import com.patrykandpatryk.vico.sample.extension.fromEntityColors
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ComposeStackedColumnChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val chartStyle = ChartStyle.fromEntityColors(entityColors = entityColors)
    val startAxis = startAxis(maxLabelCount = MAX_LABEL_COUNT, labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES)
    val bottomAxis = bottomAxis(labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES)
    ProvideChartStyle(chartStyle = chartStyle) {
        val columnChart = columnChart(mergeMode = ColumnChart.MergeMode.Stack)
        Chart(
            chart = columnChart,
            chartModelProducer = chartEntryModelProducer,
            modifier = modifier,
            startAxis = startAxis,
            bottomAxis = bottomAxis,
            marker = marker(),
        )
    }
}

@Composable
internal fun ViewStackedColumnChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    AndroidViewBinding(
        factory = StackedColumnChartBinding::inflate,
        modifier = modifier,
    ) {
        chartView.entryProducer = chartEntryModelProducer
        chartView.marker = marker
        (chartView.chart as ColumnChart).mergeMode = ColumnChart.MergeMode.Stack
        (chartView.startAxis as VerticalAxis).maxLabelCount = MAX_LABEL_COUNT
    }
}

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFF6639A6, 0xFF3490DE, 0xFF6FE7DD)
private const val AXIS_LABEL_ROTATION_DEGREES = 45f
private const val MAX_LABEL_COUNT = 2
