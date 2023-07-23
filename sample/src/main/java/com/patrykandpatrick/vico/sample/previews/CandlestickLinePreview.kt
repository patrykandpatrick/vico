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

package com.patrykandpatrick.vico.sample.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.candlestick.hollow
import com.patrykandpatrick.vico.core.chart.candlestick.CandlestickChart
import com.patrykandpatrick.vico.core.util.SampleCandlestickEntryProvider

@Preview(widthDp = 350)
@Composable
public fun CandlestickLinePreview() {
    Surface {
        val candlestickChart = CandlestickChart(
            config = CandlestickChart.Config.hollow(),
        )

        Chart(
            chart = candlestickChart,
            model = SampleCandlestickEntryProvider.sampleModel,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(),
        )
    }
}
