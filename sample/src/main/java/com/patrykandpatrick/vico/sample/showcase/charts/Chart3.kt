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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.edges.rememberFadingEdges
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.LineCartesianLayerModel
import com.patrykandpatrick.vico.databinding.Chart3Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart3(
    uiSystem: UISystem,
    modelProducer: CartesianChartModelProducer,
) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart3(modelProducer)
        UISystem.Views -> ViewChart3(modelProducer)
    }
}

@Composable
private fun ComposeChart3(modelProducer: CartesianChartModelProducer) {
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(axisValueOverrider = axisValueOverrider),
                    startAxis =
                        rememberStartAxis(
                            guideline = null,
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                            titleComponent =
                                rememberTextComponent(
                                    color = Color.Black,
                                    background = rememberShapeComponent(Shapes.pillShape, color1),
                                    padding = axisTitlePadding,
                                    margins = startAxisTitleMargins,
                                    typeface = Typeface.MONOSPACE,
                                ),
                            title = stringResource(R.string.y_axis),
                        ),
                    bottomAxis =
                        rememberBottomAxis(
                            titleComponent =
                                rememberTextComponent(
                                    background = rememberShapeComponent(Shapes.pillShape, color2),
                                    color = Color.White,
                                    padding = axisTitlePadding,
                                    margins = bottomAxisTitleMargins,
                                    typeface = Typeface.MONOSPACE,
                                ),
                            title = stringResource(R.string.x_axis),
                        ),
                    fadingEdges = rememberFadingEdges(),
                ),
            modelProducer = modelProducer,
            marker = rememberMarker(labelPosition = Marker.LabelPosition.AboveIndicator()),
            runInitialAnimation = false,
            horizontalLayout = horizontalLayout,
        )
    }
}

@Composable
private fun ViewChart3(modelProducer: CartesianChartModelProducer) {
    val marker = rememberMarker(labelPosition = Marker.LabelPosition.AboveIndicator())
    AndroidViewBinding(Chart3Binding::inflate) {
        with(chartView) {
            (chart?.layers?.get(0) as LineCartesianLayer?)?.axisValueOverrider = axisValueOverrider
            runInitialAnimation = false
            this.modelProducer = modelProducer
            this.marker = marker
        }
    }
}

private const val COLOR_1_CODE = 0xffffbb00
private const val COLOR_2_CODE = 0xff9db591
private const val AXIS_VALUE_OVERRIDER_Y_FRACTION = 1.2f

private val color1 = Color(COLOR_1_CODE)
private val color2 = Color(COLOR_2_CODE)
private val chartColors = listOf(color1, color2)
private val axisValueOverrider =
    AxisValueOverrider.adaptiveYValues<LineCartesianLayerModel>(
        yFraction = AXIS_VALUE_OVERRIDER_Y_FRACTION,
        round = true,
    )
private val axisTitleHorizontalPaddingValue = 8.dp
private val axisTitleVerticalPaddingValue = 2.dp
private val axisTitlePadding = dimensionsOf(axisTitleHorizontalPaddingValue, axisTitleVerticalPaddingValue)
private val axisTitleMarginValue = 4.dp
private val startAxisTitleMargins = dimensionsOf(end = axisTitleMarginValue)
private val bottomAxisTitleMargins = dimensionsOf(top = axisTitleMarginValue)
private val horizontalLayout = HorizontalLayout.fullWidth()
