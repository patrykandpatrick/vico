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
import com.patrykandpatryk.vico.compose.axis.horizontal.topAxis
import com.patrykandpatryk.vico.compose.axis.vertical.endAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.style.ChartStyle
import com.patrykandpatryk.vico.compose.style.ProvideChartStyle
import com.patrykandpatryk.vico.core.chart.composed.plus
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatryk.vico.databinding.ComposedChartBinding
import com.patrykandpatryk.vico.sample.extension.fromEntityColors
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ComposeComposedChart(
    composedChartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>,
    modifier: Modifier = Modifier,
) {
    val chartStyle = ChartStyle.fromEntityColors(entityColors = entityColors)
    ProvideChartStyle(chartStyle = chartStyle) {
        Chart(
            chart = columnChart() + lineChart(),
            chartModelProducer = composedChartEntryModelProducer,
            topAxis = topAxis(),
            endAxis = endAxis(),
            modifier = modifier,
            marker = marker(),
        )
    }
}

@Composable
internal fun ViewComposedChart(
    composedChartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    AndroidViewBinding(
        factory = ComposedChartBinding::inflate,
        modifier = modifier,
    ) {
        chartView.entryProducer = composedChartEntryModelProducer
        chartView.marker = marker
    }
}

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFF3D84A8, 0xFF46CDCF, 0xFFABEDD8)
