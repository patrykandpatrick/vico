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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.databinding.Chart6Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart6(
    uiSystem: UISystem,
    modelProducer: CartesianChartModelProducer,
) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart6(modelProducer)
        UISystem.Views -> ViewChart6(modelProducer)
    }
}

@Composable
private fun ComposeChart6(modelProducer: CartesianChartModelProducer) {
    val thresholdLine = rememberThresholdLine()
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        val defaultColumns = currentChartStyle.columnLayer.columns
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        remember(defaultColumns) {
                            defaultColumns.map { defaultColumn ->
                                LineComponent(
                                    defaultColumn.color,
                                    defaultColumn.thicknessDp,
                                    Shapes.cutCornerShape(topLeftPercent = COLUMN_CORNER_CUT_SIZE_PERCENT),
                                )
                            }
                        },
                        mergeMode = ColumnCartesianLayer.MergeMode.Grouped,
                    ),
                    decorations = remember(thresholdLine) { listOf(thresholdLine) },
                ),
            modelProducer = modelProducer,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
            marker = rememberMarker(),
            runInitialAnimation = false,
        )
    }
}

@Composable
private fun ViewChart6(modelProducer: CartesianChartModelProducer) {
    val thresholdLine = rememberThresholdLine()
    val decorations = remember(thresholdLine) { listOf(thresholdLine) }
    val marker = rememberMarker()
    AndroidViewBinding(Chart6Binding::inflate) {
        with(chartView) {
            chart?.setDecorations(decorations)
            runInitialAnimation = false
            this.modelProducer = modelProducer
            (bottomAxis as? HorizontalAxis<AxisPosition.Horizontal.Bottom>)?.valueFormatter = bottomAxisValueFormatter
            this.marker = marker
        }
    }
}

@Composable
private fun rememberThresholdLine(): ThresholdLine {
    val label =
        rememberTextComponent(
            color = Color.Black,
            background = shapeComponent(Shapes.rectShape, color4),
            padding = thresholdLineLabelPadding,
            margins = thresholdLineLabelMargins,
            typeface = Typeface.MONOSPACE,
        )
    val line = shapeComponent(color = thresholdLineColor)
    return remember(label, line) {
        ThresholdLine(thresholdRange = thresholdLineValueRange, labelComponent = label, lineComponent = line)
    }
}

private const val COLOR_1_CODE = 0xff3e6558
private const val COLOR_2_CODE = 0xff5e836a
private const val COLOR_3_CODE = 0xffa5ba8e
private const val COLOR_4_CODE = 0xffe9e5af
private const val THRESHOLD_LINE_VALUE_RANGE_START = 7f
private const val THRESHOLD_LINE_VALUE_RANGE_END = 14f
private const val THRESHOLD_LINE_ALPHA = .36f
private const val COLUMN_CORNER_CUT_SIZE_PERCENT = 50

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val color3 = Color(COLOR_3_CODE)
private val color4 = Color(COLOR_4_CODE)
private val chartColors = listOf(color1, color2, color3)
private val thresholdLineValueRange = THRESHOLD_LINE_VALUE_RANGE_START..THRESHOLD_LINE_VALUE_RANGE_END
private val thresholdLineLabelHorizontalPaddingValue = 8.dp
private val thresholdLineLabelVerticalPaddingValue = 2.dp
private val thresholdLineLabelMarginValue = 4.dp
private val thresholdLineLabelPadding =
    dimensionsOf(thresholdLineLabelHorizontalPaddingValue, thresholdLineLabelVerticalPaddingValue)
private val thresholdLineLabelMargins = dimensionsOf(thresholdLineLabelMarginValue)
private val thresholdLineColor = color4.copy(THRESHOLD_LINE_ALPHA)
private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val bottomAxisValueFormatter =
    AxisValueFormatter<AxisPosition.Horizontal.Bottom> { x, _, _ -> daysOfWeek[x.toInt() % daysOfWeek.size] }
