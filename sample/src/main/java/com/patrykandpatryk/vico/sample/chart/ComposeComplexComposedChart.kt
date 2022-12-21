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
import com.patrykandpatryk.vico.compose.axis.vertical.endAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.style.ChartStyle
import com.patrykandpatryk.vico.compose.style.ProvideChartStyle
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.chart.column.ColumnChart
import com.patrykandpatryk.vico.core.chart.composed.ComposedChart
import com.patrykandpatryk.vico.core.chart.composed.plus
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatryk.vico.databinding.ComplexComposedChartBinding
import com.patrykandpatryk.vico.sample.extension.fromEntityColors
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ComposeComplexComposedChart(
    composedChartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>,
    modifier: Modifier = Modifier,
) {
    val chartStyle = ChartStyle.fromEntityColors(entityColors = entityColors)
    val startAxis = startAxis(guideline = null)
    ProvideChartStyle(chartStyle = chartStyle) {
        val lineChart = lineChart(targetVerticalAxisPosition = AxisPosition.Vertical.End)
        val columnChart = columnChart(
            targetVerticalAxisPosition = AxisPosition.Vertical.Start,
            mergeMode = ColumnChart.MergeMode.Stack,
        )
        Chart(
            chart = columnChart + lineChart,
            chartModelProducer = composedChartEntryModelProducer,
            startAxis = startAxis,
            endAxis = endAxis(),
            modifier = modifier,
            marker = marker(),
        )
    }
}

@Composable
internal fun ViewComplexComposedChart(
    composedChartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    AndroidViewBinding(
        factory = ComplexComposedChartBinding::inflate,
        modifier = modifier,
    ) {
        chartView.entryProducer = composedChartEntryModelProducer
        chartView.marker = marker
        (chartView.startAxis as Axis).guideline = null
        with(chartView.chart as ComposedChart) {
            with(charts[0] as ColumnChart) {
                mergeMode = ColumnChart.MergeMode.Stack
                targetVerticalAxisPosition = AxisPosition.Vertical.Start
            }
            (charts[1] as LineChart).targetVerticalAxisPosition = AxisPosition.Vertical.End
        }
    }
}

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFF68A7AD, 0xFF99C4C8, 0xFFE5CB9F)
