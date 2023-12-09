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

import android.graphics.PorterDuff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.lineSpec
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.shape.dashedShape
import com.patrykandpatrick.vico.compose.component.shape.shader.color
import com.patrykandpatrick.vico.compose.component.shape.shader.fromComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.component.shape.shader.TopBottomShader
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.databinding.Chart9Binding
import com.patrykandpatrick.vico.sample.showcase.UISystem
import com.patrykandpatrick.vico.sample.showcase.rememberChartStyle
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

@Composable
internal fun Chart9(
    uiSystem: UISystem,
    modelProducer: CartesianChartModelProducer,
) {
    when (uiSystem) {
        UISystem.Compose -> ComposeChart9(modelProducer)
        UISystem.Views -> ViewChart9(modelProducer)
    }
}

@Composable
private fun ComposeChart9(modelProducer: CartesianChartModelProducer) {
    val marker = rememberMarker()
    ProvideChartStyle(rememberChartStyle(chartColors)) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lines =
                            listOf(
                                lineSpec(
                                    shader =
                                        TopBottomShader(
                                            DynamicShaders.color(chartColors[0]),
                                            DynamicShaders.color(chartColors[1]),
                                        ),
                                    backgroundShader =
                                        TopBottomShader(
                                            DynamicShaders.composeShader(
                                                DynamicShaders.fromComponent(
                                                    componentSize = 6.dp,
                                                    component =
                                                        rememberShapeComponent(
                                                            shape = Shapes.pillShape,
                                                            color = chartColors[0],
                                                            margins = remember { dimensionsOf(1.dp) },
                                                        ),
                                                ),
                                                verticalGradient(arrayOf(Color.Black, Color.Transparent)),
                                                PorterDuff.Mode.DST_IN,
                                            ),
                                            DynamicShaders.composeShader(
                                                DynamicShaders.fromComponent(
                                                    componentSize = 5.dp,
                                                    component =
                                                        rememberShapeComponent(
                                                            shape = Shapes.rectShape,
                                                            color = chartColors[1],
                                                            margins = remember { dimensionsOf(horizontal = 2.dp) },
                                                        ),
                                                    checkeredArrangement = false,
                                                ),
                                                verticalGradient(arrayOf(Color.Transparent, Color.Black)),
                                                PorterDuff.Mode.DST_IN,
                                            ),
                                        ),
                                ),
                            ),
                    ),
                    startAxis =
                        rememberStartAxis(
                            label =
                                axisLabelComponent(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    background =
                                        rememberShapeComponent(
                                            shape = Shapes.pillShape,
                                            color = MaterialTheme.colorScheme.background,
                                            strokeColor = MaterialTheme.colorScheme.outlineVariant,
                                            strokeWidth = 1.dp,
                                        ),
                                    padding = remember { dimensionsOf(horizontal = 6.dp, vertical = 2.dp) },
                                    margins = remember { dimensionsOf(end = 8.dp) },
                                ),
                            axis = null,
                            tick = null,
                            guideline =
                                rememberLineComponent(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape =
                                        remember {
                                            Shapes.dashedShape(
                                                shape = Shapes.pillShape,
                                                dashLength = 4.dp,
                                                gapLength = 8.dp,
                                            )
                                        },
                                ),
                            itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 4) },
                        ),
                    bottomAxis =
                        rememberBottomAxis(
                            guideline = null,
                            itemPlacer =
                                remember {
                                    AxisItemPlacer.Horizontal.default(
                                        spacing = 3,
                                        addExtremeLabelPadding = true,
                                    )
                                },
                        ),
                ),
            modelProducer = modelProducer,
            marker = marker,
            runInitialAnimation = false,
            horizontalLayout = HorizontalLayout.fullWidth(),
        )
    }
}

@Composable
private fun ViewChart9(modelProducer: CartesianChartModelProducer) {
    val marker = rememberMarker()
    val colors = chartColors
    AndroidViewBinding(Chart9Binding::inflate) {
        with(chartView) {
            runInitialAnimation = false
            this.modelProducer = modelProducer
            (chart?.bottomAxis as Axis).guideline = null
            this.marker = marker
            with(chart?.layers?.get(0) as LineCartesianLayer) {
                lines =
                    listOf(
                        LineCartesianLayer.LineSpec(
                            shader =
                                TopBottomShader(
                                    DynamicShaders.color(colors[0]),
                                    DynamicShaders.color(colors[1]),
                                ),
                            backgroundShader =
                                TopBottomShader(
                                    DynamicShaders.composeShader(
                                        DynamicShaders.fromComponent(
                                            componentSize = 6.dp,
                                            component =
                                                ShapeComponent(
                                                    shape = Shapes.pillShape,
                                                    color = colors[0].toArgb(),
                                                    margins = dimensionsOf(1.dp),
                                                ),
                                        ),
                                        verticalGradient(arrayOf(Color.Black, Color.Transparent)),
                                        PorterDuff.Mode.DST_IN,
                                    ),
                                    DynamicShaders.composeShader(
                                        DynamicShaders.fromComponent(
                                            componentSize = 5.dp,
                                            component =
                                                ShapeComponent(
                                                    shape = Shapes.rectShape,
                                                    color = colors[1].toArgb(),
                                                    margins = dimensionsOf(horizontal = 2.dp),
                                                ),
                                            checkeredArrangement = false,
                                        ),
                                        verticalGradient(arrayOf(Color.Transparent, Color.Black)),
                                        PorterDuff.Mode.DST_IN,
                                    ),
                                ),
                        ),
                    )
            }
        }
    }
}

private val chartColors
    @ReadOnlyComposable
    @Composable
    get() =
        listOf(
            colorResource(id = R.color.chart_9_color_positive),
            colorResource(id = R.color.chart_9_color_negative),
        )
