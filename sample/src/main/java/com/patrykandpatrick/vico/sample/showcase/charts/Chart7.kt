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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.legend.legendItem
import com.patrykandpatrick.vico.compose.legend.verticalLegend
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.copy
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.databinding.Chart7Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart7(
    uiSystem: UISystem,
    modelProducer: CartesianChartModelProducer,
) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart7(modelProducer)
        UISystem.Views -> ViewChart7(modelProducer)
    }
}

@Composable
private fun ComposeChart7(modelProducer: CartesianChartModelProducer) {
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        val defaultLines = currentChartStyle.lineLayer.lines
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        remember(defaultLines) { defaultLines.map { it.copy(backgroundShader = null) } },
                    ),
                ),
            modelProducer = modelProducer,
            startAxis =
                rememberStartAxis(
                    label = rememberStartAxisLabel(),
                    horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                ),
            bottomAxis = rememberBottomAxis(),
            marker = rememberMarker(),
            legend = rememberLegend(),
            runInitialAnimation = false,
        )
    }
}

@Composable
private fun ViewChart7(modelProducer: CartesianChartModelProducer) {
    val startAxisLabel = rememberStartAxisLabel()
    val marker = rememberMarker()
    val legend = rememberLegend()
    AndroidViewBinding(Chart7Binding::inflate) {
        with(chartView) {
            runInitialAnimation = false
            this.modelProducer = modelProducer
            (startAxis as VerticalAxis).horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
            (startAxis as VerticalAxis).label = startAxisLabel
            this.marker = marker
            this.legend = legend
        }
    }
}

@Composable
private fun rememberStartAxisLabel() =
    axisLabelComponent(
        color = Color.Black,
        verticalPadding = startAxisLabelVerticalPaddingValue,
        horizontalPadding = startAxisLabelHorizontalPaddingValue,
        verticalMargin = startAxisLabelMarginValue,
        horizontalMargin = startAxisLabelMarginValue,
        background = rememberShapeComponent(Shapes.roundedCornerShape(startAxisLabelBackgroundCornerRadius), color4),
    )

@Composable
private fun rememberLegend() =
    verticalLegend(
        items =
            chartColors.mapIndexed { index, chartColor ->
                legendItem(
                    icon = rememberShapeComponent(Shapes.pillShape, chartColor),
                    label =
                        rememberTextComponent(
                            color = currentChartStyle.axis.axisLabelColor,
                            textSize = legendItemLabelTextSize,
                            typeface = Typeface.MONOSPACE,
                        ),
                    labelText = stringResource(R.string.series_x, index + 1),
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
