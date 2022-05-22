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

package com.patrykandpatryk.vico.sample.chart.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatryk.vico.compose.axis.vertical.startAxis
import com.patrykandpatryk.vico.compose.chart.Chart
import com.patrykandpatryk.vico.compose.chart.column.columnChart
import com.patrykandpatryk.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatryk.vico.compose.style.ChartStyle
import com.patrykandpatryk.vico.compose.style.ProvideChartStyle
import com.patrykandpatryk.vico.core.axis.formatter.PercentageFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.sample.extension.fromEntityColors
import com.patrykandpatryk.vico.sample.util.SampleChartTokens
import com.patrykandpatryk.vico.sample.util.marker
import com.patrykandpatryk.vico.compose.component.shape.textComponent
import com.patrykandpatryk.vico.compose.dimensions.dimensionsOf
import com.patrykandpatryk.vico.core.component.shape.Shapes

@Composable
internal fun ComposeColumnChart(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val tokens = SampleChartTokens.ColumnChart
    val startAxis = startAxis(maxLabelCount = 5, valueFormatter = PercentageFormatAxisValueFormatter())
    val chartStyle = ChartStyle.fromEntityColors(entityColors = tokens.entityColors)
    val decorations = listOf(
        ThresholdLine(
            thresholdValue = tokens.THRESHOLD_VALUE,
            labelComponent = textComponent(
                color = MaterialTheme.colorScheme.surface,
                padding = dimensionsOf(all = tokens.THRESHOLD_LINE_PADDING_DP.dp),
                margins = dimensionsOf(all = tokens.THRESHOLD_LINE_MARGINS_DP.dp),
                background = ShapeComponent(
                    shape = Shapes.roundedCornerShape(all = 4.dp),
                    color = tokens.THRESHOLD_LINE_COLOR.toInt(),
                ),
            ),
            lineComponent = ShapeComponent(
                strokeColor = tokens.THRESHOLD_LINE_COLOR.toInt(),
                strokeWidthDp = tokens.THRESHOLD_LINE_STROKE_WIDTH_DP,
            )
        )
    )
    ProvideChartStyle(chartStyle = chartStyle) {
        val columnChart = columnChart(decorations = decorations)
        Chart(
            modifier = modifier,
            chart = columnChart,
            chartModelProducer = chartEntryModelProducer,
            startAxis = startAxis,
            bottomAxis = bottomAxis(),
            marker = marker(),
        )
    }
}
