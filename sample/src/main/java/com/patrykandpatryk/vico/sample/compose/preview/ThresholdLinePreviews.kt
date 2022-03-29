/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.sample.compose.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.dimensions.dimensionsOf
import com.patrykandpatryk.vico.compose.component.dimension.setPadding
import com.patrykandpatryk.vico.compose.component.lineComponent
import com.patrykandpatryk.vico.compose.component.shapeComponent
import com.patrykandpatryk.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatryk.vico.compose.component.shape.textComponent
import com.patrykandpatryk.vico.compose.style.LocalChartStyle
import com.patrykandpatryk.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.component.shape.shader.ComponentShader
import com.patrykandpatryk.vico.core.entry.entryModelOf

private val model = entryModelOf(1, 2, 3, 4)

@Composable
private fun ProvidePreviewChartStyle(content: @Composable () -> Unit) {
    val chartStyle = LocalChartStyle.current.copy(
        axis = LocalChartStyle.current.axis.copy(
            axisLabelColor = Color.Gray,
            axisLineColor = Color.Gray,
            axisGuidelineColor = Color.LightGray,
        ),
        columnChart = LocalChartStyle.current.columnChart.copy(
            columns = LocalChartStyle.current.columnChart.columns.map {
                lineComponent(
                    color = Color.Gray,
                    thickness = it.thicknessDp.dp,
                    shape = it.shape,
                    dynamicShader = it.dynamicShader,
                    margins = it.margins,
                )
            }
        )
    )
    CompositionLocalProvider(LocalChartStyle provides chartStyle, content = content)
}

@Preview(showBackground = true, widthDp = 250)
@Composable
public fun ThresholdLine() {
    ProvidePreviewChartStyle {
        Chart(
            modifier = Modifier,
            chart = columnChart().apply {
                addDecoration(
                    ThresholdLine(
                        thresholdValue = 2f,
                        lineComponent = shapeComponent(color = Color.Black),
                        textComponent = textComponent(Color.Black).apply {
                            setPadding(horizontal = 8.dp)
                        },
                    )
                )
            },
            model = model,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(),
            isHorizontalScrollEnabled = false,
        )
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
public fun ThresholdLineWithCustomText() {
    ProvidePreviewChartStyle {
        Chart(
            modifier = Modifier,
            chart = columnChart().apply {
                addDecoration(
                    ThresholdLine(
                        thresholdValue = 2f,
                        thresholdLabel = "Threshold line 1 📐",
                        lineComponent = shapeComponent(color = Color.Black),
                        textComponent = textComponent(
                            color = Color.White,
                            lineCount = 3,
                            background = shapeComponent(
                                shape = Shapes.roundedCornerShape(bottomLeftPercent = 25, bottomRightPercent = 25),
                                color = Color.Black,
                            ),
                            padding = dimensionsOf(
                                start = 8.dp,
                                top = 2.dp,
                                end = 8.dp,
                                bottom = 4.dp,
                            ),
                            margins = dimensionsOf(horizontal = 4.dp),
                        ),
                        labelVerticalPosition = ThresholdLine.LabelVerticalPosition.Bottom
                    )
                )
                addDecoration(
                    ThresholdLine(
                        thresholdValue = 3f,
                        thresholdLabel = "Threshold line 2 📐",
                        lineComponent = shapeComponent(color = Color.DarkGray),
                        textComponent = textComponent(
                            color = Color.White,
                            lineCount = 3,
                            background = shapeComponent(
                                shape = Shapes.cutCornerShape(topLeftPercent = 25, topRightPercent = 25),
                                color = Color.DarkGray,
                            ),
                            padding = dimensionsOf(
                                start = 8.dp,
                                top = 4.dp,
                                end = 8.dp,
                                bottom = 2.dp,
                            ),
                            margins = dimensionsOf(horizontal = 4.dp),
                        ),
                    )
                )
            },
            model = model,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(),
            isHorizontalScrollEnabled = false,
        )
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
public fun RangedThresholdLine() {
    ProvidePreviewChartStyle {
        Chart(
            modifier = Modifier,
            chart = columnChart().apply {
                addDecoration(
                    ThresholdLine(
                        thresholdRange = 2f..3f,
                        lineComponent = shapeComponent(color = Color.Black.copy(alpha = 0.5f)),
                        labelComponent = textComponent(Color.Black).setPadding(horizontal = 8.dp),
                    )
                )
            },
            model = model,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(),
            isHorizontalScrollEnabled = false,
        )
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
public fun RangedThresholdLineWithBrushShader() {
    ProvidePreviewChartStyle {
        Chart(
            modifier = Modifier,
            chart = columnChart().apply {
                addDecoration(
                    ThresholdLine(
                        thresholdRange = 2f..3f,
                        lineComponent = shapeComponent(
                            color = Color.Black,
                            dynamicShader = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(0.75f),
                                    Color.Black.copy(0.25f),
                                )
                            ).toDynamicShader()
                        ),
                        labelComponent = textComponent(Color.Black).setPadding(horizontal = 8.dp),
                    )
                )
            },
            model = model,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(),
            isHorizontalScrollEnabled = false,
        )
    }
}

@Preview(showBackground = true, widthDp = 250)
@Composable
public fun RangedThresholdLineWithComponentShader() {
    ProvidePreviewChartStyle {
        Chart(
            modifier = Modifier,
            chart = columnChart().apply {
                addDecoration(
                    ThresholdLine(
                        thresholdRange = 2f..3f,
                        lineComponent = shapeComponent(
                            color = Color.Black,
                            dynamicShader = ComponentShader(
                                shapeComponent(shape = Shapes.pillShape, color = Color.Black),
                                componentSizeDp = 4f,
                            ),
                            strokeWidth = 2.dp,
                            strokeColor = Color.Black,
                        ),
                        labelComponent = textComponent(Color.Black).setPadding(horizontal = 8.dp),
                    )
                )
            },
            model = model,
            startAxis = startAxis(),
            bottomAxis = bottomAxis(),
            isHorizontalScrollEnabled = false,
        )
    }
}
