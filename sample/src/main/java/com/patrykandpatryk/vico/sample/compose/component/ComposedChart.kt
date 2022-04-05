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

package com.patrykandpatryk.vico.sample.compose.component

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.chart.entry.defaultDiffAnimationSpec
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.chart.composed.plus
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.composed.ComposedChartEntryModelProducer

@Composable
internal fun ComposedChart(
    modifier: Modifier = Modifier,
    model: ComposedChartEntryModelProducer<ChartEntryModel>,
    diffAnimationSpec: AnimationSpec<Float> = defaultDiffAnimationSpec,
) {
    val columnChart = columnChart()
    val lineChart = lineChart()
    Chart(
        modifier = modifier,
        chart = remember(currentChartStyle) { columnChart + lineChart },
        chartModelProducer = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
        marker = marker(),
        diffAnimationSpec = diffAnimationSpec,
    )
}
