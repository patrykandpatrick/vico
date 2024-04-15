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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Dimensions
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.component
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.shape.toVicoShape
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.core.common.shape.Shape.Companion.Pill
import com.patrykandpatrick.vico.core.common.shape.Shape.Companion.Rectangle
import com.patrykandpatrick.vico.sample.VicoTheme

private val chartModifier = Modifier.height(100.dp)

@Preview("Sample Card With Column Chart", widthDp = 200)
@Composable
fun ColumnChartCard(): Unit =
    VicoTheme {
        val colors = MaterialTheme.colorScheme

        SampleCard {
            CartesianChartHost(
                modifier = chartModifier,
                chart =
                    rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(
                                    color = colors.primary,
                                    thickness = 8.dp,
                                    shape = RoundedCornerShape(4.dp).toVicoShape(),
                                    dynamicShader =
                                        DynamicShader.verticalGradient(arrayOf(colors.primary, colors.secondary)),
                                ),
                            ),
                        ),
                        startAxis =
                            rememberStartAxis(
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
                                        padding = Dimensions(end = 8.dp, start = 4.dp),
                                    ),
                                axis = null,
                                tick = null,
                                guideline = rememberLineComponent(colors.primary.copy(alpha = .1f)),
                            ),
                    ),
                model = CartesianChartModel(ColumnCartesianLayerModel.build { series(1, 2, 3, 2) }),
            )
        }
    }

@Preview("Sample Card With Line Chart", widthDp = 200)
@Composable
fun LineChartCard(): Unit =
    VicoTheme {
        val colors = MaterialTheme.colorScheme

        SampleCard {
            CartesianChartHost(
                modifier = Modifier.height(100.dp),
                chart =
                    rememberCartesianChart(
                        rememberLineCartesianLayer(
                            listOf(
                                rememberLineSpec(
                                    point = null,
                                    shader = DynamicShader.color(colors.primary),
                                    backgroundShader =
                                        DynamicShader.component(
                                            componentSize = 4.dp,
                                            component =
                                                rememberShapeComponent(
                                                    shape = Pill,
                                                    color = colors.primary,
                                                    margins = Dimensions(0.5.dp),
                                                ),
                                        ),
                                ),
                            ),
                            axisValueOverrider = AxisValueOverrider.fixed(minX = 0f, maxY = 3f),
                        ),
                        startAxis =
                            rememberStartAxis(
                                label =
                                    rememberTextComponent(
                                        color = colors.onSurface,
                                        textSize = 10.sp,
                                        background = rememberShapeComponent(shape = Rectangle, color = Color.LightGray),
                                        padding = Dimensions(horizontal = 4.dp, vertical = 2.dp),
                                    ),
                                axis = null,
                                tick = null,
                                guideline =
                                    rememberLineComponent(
                                        color = Color.LightGray,
                                        shape =
                                            DashedShape(
                                                shape = Pill,
                                                dashLengthDp = 2.dp.value,
                                                gapLengthDp = 4.dp.value,
                                            ),
                                    ),
                                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                            ),
                        bottomAxis =
                            rememberBottomAxis(
                                label = null,
                                axis = rememberLineComponent(Color.LightGray),
                                tick = null,
                                guideline = null,
                            ),
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
        elevation = CardDefaults.elevatedCardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            chart()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Title",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "This is a subtitle. It may be long.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
