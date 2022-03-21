/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.compose.chart.column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.vico.compose.style.ChartStyle
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.chart.column.ColumnChart
import pl.patrykgoworowski.vico.core.chart.column.ColumnChart.MergeMode
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel

/**
 * Creates a [ColumnChart].
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
 * @param spacing the horizontal padding between the edges of chart segments and the columns they contain.
 * @param innerSpacing the spacing between the columns contained in chart segments. This has no effect on
 * segments that contain a single column only.
 * @param mergeMode defines the way multiple columns are rendered in the [ColumnChart].
 * @param minX the minimum value shown on the x-axis. If not null, it overrides [ChartEntryModel.minX].
 * @param maxX the maximum value shown on the x-axis. If not null, it overrides [ChartEntryModel.maxX].
 * @param minY the minimum value shown on the y-axis. If not null, it overrides [ChartEntryModel.minY].
 * @param maxY the maximum value shown on the y-axis. If not null, it overrides [ChartEntryModel.maxY].
 *
 * @see pl.patrykgoworowski.vico.compose.chart.Chart
 * @see ColumnChart
 */
@Composable
public fun columnChart(
    columns: List<LineComponent> = currentChartStyle.columnChart.columns,
    spacing: Dp = currentChartStyle.columnChart.outsideSpacing,
    innerSpacing: Dp = currentChartStyle.columnChart.innerSpacing,
    mergeMode: MergeMode = currentChartStyle.columnChart.mergeMode,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): ColumnChart = remember { ColumnChart() }.apply {
    this.columns = columns
    this.spacingDp = spacing.value
    this.innerSpacingDp = innerSpacing.value
    this.mergeMode = mergeMode
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
}

/**
 * Creates a [ColumnChart] that uses the style provided by [chartStyle].
 *
 * @param chartStyle defines the style of this [ColumnChart].
 * @param minX the minimum value shown on the x-axis. If not null, it overrides [ChartEntryModel.minX].
 * @param maxX the maximum value shown on the x-axis. If not null, it overrides [ChartEntryModel.maxX].
 * @param minY the minimum value shown on the y-axis. If not null, it overrides [ChartEntryModel.minY].
 * @param maxY the maximum value shown on the y-axis. If not null, it overrides [ChartEntryModel.maxY].
 *
 * @see pl.patrykgoworowski.vico.compose.chart.Chart
 * @see ColumnChart
 */
public fun columnChart(
    chartStyle: ChartStyle,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): ColumnChart = ColumnChart(
    columns = chartStyle.columnChart.columns,
    spacingDp = chartStyle.columnChart.outsideSpacing.value,
    innerSpacingDp = chartStyle.columnChart.innerSpacing.value,
    mergeMode = chartStyle.columnChart.mergeMode,
).apply {
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
}
