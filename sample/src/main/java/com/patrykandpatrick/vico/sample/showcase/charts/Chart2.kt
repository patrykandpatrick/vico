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

package com.patrykandpatrick.vico.sample.showcase.charts

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.PercentageFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.databinding.Chart2Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart2(uiSystem: UISystem, chartEntryModelProducer: ChartEntryModelProducer) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart2(chartEntryModelProducer)
        UISystem.Views -> ViewChart2(chartEntryModelProducer)
    }
}

@Composable
private fun ComposeChart2(chartEntryModelProducer: ChartEntryModelProducer) {
    val thresholdLine = rememberThresholdLine()
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        val defaultColumns = currentChartStyle.columnChart.columns
        Chart(
            chart = columnChart(
                columns = remember(defaultColumns) {
                    defaultColumns.map { defaultColumn ->
                        LineComponent(defaultColumn.color, COLUMN_WIDTH_DP, defaultColumn.shape)
                    }
                },
                decorations = remember(thresholdLine) { listOf(thresholdLine) },
            ),
            chartModelProducer = chartEntryModelProducer,
            startAxis = startAxis(valueFormatter = startAxisValueFormatter, itemPlacer = startAxisItemPlacer),
            bottomAxis = bottomAxis(itemPlacer = bottomAxisItemPlacer),
            marker = rememberMarker(),
            runInitialAnimation = false,
            horizontalLayout = horizontalLayout,
        )
    }
}

@Composable
private fun ViewChart2(chartEntryModelProducer: ChartEntryModelProducer) {
    val thresholdLine = rememberThresholdLine()
    val marker = rememberMarker()
    AndroidViewBinding(Chart2Binding::inflate) {
        with(chartView) {
            chart?.addDecoration(thresholdLine)
            runInitialAnimation = false
            entryProducer = chartEntryModelProducer
            with(startAxis as VerticalAxis) {
                itemPlacer = startAxisItemPlacer
                valueFormatter = startAxisValueFormatter
            }
            (bottomAxis as HorizontalAxis).itemPlacer = bottomAxisItemPlacer
            this.marker = marker
        }
    }
}

@Composable
private fun rememberThresholdLine(): ThresholdLine {
    val line = shapeComponent(color = color2)
    val label = textComponent(
        color = Color.Black,
        background = shapeComponent(Shapes.pillShape, color2),
        padding = thresholdLineLabelPadding,
        margins = thresholdLineLabelMargins,
        typeface = Typeface.MONOSPACE,
    )
    return remember(line, label) {
        ThresholdLine(thresholdValue = THRESHOLD_LINE_VALUE, lineComponent = line, labelComponent = label)
    }
}

private const val COLOR_1_CODE = 0xffff5500
private const val COLOR_2_CODE = 0xffd3d826
private const val COLUMN_WIDTH_DP = 16f
private const val THRESHOLD_LINE_VALUE = 13f
private const val MAX_START_AXIS_ITEM_COUNT = 6
private const val BOTTOM_AXIS_ITEM_SPACING = 3
private const val BOTTOM_AXIS_ITEM_OFFSET = 1

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val chartColors = listOf(color1)
private val thresholdLineLabelMarginValue = 4.dp
private val thresholdLineLabelHorizontalPaddingValue = 8.dp
private val thresholdLineLabelVerticalPaddingValue = 2.dp
private val thresholdLineLabelPadding =
    dimensionsOf(thresholdLineLabelHorizontalPaddingValue, thresholdLineLabelVerticalPaddingValue)
private val thresholdLineLabelMargins = dimensionsOf(thresholdLineLabelMarginValue)
private val startAxisValueFormatter = PercentageFormatAxisValueFormatter<AxisPosition.Vertical.Start>()
private val horizontalLayout = HorizontalLayout.FullWidth(
    startPaddingDp = DefaultDimens.COLUMN_OUTSIDE_SPACING.half,
    endPaddingDp = DefaultDimens.COLUMN_OUTSIDE_SPACING.half,
)
private val startAxisItemPlacer = AxisItemPlacer.Vertical.default(MAX_START_AXIS_ITEM_COUNT)
private val bottomAxisItemPlacer = AxisItemPlacer.Horizontal.default(BOTTOM_AXIS_ITEM_SPACING, BOTTOM_AXIS_ITEM_OFFSET)
