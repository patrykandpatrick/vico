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

import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.lineSpec
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.model.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.model.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.common.shader.DynamicShaders
import com.patrykandpatrick.vico.core.common.shader.TopBottomShader
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

private val model = CartesianChartModel(LineCartesianLayerModel.build { series(-2, -1, 4, -2, 1, 5, -3) })

@Preview
@Composable
public fun SingleLineChartWithNegativeValues() {
    val marker = rememberMarker()
    Surface {
        CartesianChartHost(
            modifier = Modifier.height(250.dp),
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lines =
                            listOf(
                                lineSpec(
                                    shader =
                                        TopBottomShader(
                                            DynamicShaders.color(Color(0xFF25BE53)),
                                            DynamicShaders.color(Color(0xFFE73B3B)),
                                        ),
                                ),
                            ),
                    ),
                    startAxis =
                        rememberStartAxis(
                            itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = { 4 }) },
                            guideline = axisLineComponent(),
                        ),
                    bottomAxis =
                        rememberBottomAxis(
                            guideline = axisLineComponent(),
                            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = 2),
                        ),
                    persistentMarkers = mapOf(2f to marker, 3f to marker),
                ),
            model = model,
            horizontalLayout = HorizontalLayout.fullWidth(),
        )
    }
}

@Preview
@Composable
public fun SingleLineChartWithNegativeValuesAndDataLabels() {
    Surface {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lines =
                            listOf(
                                lineSpec(
                                    shader = DynamicShaders.color(Color.DarkGray),
                                    dataLabel = rememberTextComponent(),
                                ),
                            ),
                    ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
        )
    }
}

@Preview
@Composable
public fun SingleLineChartWithNegativeValuesAndAxisValuesOverridden() {
    Surface {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 4f)),
                    startAxis =
                        rememberStartAxis(
                            itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = { 4 }) },
                        ),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
        )
    }
}

@Preview
@Composable
public fun SingleLineChartWithNegativeValuesAndAxisValuesOverridden2() {
    Surface {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberLineCartesianLayer(axisValueOverrider = AxisValueOverrider.fixed(minY = -2f, maxY = 0f)),
                    startAxis =
                        rememberStartAxis(
                            itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = { 3 }) },
                        ),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
        )
    }
}
