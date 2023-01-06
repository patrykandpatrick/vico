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

package com.patrykandpatrick.vico.sample.chart

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.databinding.Chart6Binding
import com.patrykandpatrick.vico.sample.util.rememberChartStyle
import com.patrykandpatrick.vico.sample.util.rememberMarker

@Composable
internal fun ComposeChart6(chartEntryModelProducer: ChartEntryModelProducer, modifier: Modifier = Modifier) {
    val thresholdLine = rememberThresholdLine()
    ProvideChartStyle(rememberChartStyle(entityColors)) {
        Chart(
            chart = columnChart(
                mergeMode = ColumnChart.MergeMode.Grouped,
                decorations = remember(thresholdLine) { listOf(thresholdLine) },
            ),
            chartModelProducer = chartEntryModelProducer,
            modifier = modifier,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(valueFormatter = bottomAxisValueFormatter),
            marker = rememberMarker(),
        )
    }
}

@Composable
internal fun ViewChart6(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val thresholdLine = rememberThresholdLine()
    val decorations = remember(thresholdLine) { listOf(thresholdLine) }
    val marker = rememberMarker()
    AndroidViewBinding(Chart6Binding::inflate, modifier) {
        chartView.chart?.setDecorations(decorations)
        chartView.entryProducer = chartEntryModelProducer
        (chartView.bottomAxis as? HorizontalAxis<AxisPosition.Horizontal.Bottom>)?.valueFormatter =
            bottomAxisValueFormatter
        chartView.marker = marker
    }
}

@Composable
private fun rememberThresholdLine(): ThresholdLine {
    val label = textComponent(
        color = Color.White,
        background = shapeComponent(Shapes.roundedCornerShape(thresholdLineLabelBackgroundCornerRadius), color1),
        padding = thresholdLineLabelPadding,
        margins = thresholdLineLabelMargins,
        typeface = Typeface.MONOSPACE,
    )
    val line = shapeComponent(color = thresholdLineColor)
    return remember(label, line) {
        ThresholdLine(thresholdRange = thresholdLineValueRange, labelComponent = label, lineComponent = line)
    }
}

private const val COLOR_1_CODE = 0xff68a8ad
private const val COLOR_2_CODE = 0xff95c1c6
private const val COLOR_3_CODE = 0xffe4cba0
private const val THRESHOLD_LINE_VALUE_RANGE_START = 7f
private const val THRESHOLD_LINE_VALUE_RANGE_END = 14f
private const val THRESHOLD_LINE_ALPHA = .16f

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val color3 = Color(COLOR_3_CODE)
private val entityColors = listOf(color1, color2, color3)
private val thresholdLineValueRange = THRESHOLD_LINE_VALUE_RANGE_START..THRESHOLD_LINE_VALUE_RANGE_END
private val thresholdLineLabelBackgroundCornerRadius = 4.dp
private val thresholdLineLabelPaddingValue = 4.dp
private val thresholdLineLabelMarginValue = 4.dp
private val thresholdLineLabelPadding = dimensionsOf(thresholdLineLabelPaddingValue)
private val thresholdLineLabelMargins = dimensionsOf(thresholdLineLabelMarginValue)
private val thresholdLineColor = color1.copy(THRESHOLD_LINE_ALPHA)
private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val bottomAxisValueFormatter =
    AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _ -> daysOfWeek[x.toInt() % daysOfWeek.size] }
