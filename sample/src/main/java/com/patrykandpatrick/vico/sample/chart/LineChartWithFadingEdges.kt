@file:Suppress("MagicNumber")
/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.chart

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.edges.rememberFadingEdges
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.databinding.LineChartFadingEdgesBinding
import com.patrykandpatrick.vico.sample.extension.fromEntityColors
import com.patrykandpatrick.vico.sample.util.marker

private val axisValuesOverrider = AxisValuesOverrider.adaptiveYValues(1.2f, true)

@Composable
internal fun ComposeLineChartWithFadingEdges(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {

    val startAxis = startAxis(
        horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
        title = "x-axis title",
        titleComponent = textComponent(
            background = shapeComponent(
                shape = Shapes.pillShape,
                color = MaterialTheme.colorScheme.primary,
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            padding = dimensionsOf(horizontal = 8.dp, vertical = 2.dp),
            margins = dimensionsOf(end = 4.dp),
        ),
    )

    val bottomAxis = bottomAxis(
        guideline = null,
        title = "y-axis title",
        titleComponent = textComponent(
            background = shapeComponent(
                shape = Shapes.pillShape,
                color = MaterialTheme.colorScheme.secondary,
            ),
            color = MaterialTheme.colorScheme.onSecondary,
            padding = dimensionsOf(horizontal = 8.dp, vertical = 2.dp),
            margins = dimensionsOf(top = 4.dp),
            typeface = Typeface.SANS_SERIF,
        ),
    )
    val chartStyle = ChartStyle.fromEntityColors(entityColors = entityColors)
    ProvideChartStyle(chartStyle = chartStyle) {
        val lineChart = lineChart(
            axisValuesOverrider = axisValuesOverrider,
            pointPosition = LineChart.PointPosition.Start,
        )
        Chart(
            modifier = modifier,
            chart = lineChart,
            chartModelProducer = chartEntryModelProducer,
            startAxis = startAxis,
            bottomAxis = bottomAxis,
            marker = marker(),
            fadingEdges = rememberFadingEdges(),
        )
    }
}

@Composable
internal fun ViewLineChartWithFadingEdges(
    chartEntryModelProducer: ChartEntryModelProducer,
    modifier: Modifier = Modifier,
) {
    val marker = marker()
    AndroidViewBinding(
        factory = LineChartFadingEdgesBinding::inflate,
        modifier = modifier,
    ) {
        chartView.entryProducer = chartEntryModelProducer
        chartView.marker = marker
        (chartView.chart as LineChart).axisValuesOverrider = axisValuesOverrider
    }
}

@Suppress("MagicNumber")
private val entityColors = longArrayOf(0xFFAA96DA)
