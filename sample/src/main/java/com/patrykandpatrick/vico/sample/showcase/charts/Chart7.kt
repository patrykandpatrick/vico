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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.legend.legendItem
import com.patrykandpatrick.vico.compose.legend.verticalLegend
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.copy
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.databinding.Chart7Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart7(uiSystem: UISystem, chartEntryModelProducer: ChartEntryModelProducer) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart7(chartEntryModelProducer)
        UISystem.Views -> ViewChart7(chartEntryModelProducer)
    }
}

@Composable
private fun ComposeChart7(chartEntryModelProducer: ChartEntryModelProducer) {
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        val defaultLines = currentChartStyle.lineChart.lines
        Chart(
            chart = lineChart(
                remember(defaultLines) {
                    defaultLines.map { defaultLine -> defaultLine.copy(lineBackgroundShader = null) }
                },
            ),
            chartModelProducer = chartEntryModelProducer,
            startAxis = startAxis(
                label = rememberStartAxisLabel(),
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            ),
            bottomAxis = bottomAxis(),
            marker = rememberMarker(),
            legend = rememberLegend(),
        )
    }
}

@Composable
private fun ViewChart7(chartEntryModelProducer: ChartEntryModelProducer) {
    val startAxisLabel = rememberStartAxisLabel()
    val marker = rememberMarker()
    val legend = rememberLegend()
    AndroidViewBinding(Chart7Binding::inflate) {
        chartView.entryProducer = chartEntryModelProducer
        with(chartView.startAxis as VerticalAxis) {
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
            label = startAxisLabel
        }
        chartView.marker = marker
        chartView.legend = legend
    }
}

@Composable
private fun rememberStartAxisLabel() = axisLabelComponent(
    color = Color.Black,
    verticalPadding = startAxisLabelVerticalPaddingValue,
    horizontalPadding = startAxisLabelHorizontalPaddingValue,
    verticalMargin = startAxisLabelMarginValue,
    horizontalMargin = startAxisLabelMarginValue,
    background = shapeComponent(Shapes.roundedCornerShape(startAxisLabelBackgroundCornerRadius), color4),
)

@Composable
private fun rememberLegend() = verticalLegend(
    items = chartColors.mapIndexed { index, chartColor ->
        legendItem(
            icon = shapeComponent(Shapes.pillShape, chartColor),
            label = textComponent(
                color = currentChartStyle.axis.axisLabelColor,
                textSize = legendItemLabelTextSize,
                typeface = Typeface.MONOSPACE,
            ),
            labelText = stringResource(R.string.data_set_x, index + 1),
        )
    },
    iconSize = legendItemIconSize,
    iconPadding = legendItemIconPaddingValue,
    spacing = legendItemSpacing,
    padding = legendPadding,
)

private const val COLOR_1_CODE = 0xffb983ff
private const val COLOR_2_CODE = 0xff91b1fd
private const val COLOR_3_CODE = 0xff8fdaff
private const val COLOR_4_CODE = 0xfffab94d

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val color3 = Color(COLOR_3_CODE)
private val color4 = Color(COLOR_4_CODE)
private val chartColors = listOf(color1, color2, color3)
private val startAxisLabelVerticalPaddingValue = 2.dp
private val startAxisLabelHorizontalPaddingValue = 8.dp
private val startAxisLabelMarginValue = 4.dp
private val startAxisLabelBackgroundCornerRadius = 4.dp
private val legendItemLabelTextSize = 12.sp
private val legendItemIconSize = 8.dp
private val legendItemIconPaddingValue = 10.dp
private val legendItemSpacing = 4.dp
private val legendTopPaddingValue = 8.dp
private val legendPadding = dimensionsOf(top = legendTopPaddingValue)
