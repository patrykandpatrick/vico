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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition.Vertical
import com.patrykandpatrick.vico.core.axis.AxisPosition.Vertical.End
import com.patrykandpatrick.vico.core.axis.AxisPosition.Vertical.Start
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.composed.plus
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.sample.showcase.rememberMarker

private val model1 = entryModelOf(0 to 1, 1 to 2, 2 to 4, 3 to 1, 4 to 4)
private val model2 = entryModelOf(1 to 4, 2 to 1, 3 to 8, 4 to 12, 5 to 5)

private val markerMap: Map<Float, Marker>
    @Composable get() = mapOf(4f to rememberMarker())

@Composable
private fun getColumnChart(
    markerMap: Map<Float, Marker> = emptyMap(),
    targetVerticalAxisPosition: Vertical? = null,
): ColumnChart = columnChart(
    columns = listOf(
        lineComponent(
            color = Color.Black,
            thickness = 8.dp,
            shape = Shapes.pillShape,
        ),
    ),
    persistentMarkers = markerMap,
    targetVerticalAxisPosition = targetVerticalAxisPosition,
)

@Composable
private fun getLineChart(
    markerMap: Map<Float, Marker> = emptyMap(),
    targetVerticalAxisPosition: Vertical? = null,
): LineChart = lineChart(
    lines = listOf(
        lineSpec(
            lineColor = Color.DarkGray,
            lineBackgroundShader = verticalGradient(
                arrayOf(Color.DarkGray, Color.DarkGray.copy(alpha = 0f)),
            ),
        ),
    ),
    persistentMarkers = markerMap,
    targetVerticalAxisPosition = targetVerticalAxisPosition,
)

private val startAxis: Axis<Start>
    @Composable get() = rememberStartAxis(
        label = textComponent(color = Color.Black),
        itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 5) },
    )

private val endAxis: Axis<End>
    @Composable get() = rememberEndAxis(
        label = textComponent(color = Color.DarkGray),
        itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 7) },
    )

@Composable
@Preview("Chart with independent axes", widthDp = 350)
public fun ChartWithIndependentAxes(modifier: Modifier = Modifier) {
    val composedChart = getColumnChart(targetVerticalAxisPosition = Start) +
        getLineChart(targetVerticalAxisPosition = End)

    composedChart.setPersistentMarkers(markerMap)

    Chart(
        chart = composedChart,
        model = model1 + model2,
        startAxis = startAxis,
        bottomAxis = rememberBottomAxis(),
        endAxis = endAxis,
        modifier = modifier,
    )
}

@Composable
@Preview("Chart with dependent axes", widthDp = 350)
public fun ChartWithDependentAxes(modifier: Modifier = Modifier) {
    val composedChart = getColumnChart() + getLineChart()

    composedChart.setPersistentMarkers(markerMap)

    Chart(
        chart = composedChart,
        model = model1 + model2,
        startAxis = startAxis,
        bottomAxis = rememberBottomAxis(),
        endAxis = endAxis,
        modifier = modifier,
    )
}

@Composable
@Preview("Column chart", widthDp = 350)
public fun ColumnChart(modifier: Modifier = Modifier) {
    Chart(
        chart = getColumnChart(markerMap = markerMap),
        model = model1,
        startAxis = startAxis,
        bottomAxis = rememberBottomAxis(),
        modifier = modifier,
    )
}

@Composable
@Preview("Line chart", widthDp = 350)
public fun LineChart(modifier: Modifier = Modifier) {
    Chart(
        chart = getLineChart(markerMap = markerMap),
        model = model2,
        startAxis = startAxis,
        bottomAxis = rememberBottomAxis(),
        modifier = modifier,
    )
}
