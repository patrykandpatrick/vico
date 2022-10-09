@file:Suppress("Deprecation")
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

package com.patrykandpatryk.vico.sample.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.entryModelOf

private const val WHITE: Long = 0xFFFFFFFF

private val model = entryModelOf(1, 2, 3, 4, 3)

private val chart: Chart<ChartEntryModel>
    @Composable get() = columnChart(maxX = 6f)

private val axisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { i, _ ->
    "${i.toInt()} Lorem ipsum"
}

@Preview(widthDp = 400, backgroundColor = WHITE, showBackground = true)
@Composable
public fun ColumnChartEdgeTickPosition() {
    Chart(
        chart = chart,
        model = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(
            tickPosition = HorizontalAxis.TickPosition.Edge,
            valueFormatter = axisValueFormatter,
        ),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
    )
}

@Preview(widthDp = 400, backgroundColor = WHITE, showBackground = true)
@Composable
public fun ColumnChartWithCustomEdgeTickPosition() {
    Chart(
        chart = chart,
        model = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(
            tickPosition = HorizontalAxis.TickPosition.Center(offset = 0, spacing = 2),
            valueFormatter = axisValueFormatter,
        ),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
    )
}

@Preview(widthDp = 400, backgroundColor = WHITE, showBackground = true)
@Composable
public fun ColumnChartWithEdgeTickPosition() {
    Chart(
        chart = chart,
        model = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(
            tickPosition = HorizontalAxis.TickPosition.Center(),
            valueFormatter = axisValueFormatter,
        ),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
    )
}

@Preview(widthDp = 400, backgroundColor = WHITE, showBackground = true)
@Composable
public fun ColumnChartWithEdgeTickPositionDeprecated() {
    Chart(
        chart = chart,
        model = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(
            valueFormatter = axisValueFormatter,
        ).apply {
            tickType = HorizontalAxis.TickType.Minor
        },
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
    )
}

@Preview(widthDp = 400, backgroundColor = WHITE, showBackground = true)
@Composable
public fun ColumnChartWithCenterTickPositionDeprecated() {
    Chart(
        modifier = Modifier,
        chart = chart,
        model = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(
            valueFormatter = axisValueFormatter,
        ).apply {
            tickType = HorizontalAxis.TickType.Major
        },
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
    )
}
