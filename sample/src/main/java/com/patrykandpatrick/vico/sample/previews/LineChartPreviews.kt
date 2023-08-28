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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryModelOf

private val model1 = entryModelOf(0, 2, 4, 0, 2)
private val model2 = entryModelOf(1, 3, 4, 1, 3)
private val model3 = entryModelOf(entriesOf(3, 2, 2, 3, 1), entriesOf(1, 3, 1, 2, 3))

@Preview("Line Chart Dark", widthDp = 200)
@Composable
public fun LineChartDark() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black,
    ) {
        val yellow = Color(0xFFFFAA4A)
        val pink = Color(0xFFFF4AAA)

        Chart(
            modifier = Modifier.padding(8.dp),
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineColor = yellow,
                        lineBackgroundShader = verticalGradient(
                            arrayOf(yellow.copy(0.5f), yellow.copy(alpha = 0f)),
                        ),
                    ),
                    lineSpec(
                        lineColor = pink,
                        lineBackgroundShader = verticalGradient(
                            arrayOf(pink.copy(0.5f), pink.copy(alpha = 0f)),
                        ),
                    ),
                ),
                axisValuesOverrider = AxisValuesOverrider.fixed(maxY = 4f),
            ),
            model = model3,
        )
    }
}

@Preview("Line Chart", widthDp = 200)
@Composable
public fun RegularLineChart() {
    Chart(
        chart = lineChart(),
        model = model1,
        startAxis = rememberStartAxis(),
    )
}

@Preview("Line Chart Expanded", widthDp = 200)
@Composable
public fun RegularLineChartExpanded() {
    Chart(
        chart = lineChart(
            axisValuesOverrider = AxisValuesOverrider.fixed(
                minY = -1f,
                maxY = 5f,
            ),
        ),
        model = model1,
        startAxis = rememberStartAxis(),
    )
}

@Preview("Line Chart Collapsed", widthDp = 200)
@Composable
public fun RegularLineChartCollapsed() {
    Chart(
        chart = lineChart(
            axisValuesOverrider = AxisValuesOverrider.fixed(
                minY = 1f,
                maxY = 3f,
            ),
        ),
        model = model1,
        startAxis = rememberStartAxis(),
    )
}

@Preview("Composed Chart", widthDp = 200)
@Composable
public fun ComposedLineChart() {
    Chart(
        chart = lineChart() + lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = Color.Blue,
                    lineBackgroundShader = verticalGradient(
                        colors = arrayOf(
                            Color.Blue.copy(alpha = 0.4f),
                            Color.Blue.copy(alpha = 0f),
                        ),
                    ),
                ),
            ),
        ),
        model = model1 + model2,
        startAxis = rememberStartAxis(),
    )
}

@Preview("Composed Chart Collapsed", widthDp = 200)
@Composable
public fun ComposedLineChartCollapsed() {
    Chart(
        chart = lineChart(
            axisValuesOverrider = AxisValuesOverrider.fixed(
                minY = 1f,
                maxY = 3f,
            ),
        ) + lineChart(
            axisValuesOverrider = AxisValuesOverrider.fixed(
                minY = 1f,
                maxY = 3f,
            ),
        ),
        model = model1 + model2,
        startAxis = rememberStartAxis(),
    )
}
