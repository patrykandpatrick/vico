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
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.candlestick.hollow
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryModelProducer
import com.patrykandpatrick.vico.core.chart.candlestick.CandlestickChart
import com.patrykandpatrick.vico.databinding.Chart9Binding
import com.patrykandpatrick.vico.sample.util.rememberMarker

@Composable
internal fun ComposeChart9(
    modelProducer: CandlestickEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val startAxis = startAxis(
        maxLabelCount = MAX_LABEL_COUNT,
    )

    val candlestickChart = CandlestickChart(
        config = CandlestickChart.Config.hollow(),
    )

    Chart(
        modifier = modifier,
        chart = candlestickChart,
        chartModelProducer = modelProducer,
        startAxis = startAxis,
        bottomAxis = bottomAxis(),
        marker = rememberMarker(),
    )
}

@Suppress("UnusedPrivateMember")
@Composable
internal fun ViewChart9(
    modelProducer: CandlestickEntryModelProducer,
    modifier: Modifier = Modifier,
) {

    val candlestickChart = CandlestickChart(
        config = CandlestickChart.Config.hollow(),
    )

    AndroidViewBinding(Chart9Binding::inflate, modifier) {
        with(chartView) {
            chart = candlestickChart
            (startAxis as VerticalAxis<AxisPosition.Vertical.Start>).maxLabelCount = MAX_LABEL_COUNT
            entryProducer = modelProducer
        }
    }
}

private const val MAX_LABEL_COUNT = 5
