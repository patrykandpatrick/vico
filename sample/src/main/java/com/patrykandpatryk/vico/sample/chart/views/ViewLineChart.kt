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

package com.patrykandpatryk.vico.sample.chart.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.databinding.LineChartBinding
import com.patrykandpatryk.vico.sample.util.SampleChartTokens
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ViewLineChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    val tokens = SampleChartTokens.LineChart
    AndroidViewBinding(
        factory = LineChartBinding::inflate,
        modifier = modifier,
    ) {
        chart.entryProducer = chartEntryModelProducer
        chart.marker = marker
        (chart.bottomAxis as Axis).guideline = null
        chart.chart?.addPersistentMarker(
            x = tokens.PERSISTENT_MARKER_X,
            marker = marker,
        )
    }
}
