/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.previews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.color
import com.patrykandpatrick.vico.compose.component.shape.shader.fromComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.compose.component.shape.toVicoShape
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.horizontal.createHorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.axis.vertical.createVerticalAxis
import com.patrykandpatrick.vico.core.chart.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes.pillShape
import com.patrykandpatrick.vico.core.component.shape.Shapes.rectShape
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.model.LineCartesianLayerModel
import com.patrykandpatrick.vico.sample.VicoTheme

private val chartModifier = Modifier.height(100.dp)

@Preview("Sample Card With Column Chart", widthDp = 200)
@Composable
public fun ColumnChartCard(): Unit =
    VicoTheme {
        val colors = MaterialTheme.colors

        SampleCard {
            CartesianChartHost(
                modifier = chartModifier,
                chart =
                    rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            listOf(
                                rememberLineComponent(
                                    color = colors.primary,
                                    thickness = 8.dp,
                                    shape = RoundedCornerShape(4.dp).toVicoShape(),
                                    dynamicShader =
                                        DynamicShaders.verticalGradient(arrayOf(colors.primary, colors.secondary)),
                                ),
                            ),
                        ),
                        startAxis =
                            createVerticalAxis {
                                label =
                                    rememberTextComponent(
                                        color = colors.primary,
                                        textSize = 10.sp,
                                        background =
                                            rememberShapeComponent(
                                                shape =
                                                    CutCornerShape(
                                                        CornerSize(percent = 25),
                                                        CornerSize(percent = 50),
                                                        CornerSize(percent = 50),
                                                        CornerSize(percent = 25),
                                                    ).toVicoShape(),
                                                color = colors.primary.copy(alpha = 0.1f),
                                            ),
                                        padding = dimensionsOf(end = 8.dp, start = 4.dp),
                                    )
                                axis = null
                                tick = null
                                guideline =
                                    LineComponent(
                                        colors.primary.copy(alpha = 0.1f).toArgb(),
                                        1.dp.value,
                                    )
                            },
                    ),
                model = CartesianChartModel(ColumnCartesianLayerModel.build { series(1, 2, 3, 2) }),
            )
        }
    }

@Preview("Sample Card With Line Chart", widthDp = 200)
@Composable
public fun LineChartCard(): Unit =
    VicoTheme {
        val colors = MaterialTheme.colors

        SampleCard {
            CartesianChartHost(
                modifier = Modifier.height(100.dp),
                chart =
                    rememberCartesianChart(
                        rememberLineCartesianLayer(
                            listOf(
                                rememberLineSpec(
                                    point = null,
                                    shader = DynamicShaders.color(colors.primary),
                                    backgroundShader =
                                        DynamicShaders.fromComponent(
                                            componentSize = 4.dp,
                                            component =
                                                rememberShapeComponent(shape = pillShape, color = colors.primary)
                                                    .apply { setMargins(0.5.dp.value) },
                                        ),
                                ),
                            ),
                            axisValueOverrider = AxisValueOverrider.fixed(minX = 0f, maxY = 3f),
                        ),
                        startAxis =
                            createVerticalAxis {
                                label =
                                    rememberTextComponent(
                                        color = colors.onSurface,
                                        textSize = 10.sp,
                                        background = rememberShapeComponent(shape = rectShape, color = Color.LightGray),
                                        padding = dimensionsOf(horizontal = 4.dp, vertical = 2.dp),
                                    )
                                axis = null
                                tick = null
                                guideline =
                                    LineComponent(
                                        color = Color.LightGray.toArgb(),
                                        thicknessDp = 1.dp.value,
                                        shape =
                                            DashedShape(
                                                shape = pillShape,
                                                dashLengthDp = 2.dp.value,
                                                gapLengthDp = 4.dp.value,
                                            ),
                                    )
                                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
                            },
                        bottomAxis =
                            createHorizontalAxis {
                                label = null
                                tick = null
                                guideline = null
                                axis = rememberLineComponent(color = Color.LightGray, thickness = 1.dp)
                            },
                    ),
                model =
                    CartesianChartModel(
                        LineCartesianLayerModel.build {
                            series(x = listOf(-1, 0, 1, 2, 3, 4, 5), y = listOf(0, 0, 1, 2, 0, 2, 1))
                        },
                    ),
            )
        }
    }

@Composable
private fun SampleCard(chart: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            chart()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Title",
                style = MaterialTheme.typography.h6,
            )
            Text(
                text = "This is a subtitle. It may be long.",
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}
