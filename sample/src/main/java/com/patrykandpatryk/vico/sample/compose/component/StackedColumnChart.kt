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

package com.patrykandpatryk.vico.sample.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatryk.vico.compose.axis.axisLabelComponent
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer

@Composable
internal fun StackedColumnChart(
    modifier: Modifier = Modifier,
    chartEntryModelProducer: ChartEntryModelProducer,
) {
    val axisGuideline = axisGuidelineComponent(shape = Shapes.rectShape)

    val startAxis = startAxis(
        label = axisLabelComponent(rotationDegrees = AXIS_LABEL_ROTATION_DEGREES),
        guideline = axisGuideline,
    )

    val chart = columnChart(
        innerSpacing = 4.dp,
        spacing = 24.dp,
        mergeMode = MergeMode.Stack,
    )

    Chart(
        modifier = modifier,
        chart = chart,
        chartModelProducer = chartEntryModelProducer,
        startAxis = startAxis,
        bottomAxis = bottomAxis(guideline = axisGuideline),
        marker = marker(),
    )
}

private const val AXIS_LABEL_ROTATION_DEGREES = 45f
