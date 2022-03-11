/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.app.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.compose.axis.vertical.startAxis
import pl.patrykgoworowski.vico.compose.chart.Chart
import pl.patrykgoworowski.vico.compose.chart.column.columnChart
import pl.patrykgoworowski.vico.compose.dimensions.dimensionsOf
import pl.patrykgoworowski.vico.compose.component.dimension.setPadding
import pl.patrykgoworowski.vico.compose.component.lineComponent
import pl.patrykgoworowski.vico.compose.component.shapeComponent
import pl.patrykgoworowski.vico.compose.component.shape.shader.toDynamicShader
import pl.patrykgoworowski.vico.compose.component.shape.textComponent
import pl.patrykgoworowski.vico.compose.style.LocalChartStyle
import pl.patrykgoworowski.vico.core.chart.decoration.ThresholdLine
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.shader.ComponentShader
import pl.patrykgoworowski.vico.core.entry.entryModelOf

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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
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

@Preview(showBackground = true)
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
                        textComponent = textComponent(Color.Black).setPadding(horizontal = 8.dp),
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

@Preview(showBackground = true)
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
                        textComponent = textComponent(Color.Black).setPadding(horizontal = 8.dp),
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

@Preview(showBackground = true)
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
                        textComponent = textComponent(Color.Black).setPadding(horizontal = 8.dp),
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
