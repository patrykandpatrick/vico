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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.edges.rememberFadingEdges
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.databinding.Chart3Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart3(uiSystem: UISystem, chartEntryModelProducer: ChartEntryModelProducer) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart3(chartEntryModelProducer)
        UISystem.Views -> ViewChart3(chartEntryModelProducer)
    }
}

@Composable
private fun ComposeChart3(chartEntryModelProducer: ChartEntryModelProducer) {
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        Chart(
            chart = lineChart(pointPosition = LineChart.PointPosition.Start, axisValuesOverrider = axisValueOverrider),
            chartModelProducer = chartEntryModelProducer,
            startAxis = startAxis(
                guideline = null,
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                titleComponent = textComponent(
                    color = Color.Black,
                    background = shapeComponent(Shapes.pillShape, color1),
                    padding = axisTitlePadding,
                    margins = startAxisTitleMargins,
                    typeface = Typeface.MONOSPACE,
                ),
                title = stringResource(R.string.y_axis),
            ),
            bottomAxis = bottomAxis(
                titleComponent = textComponent(
                    background = shapeComponent(Shapes.pillShape, color2),
                    color = Color.White,
                    padding = axisTitlePadding,
                    margins = bottomAxisTitleMargins,
                    typeface = Typeface.MONOSPACE,
                ),
                title = stringResource(R.string.x_axis),
            ),
            marker = rememberMarker(),
            fadingEdges = rememberFadingEdges(),
        )
    }
}

@Composable
private fun ViewChart3(chartEntryModelProducer: ChartEntryModelProducer) {
    val marker = rememberMarker()
    AndroidViewBinding(Chart3Binding::inflate) {
        (chartView.chart as LineChart).axisValuesOverrider = axisValueOverrider
        chartView.entryProducer = chartEntryModelProducer
        chartView.marker = marker
    }
}

private const val COLOR_1_CODE = 0xffffbb00
private const val COLOR_2_CODE = 0xff9db591
private const val AXIS_VALUE_OVERRIDER_Y_FRACTION = 1.2f

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val chartColors = listOf(color1, color2)
private val axisValueOverrider =
    AxisValuesOverrider.adaptiveYValues(yFraction = AXIS_VALUE_OVERRIDER_Y_FRACTION, round = true)
private val axisTitleHorizontalPaddingValue = 8.dp
private val axisTitleVerticalPaddingValue = 2.dp
private val axisTitlePadding = dimensionsOf(axisTitleHorizontalPaddingValue, axisTitleVerticalPaddingValue)
private val axisTitleMarginValue = 4.dp
private val startAxisTitleMargins = dimensionsOf(end = axisTitleMarginValue)
private val bottomAxisTitleMargins = dimensionsOf(top = axisTitleMarginValue)
