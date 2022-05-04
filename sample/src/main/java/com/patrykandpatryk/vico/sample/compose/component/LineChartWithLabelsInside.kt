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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.axisLabelComponent
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.line.lineChart
import com.patrykandpatryk.vico.compose.chart.line.lineSpec
import com.patrykandpatryk.vico.compose.component.shapeComponent
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.sample.util.Tokens

@Composable
internal fun LineChartWithLabelsInside(
    modifier: Modifier = Modifier,
    chartEntryModelProducer: ChartEntryModelProducer,
) {
    val startAxis = startAxis(
        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
        label = axisLabelComponent(
            verticalMargin = Tokens.LineChartWithLabelsInside.LABEL_VERTICAL_MARGIN_DP.dp,
            verticalPadding = Tokens.LineChartWithLabelsInside.LABEL_VERTICAL_PADDING_DP.dp,
            horizontalPadding = Tokens.LineChartWithLabelsInside.LABEL_HORIZONTAL_PADDING_DP.dp,
            color = MaterialTheme.colorScheme.onSecondary,
            background = shapeComponent(
                shape = Shapes.pillShape,
                color = MaterialTheme.colorScheme.secondary,
            ),
        ),
    )

    Chart(
        modifier = modifier,
        chart = lineChart(
            lines = listOf(
                lineSpec(lineBackgroundShader = null),
            ),
        ),
        chartModelProducer = chartEntryModelProducer,
        marker = marker(),
        startAxis = startAxis,
        bottomAxis = bottomAxis(),
    )
}
