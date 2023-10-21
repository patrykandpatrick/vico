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

import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.fill.solid
import com.patrykandpatrick.vico.compose.chart.fill.split
import com.patrykandpatrick.vico.compose.chart.layout.fullWidth
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.fill.FillStyle
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

private val model = entryModelOf(-2f, -1f, 4f, -2f, 1f, 5f, -3f)

@Preview
@Composable
public fun SingleLineChartWithNegativeValues() {
    val marker = rememberMarker()
    Surface {
        Chart(
            modifier = Modifier.height(250.dp),
            chart = lineChart(
                lines = listOf(
                    lineSpec(
                        lineFill = FillStyle.split(
                            positiveColor = Color(0xFF25BE53),
                            negativeColor = Color(0xFFE73B3B),
                        ),
                    ),
                ),
                persistentMarkers = mapOf(
                    2f to marker,
                    3f to marker,
                ),
            ),
            model = model,
            startAxis = rememberStartAxis(
                itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 4) },
                guideline = axisLineComponent(),
            ),
            bottomAxis = rememberBottomAxis(
                guideline = axisLineComponent(),
                itemPlacer = AxisItemPlacer.Horizontal.default(
                    spacing = 2,
                ),
            ),
            horizontalLayout = HorizontalLayout.fullWidth(),
        )
    }
}

@Preview
@Composable
public fun SingleLineChartWithNegativeValuesAndDataLabels() {
    Surface {
        Chart(
            chart = lineChart(
                lines = listOf(lineSpec(lineFill = FillStyle.solid(Color.DarkGray), dataLabel = textComponent())),
            ),
            model = model,
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
        )
    }
}

@Preview
@Composable
public fun SingleLineChartWithNegativeValuesAndAxisValuesOverridden() {
    Surface {
        Chart(
            chart = lineChart(
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = 1f,
                    maxY = 4f,
                ),
            ),
            model = model,
            startAxis = rememberStartAxis(itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 4) }),
            bottomAxis = rememberBottomAxis(),
        )
    }
}

@Preview
@Composable
public fun SingleLineChartWithNegativeValuesAndAxisValuesOverridden2() {
    Surface {
        Chart(
            chart = lineChart(
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = -2f,
                    maxY = 0f,
                ),
            ),
            model = model,
            startAxis = rememberStartAxis(itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 3) }),
            bottomAxis = rememberBottomAxis(),
        )
    }
}
