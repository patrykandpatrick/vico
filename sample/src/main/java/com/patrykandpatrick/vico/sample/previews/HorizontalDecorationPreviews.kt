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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.decoration.rememberHorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.decoration.rememberHorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.toDynamicShader
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.shader.ComponentShader
import com.patrykandpatrick.vico.core.common.shape.Shape

private val model = CartesianChartModel(ColumnCartesianLayerModel.build { series(1, 2, 3, 4) })

val Color.Companion.DimmedGray: Color
    get() = Color(0xFFAAAAAA)

@Composable
private fun ProvidePreviewVicoTheme(content: @Composable () -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier =
            Modifier
                .background(color = Color.LightGray, shape = RoundedCornerShape(size = 4.dp))
                .padding(8.dp),
    ) {
        ProvideVicoTheme(
            vicoTheme.copy(
                columnCartesianLayerColors = listOf(Color.DimmedGray),
                lineColor = Color.DimmedGray,
                textColor = Color.DimmedGray,
            ),
            content,
        )
    }
}

@Preview(widthDp = 250)
@Composable
fun ThresholdLine() {
    ProvidePreviewVicoTheme {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    decorations =
                        listOf(
                            rememberHorizontalLine(
                                y = { 2f },
                                line = rememberLineComponent(color = Color.Black, thickness = 2.dp),
                                labelComponent =
                                    rememberTextComponent(
                                        Color.Black,
                                        padding = Dimensions.of(horizontal = 8.dp),
                                    ),
                            ),
                        ),
                ),
            model = model,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
fun ThresholdLineWithCustomText() {
    ProvidePreviewVicoTheme {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            rememberHorizontalLine(
                                y = { 2f },
                                line = rememberLineComponent(color = Color.Black, thickness = 2.dp),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.White,
                                        lineCount = 3,
                                        background =
                                            rememberShapeComponent(
                                                shape =
                                                    Shape.rounded(
                                                        bottomLeftPercent = 25,
                                                        bottomRightPercent = 25,
                                                    ),
                                                color = Color.Black,
                                            ),
                                        padding =
                                            Dimensions.of(
                                                start = 8.dp,
                                                top = 2.dp,
                                                end = 8.dp,
                                                bottom = 4.dp,
                                            ),
                                        margins = Dimensions.of(horizontal = 4.dp),
                                    ),
                                label = { "Horizontal line 1 📐" },
                                verticalLabelPosition = VerticalPosition.Bottom,
                            ),
                            rememberHorizontalLine(
                                y = { 3f },
                                line = rememberLineComponent(color = Color.DarkGray, thickness = 2.dp),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.White,
                                        lineCount = 3,
                                        background =
                                            rememberShapeComponent(
                                                shape =
                                                    Shape.cut(
                                                        topLeftPercent = 25,
                                                        topRightPercent = 25,
                                                    ),
                                                color = Color.DarkGray,
                                            ),
                                        padding =
                                            Dimensions.of(
                                                start = 8.dp,
                                                top = 4.dp,
                                                end = 8.dp,
                                                bottom = 2.dp,
                                            ),
                                        margins = Dimensions.of(horizontal = 4.dp),
                                    ),
                                label = { "Horizontal line 2 📐" },
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
fun RangedThresholdLine() {
    ProvidePreviewVicoTheme {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            rememberHorizontalBox(
                                y = { 2f..3f },
                                box = rememberShapeComponent(color = Color.Black.copy(alpha = .5f)),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.Black,
                                        padding = Dimensions.of(horizontal = 8.dp),
                                    ),
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
fun RangedThresholdLineWithBrushShader() {
    ProvidePreviewVicoTheme {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            rememberHorizontalBox(
                                y = { 2f..3f },
                                box =
                                    rememberShapeComponent(
                                        color = Color.Black,
                                        dynamicShader =
                                            Brush.verticalGradient(
                                                colors =
                                                    listOf(
                                                        Color.Black.copy(0.75f),
                                                        Color.Black.copy(0.25f),
                                                    ),
                                            ).toDynamicShader(),
                                    ),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.Black,
                                        padding = Dimensions.of(horizontal = 8.dp),
                                    ),
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
fun RangedThresholdLineWithComponentShader() {
    ProvidePreviewVicoTheme {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            rememberHorizontalBox(
                                y = { 2f..3f },
                                box =
                                    rememberShapeComponent(
                                        color = Color.Black,
                                        dynamicShader =
                                            ComponentShader(
                                                rememberShapeComponent(shape = Shape.Pill, color = Color.Black),
                                                componentSizeDp = 4f,
                                            ),
                                        strokeWidth = 2.dp,
                                        strokeColor = Color.Black,
                                    ),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.Black,
                                        padding = Dimensions.of(horizontal = 8.dp),
                                    ),
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            scrollState = rememberVicoScrollState(scrollEnabled = false),
        )
    }
}
