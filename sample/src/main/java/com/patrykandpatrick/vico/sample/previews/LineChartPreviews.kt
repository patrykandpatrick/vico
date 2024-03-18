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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.model.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.common.shader.DynamicShaders

private val model1 = CartesianChartModel(LineCartesianLayerModel.build { series(0, 2, 4, 0, 2) })

private val model2 =
    CartesianChartModel(
        LineCartesianLayerModel.build { series(0, 2, 4, 0, 2) },
        LineCartesianLayerModel.build { series(1, 3, 4, 1, 3) },
    )

private val model3 =
    CartesianChartModel(
        LineCartesianLayerModel.build {
            series(3, 2, 2, 3, 1)
            series(1, 3, 1, 2, 3)
        },
    )

@Preview("Line Chart Dark", widthDp = 200)
@Composable
fun LineChartDark() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black,
    ) {
        val yellow = Color(0xFFFFAA4A)
        val pink = Color(0xFFFF4AAA)

        CartesianChartHost(
            modifier = Modifier.padding(8.dp),
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        listOf(
                            rememberLineSpec(
                                shader = DynamicShaders.color(yellow),
                                backgroundShader =
                                    DynamicShaders.verticalGradient(
                                        arrayOf(yellow.copy(alpha = 0.5f), yellow.copy(alpha = 0f)),
                                    ),
                            ),
                            rememberLineSpec(
                                shader = DynamicShaders.color(pink),
                                backgroundShader =
                                    DynamicShaders.verticalGradient(
                                        arrayOf(pink.copy(alpha = 0.5f), pink.copy(alpha = 0f)),
                                    ),
                            ),
                        ),
                        axisValueOverrider = AxisValueOverrider.fixed(maxY = 4f),
                    ),
                ),
            model = model3,
        )
    }
}

@Preview("Line Chart", widthDp = 200)
@Composable
fun RegularLineChart() {
    CartesianChartHost(
        chart = rememberCartesianChart(rememberLineCartesianLayer(), startAxis = rememberStartAxis()),
        model = model1,
    )
}

@Preview("Line Chart Expanded", widthDp = 200)
@Composable
fun RegularLineChartExpanded() {
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = -1f, maxY = 5f)),
                startAxis = rememberStartAxis(),
            ),
        model = model1,
    )
}

@Preview("Line Chart Collapsed", widthDp = 200)
@Composable
fun RegularLineChartCollapsed() {
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 3f)),
                startAxis = rememberStartAxis(),
            ),
        model = model1,
    )
}

@Preview("Composed Chart", widthDp = 200)
@Composable
fun ComposedLineChart() {
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                rememberLineCartesianLayer(
                    listOf(
                        rememberLineSpec(
                            shader = DynamicShaders.color(Color.Blue),
                            backgroundShader =
                                DynamicShaders.verticalGradient(
                                    arrayOf(Color.Blue.copy(alpha = 0.4f), Color.Blue.copy(alpha = 0f)),
                                ),
                        ),
                    ),
                ),
                startAxis = rememberStartAxis(),
            ),
        model = model2,
    )
}

@Preview("Composed Chart Collapsed", widthDp = 200)
@Composable
fun ComposedLineChartCollapsed() {
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 3f)),
                rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 3f)),
                startAxis = rememberStartAxis(),
            ),
        model = model2,
    )
}
