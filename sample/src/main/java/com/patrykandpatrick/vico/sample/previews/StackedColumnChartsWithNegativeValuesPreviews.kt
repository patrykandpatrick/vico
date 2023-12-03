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
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.chart.values.AxisValueOverrider
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.model.CartesianChartModel
import com.patrykandpatrick.vico.core.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

private val model =
    CartesianChartModel(
        ColumnCartesianLayerModel.build {
            series(2, -1, -4, 2, 1, -5, -2, -3)
            series(3, -2, 2, -1, 2, -3, -4, -1)
            series(1, -2, 2, 1, -1, 4, 4, -2)
        },
    )

private val columns: List<LineComponent>
    @Composable
    get() =
        listOf(
            lineComponent(color = Color(0xFF494949), thickness = 8.dp),
            lineComponent(color = Color(0xFF7C7A7A), thickness = 8.dp),
            lineComponent(color = Color(0xFFFF5D73), thickness = 8.dp),
        )

@Preview
@Composable
public fun StackedColumnChartWithNegativeValues() {
    val marker = rememberMarker()
    Surface {
        CartesianChartHost(
            modifier = Modifier.height(250.dp),
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(columns = columns, mergeMode = ColumnCartesianLayer.MergeMode.Stacked),
                    persistentMarkers = mapOf(2f to marker, 3f to marker),
                ),
            model = model,
            startAxis = rememberStartAxis(itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 8) }),
            bottomAxis = rememberBottomAxis(),
        )
    }
}

@Preview
@Composable
public fun StackedColumnChartWithNegativeValuesAndDataLabels() {
    Surface {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columns = columns,
                        dataLabel = textComponent(),
                        mergeMode = ColumnCartesianLayer.MergeMode.Stacked,
                    ),
                ),
            model = model,
            startAxis = rememberStartAxis(itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 8) }),
            bottomAxis = rememberBottomAxis(),
        )
    }
}

@Preview
@Composable
public fun StackedColumnChartWithNegativeValuesAndAxisValuesOverridden() {
    Surface {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columns = columns,
                        axisValueOverrider = AxisValueOverrider.fixed(minY = 1f, maxY = 4f),
                        mergeMode = ColumnCartesianLayer.MergeMode.Stacked,
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
public fun StackedColumnChartWithNegativeValuesAndAxisValuesOverridden2() {
    Surface {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columns = columns,
                        axisValueOverrider = AxisValueOverrider.fixed(minY = -2f, maxY = 0f),
                        mergeMode = ColumnCartesianLayer.MergeMode.Stacked,
                    ),
                ),
            model = model,
            startAxis = rememberStartAxis(itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 3) }),
            bottomAxis = rememberBottomAxis(),
        )
    }
}
