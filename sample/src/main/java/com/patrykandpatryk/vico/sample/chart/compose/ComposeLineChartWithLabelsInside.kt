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

package com.patrykandpatryk.vico.sample.chart.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.axisLabelComponent
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.sample.util.marker
import com.patrykandpatryk.vico.sample.util.SampleChartTokens

@Composable
internal fun ComposeLineChartWithLabelsInside(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val tokens = SampleChartTokens.LineChartWithLabelsInside
    val axisLabel = axisLabelComponent(
        horizontalPadding = tokens.LABEL_PADDING_HORIZONTAL_DP.dp,
        verticalPadding = tokens.LABEL_PADDING_VERTICAL_DP.dp,
        horizontalMargin = tokens.LABEL_MARGIN_DP.dp,
        verticalMargin = tokens.LABEL_MARGIN_DP.dp,
        background = ShapeComponent(
            shape = Shapes.pillShape,
            color = tokens.LABEL_BACKGROUND_COLOR.toInt(),
        ),
    )
    val lineChart = lineChart(
        lines = listOf(
            lineSpec(
                lineColor = Color(tokens.entityColors[0]),
                lineBackgroundShader = null,
            ),
            lineSpec(
                lineColor = Color(tokens.entityColors[1]),
                lineBackgroundShader = null,
            ),
            lineSpec(
                lineColor = Color(tokens.entityColors[2]),
                lineBackgroundShader = null,
            ),
        ),
    )
    val startAxis = startAxis(
        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
        label = axisLabel,
    )
    Chart(
        modifier = modifier,
        chart = lineChart,
        chartModelProducer = chartEntryModelProducer,
        startAxis = startAxis,
        bottomAxis = bottomAxis(),
        marker = marker(),
    )
}
