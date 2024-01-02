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

package com.patrykandpatrick.vico.sample.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.LocalChartStyle
import com.patrykandpatrick.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.ComponentShader
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.model.ColumnCartesianLayerModel

private val model = CartesianChartModel(ColumnCartesianLayerModel.build { series(1, 2, 3, 4) })

public val Color.Companion.DimmedGray: Color
    get() = Color(0xFFAAAAAA)

@Composable
private fun ProvidePreviewChartStyle(content: @Composable () -> Unit) {
    val chartStyle =
        LocalChartStyle.current.copy(
            axis =
                LocalChartStyle.current.axis.copy(
                    axisLabelColor = Color.DimmedGray,
                    axisLineColor = Color.DimmedGray,
                    axisTickColor = Color.DimmedGray,
                    axisGuidelineColor = Color.DimmedGray,
                ),
            columnLayer =
                LocalChartStyle.current.columnLayer.copy(
                    columns =
                        LocalChartStyle.current.columnLayer.columns.map {
                            rememberLineComponent(
                                color = Color.DimmedGray,
                                thickness = it.thicknessDp.dp,
                                shape = it.shape,
                                dynamicShader = it.dynamicShader,
                                margins = it.margins,
                            )
                        },
                ),
        )
    Surface(
        color = Color.Transparent,
        modifier =
            Modifier
                .background(color = Color.LightGray, shape = RoundedCornerShape(size = 4.dp))
                .padding(8.dp),
    ) {
        CompositionLocalProvider(LocalChartStyle provides chartStyle, content = content)
    }
}

@Preview(widthDp = 250)
@Composable
public fun ThresholdLine() {
    ProvidePreviewChartStyle {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    decorations =
                        listOf(
                            ThresholdLine(
                                thresholdValue = 2f,
                                lineComponent = rememberShapeComponent(color = Color.Black),
                                labelComponent =
                                    rememberTextComponent(Color.Black, padding = dimensionsOf(horizontal = 8.dp)),
                            ),
                        ),
                ),
            model = model,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
public fun ThresholdLineWithCustomText() {
    ProvidePreviewChartStyle {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            ThresholdLine(
                                thresholdValue = 2f,
                                thresholdLabel = "Threshold line 1 📐",
                                lineComponent = rememberShapeComponent(color = Color.Black),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.White,
                                        lineCount = 3,
                                        background =
                                            rememberShapeComponent(
                                                shape =
                                                    Shapes.roundedCornerShape(
                                                        bottomLeftPercent = 25,
                                                        bottomRightPercent = 25,
                                                    ),
                                                color = Color.Black,
                                            ),
                                        padding =
                                            dimensionsOf(
                                                start = 8.dp,
                                                top = 2.dp,
                                                end = 8.dp,
                                                bottom = 4.dp,
                                            ),
                                        margins = dimensionsOf(horizontal = 4.dp),
                                    ),
                                labelVerticalPosition = ThresholdLine.LabelVerticalPosition.Bottom,
                            ),
                            ThresholdLine(
                                thresholdValue = 3f,
                                thresholdLabel = "Threshold line 2 📐",
                                lineComponent = rememberShapeComponent(color = Color.DarkGray),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.White,
                                        lineCount = 3,
                                        background =
                                            rememberShapeComponent(
                                                shape =
                                                    Shapes.cutCornerShape(
                                                        topLeftPercent = 25,
                                                        topRightPercent = 25,
                                                    ),
                                                color = Color.DarkGray,
                                            ),
                                        padding =
                                            dimensionsOf(
                                                start = 8.dp,
                                                top = 4.dp,
                                                end = 8.dp,
                                                bottom = 2.dp,
                                            ),
                                        margins = dimensionsOf(horizontal = 4.dp),
                                    ),
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
public fun RangedThresholdLine() {
    ProvidePreviewChartStyle {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            ThresholdLine(
                                thresholdRange = 2f..3f,
                                lineComponent = rememberShapeComponent(color = Color.Black.copy(alpha = 0.5f)),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.Black,
                                        padding = dimensionsOf(horizontal = 8.dp),
                                    ),
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
public fun RangedThresholdLineWithBrushShader() {
    ProvidePreviewChartStyle {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            ThresholdLine(
                                thresholdRange = 2f..3f,
                                lineComponent =
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
                                        padding = dimensionsOf(horizontal = 8.dp),
                                    ),
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        )
    }
}

@Preview(widthDp = 250)
@Composable
public fun RangedThresholdLineWithComponentShader() {
    ProvidePreviewChartStyle {
        CartesianChartHost(
            modifier = Modifier,
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    decorations =
                        listOf(
                            ThresholdLine(
                                thresholdRange = 2f..3f,
                                lineComponent =
                                    rememberShapeComponent(
                                        color = Color.Black,
                                        dynamicShader =
                                            ComponentShader(
                                                rememberShapeComponent(shape = Shapes.pillShape, color = Color.Black),
                                                componentSizeDp = 4f,
                                            ),
                                        strokeWidth = 2.dp,
                                        strokeColor = Color.Black,
                                    ),
                                labelComponent =
                                    rememberTextComponent(
                                        color = Color.Black,
                                        padding = dimensionsOf(horizontal = 8.dp),
                                    ),
                            ),
                        ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ),
            model = model,
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        )
    }
}
