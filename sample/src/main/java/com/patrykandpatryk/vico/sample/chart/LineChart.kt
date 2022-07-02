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

package com.patrykandpatryk.vico.sample.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.style.ChartStyle
import com.patrykandpatryk.vico.compose.style.ProvideChartStyle
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.databinding.LineChartBinding
import com.patrykandpatryk.vico.sample.extension.fromEntityColors
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ComposeLineChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val startAxis = startAxis()
    val bottomAxis = bottomAxis(guideline = null)
    val chartStyle = ChartStyle.fromEntityColors(entityColors = entityColors)
    ProvideChartStyle(chartStyle = chartStyle) {
        val lineChart = lineChart(persistentMarkers = mapOf(PERSISTENT_MARKER_X to marker()))
        Chart(
            modifier = modifier,
            chart = lineChart,
            chartModelProducer = chartEntryModelProducer,
            startAxis = startAxis,
            bottomAxis = bottomAxis,
            marker = marker(),
            chartScale = 3f
        )
    }
}

@Composable
internal fun ViewLineChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    AndroidViewBinding(
        factory = LineChartBinding::inflate,
        modifier = modifier,
    ) {
        chartView.entryProducer = chartEntryModelProducer
        chartView.marker = marker
        (chartView.bottomAxis as Axis).guideline = null
        chartView.chart?.addPersistentMarker(
            x = PERSISTENT_MARKER_X,
            marker = marker,
        )
    }
}

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFFAA96DA)
private const val PERSISTENT_MARKER_X = 6f
