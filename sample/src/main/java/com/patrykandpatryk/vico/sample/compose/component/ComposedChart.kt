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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.horizontal.topAxis
import com.patrykandpatryk.vico.compose.axis.vertical.endAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.chart.entry.defaultDiffAnimationSpec
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.compose.component.shape.lineComponent
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.chart.composed.plus
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatryk.vico.sample.util.Tokens

@Composable
internal fun ComposedChart(
    modifier: Modifier = Modifier,
    model: ComposedChartEntryModelProducer<ChartEntryModel>,
    diffAnimationSpec: AnimationSpec<Float> = defaultDiffAnimationSpec,
) {
    val lineBackgroundShader = dottedShader(
        dotColor = MaterialTheme.colorScheme.secondary.copy(
            alpha = Tokens.ComposedChart.SHADER_ALPHA,
        ),
    )

    val lineChart = lineChart(
        lines = listOf(
            lineSpec(
                lineBackgroundShader = lineBackgroundShader,
                lineColor = MaterialTheme.colorScheme.secondary,
            ),
        ),
    )

    val columnChart = columnChart(
        columns = listOf(
            lineComponent(
                color = MaterialTheme.colorScheme.primary,
                thickness = DefaultDimens.COLUMN_WIDTH.dp,
                shape = Shapes.cutCornerShape(topLeftPercent = 50),
            ),
        ),
    )

    Chart(
        modifier = modifier,
        chart = remember(currentChartStyle) { columnChart + lineChart },
        chartModelProducer = model,
        topAxis = topAxis(),
        endAxis = endAxis(),
        marker = marker(),
        diffAnimationSpec = diffAnimationSpec,
    )
}
