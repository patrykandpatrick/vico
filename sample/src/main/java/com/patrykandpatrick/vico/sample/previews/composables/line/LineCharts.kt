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

package com.patrykandpatrick.vico.sample.previews.composables.line

import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import com.patrykandpatrick.vico.sample.previews.annotation.ChartPreview
import com.patrykandpatrick.vico.sample.previews.resource.PreviewSurface
import com.patrykandpatrick.vico.sample.previews.resource.mediumLineModel
import com.patrykandpatrick.vico.sample.previews.resource.shortLineModel

@ChartPreview
@Composable
public fun DefaultLineChart(
    model: CartesianChartModel = shortLineModel,
    scrollable: Boolean = true,
    initialScroll: InitialScroll = InitialScroll.Start,
) {
    PreviewSurface {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = scrollable, initialScroll = initialScroll),
        )
    }
}

@ChartPreview
@Composable
public fun DefaultLineChartLongScrollable() {
    DefaultLineChart(model = mediumLineModel)
}

@ChartPreview
@Composable
public fun DefaultLineChartLongScrollableEnd() {
    DefaultLineChart(model = mediumLineModel, initialScroll = InitialScroll.End)
}

@ChartPreview
@Composable
public fun DefaultLineChartLongNonScrollable() {
    DefaultLineChart(model = mediumLineModel, scrollable = false)
}
