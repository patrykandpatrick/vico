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

package pl.patrykgoworowski.vico.sample.ui.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import pl.patrykgoworowski.vico.compose.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.compose.axis.vertical.startAxis
import pl.patrykgoworowski.vico.compose.chart.Chart
import pl.patrykgoworowski.vico.compose.chart.column.columnChart
import pl.patrykgoworowski.vico.compose.chart.entry.defaultDiffAnimationSpec
import pl.patrykgoworowski.vico.compose.chart.line.lineChart
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.chart.composed.plus
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.composed.ComposedChartEntryModelProducer

@Composable
internal fun ComposedChart(
    modifier: Modifier = Modifier,
    model: ComposedChartEntryModelProducer<ChartEntryModel>,
    diffAnimationSpec: AnimationSpec<Float> = defaultDiffAnimationSpec,
) {
    val chartStyle = currentChartStyle
    Chart(
        modifier = modifier,
        chart = remember { columnChart(chartStyle) + lineChart(chartStyle) },
        chartModelProducer = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
        marker = markerComponent(),
        diffAnimationSpec = diffAnimationSpec,
    )
}
