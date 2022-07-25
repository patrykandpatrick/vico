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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.component.shape.lineComponent
import com.patrykandpatryk.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatryk.vico.compose.component.shape.textComponent
import com.patrykandpatryk.vico.compose.dimensions.dimensionsOf
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.axis.formatter.PercentageFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.databinding.ColumnChartBinding
import com.patrykandpatryk.vico.sample.util.marker

@Composable
internal fun ComposeColumnChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val startAxis = startAxis(
        maxLabelCount = MAX_LABEL_COUNT,
        valueFormatter = PercentageFormatAxisValueFormatter(),
    )
    val decorations = listOf(rememberLineChartThresholdLine())
    val columnChart = columnChart(
        decorations = decorations,
        columns = entityColors.map {
            lineComponent(
                color = Color(it),
                thickness = COLUMN_WIDTH_DP.dp,
                shape = Shapes.roundedCornerShape(allPercent = DefaultDimens.COLUMN_ROUNDNESS_PERCENT),
            )
        },
    )
    Chart(
        modifier = modifier,
        chart = columnChart,
        chartModelProducer = chartEntryModelProducer,
        startAxis = startAxis,
        bottomAxis = bottomAxis(
            tickPosition = HorizontalAxis.TickPosition.Center(
                offset = 1,
                spacing = 3,
            ),
        ),
        marker = marker(),
    )
}

@Composable
internal fun ViewColumnChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    val thresholdLine = rememberLineChartThresholdLine()
    AndroidViewBinding(
        factory = ColumnChartBinding::inflate,
        modifier = modifier,
    ) {
        chartView.entryProducer = chartEntryModelProducer
        chartView.marker = marker
        chartView.chart?.addDecoration(decoration = thresholdLine)
        with(chartView.startAxis as VerticalAxis) {
            this.maxLabelCount = MAX_LABEL_COUNT
            this.valueFormatter = PercentageFormatAxisValueFormatter()
        }
    }
}

@Composable
private fun rememberLineChartThresholdLine(): ThresholdLine {
    val labelComponent = textComponent(
        color = MaterialTheme.colorScheme.surface,
        padding = dimensionsOf(all = THRESHOLD_LINE_PADDING_DP.dp),
        margins = dimensionsOf(all = THRESHOLD_LINE_MARGINS_DP.dp),
        background = ShapeComponent(
            shape = Shapes.roundedCornerShape(all = THRESHOLD_LINE_CORNER_RADIUS_DP.dp),
            color = THRESHOLD_LINE_COLOR.toInt(),
        ),
    )
    return remember(labelComponent) {
        ThresholdLine(
            thresholdValue = THRESHOLD_VALUE,
            labelComponent = labelComponent,
            lineComponent = ShapeComponent(
                strokeColor = THRESHOLD_LINE_COLOR.toInt(),
                strokeWidthDp = THRESHOLD_LINE_STROKE_WIDTH_DP,
            ),
        )
    }
}

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFFFF6F3C)
private const val THRESHOLD_VALUE = 13f
private const val THRESHOLD_LINE_COLOR = 0xFF3EC1D3
private const val THRESHOLD_LINE_STROKE_WIDTH_DP = 2f
private const val THRESHOLD_LINE_PADDING_DP = 4f
private const val THRESHOLD_LINE_MARGINS_DP = 4f
private const val MAX_LABEL_COUNT = 5
private const val THRESHOLD_LINE_CORNER_RADIUS_DP = 4f
private const val COLUMN_WIDTH_DP = 16f
