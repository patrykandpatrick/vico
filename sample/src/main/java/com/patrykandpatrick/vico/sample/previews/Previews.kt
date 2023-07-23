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
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.fromComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.axis.horizontal.createHorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.axis.vertical.createVerticalAxis
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes.pillShape
import com.patrykandpatrick.vico.core.component.shape.Shapes.rectShape
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.sample.utils.VicoTheme

private val chartModifier = Modifier.height(100.dp)

@Preview("Sample Card With Column Chart", widthDp = 200)
@Composable
public fun ColumnChartCard(): Unit = VicoTheme {
    val colors = MaterialTheme.colors

    SampleCard {
        Chart(
            modifier = chartModifier,
            chart = columnChart(
                columns = listOf(
                    lineComponent(
                        colors.primary,
                        thickness = 8.dp,
                        shape = RoundedCornerShape(4.dp),
                        dynamicShader = verticalGradient(arrayOf(colors.primary, colors.secondary)),
                    ),
                ),
            ),
            startAxis = createVerticalAxis {
                label = textComponent(
                    color = colors.primary,
                    textSize = 10.sp,
                    background = shapeComponent(
                        shape = CutCornerShape(
                            CornerSize(percent = 25),
                            CornerSize(percent = 50),
                            CornerSize(percent = 50),
                            CornerSize(percent = 25),
                        ),
                        color = colors.primary.copy(alpha = 0.1f),
                    ),
                    padding = dimensionsOf(end = 8.dp, start = 4.dp),
                )
                axis = null
                tick = null
                guideline = LineComponent(
                    colors.primary.copy(alpha = 0.1f).toArgb(),
                    1.dp.value,
                )
            },
            model = @Suppress("MagicNumber") (entryModelOf(1, 2, 3, 2)),
        )
    }
}

@Preview("Sample Card With Line Chart", widthDp = 200)
@Composable
public fun LineChartCard(): Unit = VicoTheme {
    val colors = MaterialTheme.colors

    SampleCard {
        Chart(
            modifier = Modifier.height(100.dp),
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        point = null,
                        lineColor = colors.primary,
                        lineBackgroundShader = DynamicShaders.fromComponent(
                            componentSize = 4.dp,
                            component = shapeComponent(shape = pillShape, color = colors.primary).apply {
                                setMargins(0.5.dp.value)
                            },
                        ),
                    ),
                ),
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minX = 0f,
                    maxY = 3f,
                ),
            ),
            model = entryModelOf(-1 to 0, 0 to 0, 1 to 1, 2 to 2, 3 to 0, 4 to 2, 5 to 1),
            startAxis = createVerticalAxis {
                label = textComponent(
                    color = colors.onSurface,
                    textSize = 10.sp,
                    background = shapeComponent(shape = rectShape, color = Color.LightGray),
                    padding = dimensionsOf(horizontal = 4.dp, vertical = 2.dp),
                )
                axis = null
                tick = null
                guideline = LineComponent(
                    color = Color.LightGray.toArgb(),
                    thicknessDp = 1.dp.value,
                    shape = DashedShape(
                        shape = pillShape,
                        dashLengthDp = 2.dp.value,
                        gapLengthDp = 4.dp.value,
                    ),
                )
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
            },
            bottomAxis = createHorizontalAxis {
                label = null
                tick = null
                guideline = null
                axis = lineComponent(color = Color.LightGray, thickness = 1.dp)
            },
        )
    }
}

@Composable
private fun SampleCard(
    chart: @Composable ColumnScope.() -> Unit,
) {
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
