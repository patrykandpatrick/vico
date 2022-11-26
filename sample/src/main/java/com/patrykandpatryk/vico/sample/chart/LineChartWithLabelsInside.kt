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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatryk.vico.R
import com.patrykandpatryk.vico.compose.axis.axisLabelComponent
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.compose.component.shapeComponent
import com.patrykandpatryk.vico.compose.component.textComponent
import com.patrykandpatryk.vico.compose.dimensions.dimensionsOf
import com.patrykandpatryk.vico.compose.legend.verticalLegend
import com.patrykandpatryk.vico.compose.legend.verticalLegendItem
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.legend.VerticalLegend
import com.patrykandpatryk.vico.databinding.LineChartWithLabelsInsideBinding
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ComposeLineChartWithLabelsInside(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val lineChart = lineChart(
        pointPosition = LineChart.PointPosition.Start,
        lines = entityColors.map { color ->
            lineSpec(
                lineColor = Color(color),
                lineBackgroundShader = null,
            )
        },
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
        legend = legend(),
    )
}

@Composable
internal fun ViewLineChartWithLabelsInside(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    val axisLabel = lineChartWithLabelsInsideAxisLabel()
    val legend = legend()
    AndroidViewBinding(
        factory = LineChartWithLabelsInsideBinding::inflate,
        modifier = modifier,
    ) {
        chartView.entryProducer = chartEntryModelProducer
        chartView.marker = marker
        chartView.legend = legend

        with(chartView.startAxis as VerticalAxis) {
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
    background = shapeComponent(
        shape = Shapes.pillShape,
        color = Color(LABEL_BACKGROUND_COLOR),
    ),
)

@Composable
private fun legend(): VerticalLegend = verticalLegend(
    items = entityColors.mapIndexed { index, color ->
        verticalLegendItem(
            icon = shapeComponent(shape = Shapes.pillShape, color = Color(color)),
            label = textComponent(textSize = LEGEND_LABEL_SIZE_SP.sp),
            labelText = LocalContext.current.getString(R.string.line_chart_with_labels_inside_label_legend, index),
        )
    },
    iconSize = 8.dp,
    iconPadding = 10.dp,
    spacing = 4.dp,
    padding = dimensionsOf(top = 8.dp),
)

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFFB983FF, 0xFF94B3FD, 0xFF94DAFF)
private const val LABEL_BACKGROUND_COLOR = 0xFFFABB51
private const val LABEL_PADDING_VERTICAL_DP = 4f
private const val LABEL_PADDING_HORIZONTAL_DP = 8f
private const val LABEL_MARGIN_DP = 4f
private const val LEGEND_LABEL_SIZE_SP = 12f
