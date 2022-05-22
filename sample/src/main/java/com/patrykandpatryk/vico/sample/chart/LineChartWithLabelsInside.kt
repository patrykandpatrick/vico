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

package com.patrykandpatryk.vico.sample.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
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
import com.patrykandpatryk.vico.databinding.LineChartWithLabelsInsideBinding
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ComposeLineChartWithLabelsInside(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val lineChart = lineChart(
        lines = entityColors.map {
            lineSpec(
                lineColor = Color(it),
                lineBackgroundShader = null,
            )
        }
    )
    val startAxis = startAxis(
        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
        label = lineChartWithLabelsInsideAxisLabel(),
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

@Composable
internal fun ViewLineChartWithLabelsInside(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    val axisLabel = lineChartWithLabelsInsideAxisLabel()
    AndroidViewBinding(
        factory = LineChartWithLabelsInsideBinding::inflate,
        modifier = modifier,
    ) {
        chart.entryProducer = chartEntryModelProducer
        chart.marker = marker
        with(chart.startAxis as VerticalAxis) {
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
            label = axisLabel
        }
    }
}

@Composable
private fun lineChartWithLabelsInsideAxisLabel() = axisLabelComponent(
    horizontalPadding = LABEL_PADDING_HORIZONTAL_DP.dp,
    verticalPadding = LABEL_PADDING_VERTICAL_DP.dp,
    horizontalMargin = LABEL_MARGIN_DP.dp,
    verticalMargin = LABEL_MARGIN_DP.dp,
    background = ShapeComponent(
        shape = Shapes.pillShape,
        color = LABEL_BACKGROUND_COLOR.toInt(),
    ),
)

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFFB983FF, 0xFF94B3FD, 0xFF94DAFF)
private const val LABEL_BACKGROUND_COLOR = 0xFFFABB51
private const val LABEL_PADDING_VERTICAL_DP = 4f
private const val LABEL_PADDING_HORIZONTAL_DP = 8f
private const val LABEL_MARGIN_DP = 4f
