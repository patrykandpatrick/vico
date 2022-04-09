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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer

@Composable
internal fun LineChart(
    modifier: Modifier = Modifier,
    chartEntryModelProducer: ChartEntryModelProducer,
) {
    val decorations = listOf(
        ThresholdLine(
            thresholdValue = THRESHOLD_VALUE,
            textComponent = thresholdLineLabel(
                color = MaterialTheme.colorScheme.onSecondary,
                backgroundColor = MaterialTheme.colorScheme.secondary,
            ),
            lineComponent = ShapeComponent(
                strokeColor = MaterialTheme.colorScheme.secondary.toArgb(),
                strokeWidthDp = THRESHOLD_LINE_STROKE_WIDTH_DP,
            )
        )
    )

    Chart(
        modifier = modifier,
        chart = lineChart(decorations = decorations),
        chartModelProducer = chartEntryModelProducer,
        marker = marker(),
        bottomAxis = bottomAxis(),
        startAxis = startAxis(),
    )
}

private const val THRESHOLD_VALUE = 7f
private const val THRESHOLD_LINE_STROKE_WIDTH_DP = 2f
