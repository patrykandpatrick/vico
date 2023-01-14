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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.databinding.Chart1Binding
import com.patrykandpatrick.vico.sample.util.rememberChartStyle
import com.patrykandpatrick.vico.sample.util.rememberMarker

@Composable
internal fun ComposeChart1(chartEntryModelProducer: ChartEntryModelProducer, modifier: Modifier = Modifier) {
    val marker = rememberMarker()
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        Chart(
            chart = lineChart(persistentMarkers = remember(marker) { mapOf(PERSISTENT_MARKER_X to marker) }),
            chartModelProducer = chartEntryModelProducer,
            modifier = modifier,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(guideline = null),
            marker = marker,
        )
    }
}

@Composable
internal fun ViewChart1(chartEntryModelProducer: ChartEntryModelProducer, modifier: Modifier = Modifier) {
    val marker = rememberMarker()
    AndroidViewBinding(Chart1Binding::inflate, modifier) {
        with(chartView) {
            chart?.addPersistentMarker(PERSISTENT_MARKER_X, marker)
            entryProducer = chartEntryModelProducer
            (bottomAxis as Axis).guideline = null
            this.marker = marker
        }
    }
}

private const val COLOR_1_CODE = 0xffa485e0
private const val PERSISTENT_MARKER_X = 10f

private val color1 = Color(COLOR_1_CODE)
private val chartColors = listOf(color1)
