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

package com.patrykandpatryk.vico.compose.chart.column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.column.ColumnChart
import com.patrykandpatryk.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatryk.vico.core.chart.decoration.Decoration
import com.patrykandpatryk.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatryk.vico.core.chart.values.ChartValues
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.text.TextComponent
import com.patrykandpatryk.vico.core.component.text.VerticalPosition
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.formatter.ValueFormatter
import com.patrykandpatryk.vico.core.marker.Marker

/**
 * Creates a [ColumnChart].
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
 * @param spacing the horizontal padding between the edges of chart segments and the columns they contain.
 * @param innerSpacing the spacing between the columns contained in chart segments. This has no effect on
 * segments that contain a single column only.
 * @param mergeMode defines the way multiple columns are rendered in the [ColumnChart].
 * @param decorations the list of [Decoration]s that will be added to the [ColumnChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels in degrees.
 * @param axisValuesOverrider overrides the minimum and maximum x-axis and y-axis values.
 * @param targetVerticalAxisPosition if this is set, any [com.patrykandpatryk.vico.core.axis.AxisRenderer] with an
 * [AxisPosition] equal to the provided value will use the [ChartValues] provided by this chart.
 * This is meant to be used with [com.patrykandpatryk.vico.core.chart.composed.ComposedChart].
 *
 * @see com.patrykandpatryk.vico.compose.chart.Chart
 * @see ColumnChart
 */
@Composable
public fun columnChart(
    columns: List<LineComponent> = currentChartStyle.columnChart.columns,
    spacing: Dp = currentChartStyle.columnChart.outsideSpacing,
    innerSpacing: Dp = currentChartStyle.columnChart.innerSpacing,
    mergeMode: MergeMode = currentChartStyle.columnChart.mergeMode,
    decorations: List<Decoration>? = null,
    persistentMarkers: Map<Float, Marker>? = null,
    targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    dataLabel: TextComponent? = currentChartStyle.columnChart.dataLabel,
    dataLabelVerticalPosition: VerticalPosition = currentChartStyle.columnChart.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = currentChartStyle.columnChart.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = currentChartStyle.columnChart.dataLabelRotationDegrees,
    axisValuesOverrider: AxisValuesOverrider<ChartEntryModel>? = null,
): ColumnChart = remember { ColumnChart() }.apply {
    this.columns = columns
    this.spacingDp = spacing.value
    this.innerSpacingDp = innerSpacing.value
    this.mergeMode = mergeMode
    this.dataLabel = dataLabel
    this.dataLabelVerticalPosition = dataLabelVerticalPosition
    this.dataLabelValueFormatter = dataLabelValueFormatter
    this.dataLabelRotationDegrees = dataLabelRotationDegrees
    this.axisValuesOverrider = axisValuesOverrider
    this.targetVerticalAxisPosition = targetVerticalAxisPosition
    decorations?.also(::setDecorations)
    persistentMarkers?.also(::setPersistentMarkers)
}

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
 * @param decorations the list of [Decoration]s that will be added to the [ColumnChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param targetVerticalAxisPosition if this is set, any [com.patrykandpatryk.vico.core.axis.AxisRenderer] with an
 * [AxisPosition] equal to the provided value will use the [ChartValues] provided by this chart.
 * This is meant to be used with [com.patrykandpatryk.vico.core.chart.composed.ComposedChart].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels in degrees.
 *
 * @see com.patrykandpatryk.vico.compose.chart.Chart
 * @see ColumnChart
 */
@Deprecated(message = "Axis values should be overridden `AxisValuesOverrider`.")
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
    decorations: List<Decoration>? = null,
    persistentMarkers: Map<Float, Marker>? = null,
    targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    dataLabel: TextComponent? = currentChartStyle.columnChart.dataLabel,
    dataLabelVerticalPosition: VerticalPosition = currentChartStyle.columnChart.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = currentChartStyle.columnChart.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = currentChartStyle.columnChart.dataLabelRotationDegrees,
): ColumnChart = remember { ColumnChart() }.apply {
    this.columns = columns
    this.spacingDp = spacing.value
    this.innerSpacingDp = innerSpacing.value
    this.mergeMode = mergeMode
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
    this.targetVerticalAxisPosition = targetVerticalAxisPosition
    this.dataLabel = dataLabel
    this.dataLabelVerticalPosition = dataLabelVerticalPosition
    this.dataLabelValueFormatter = dataLabelValueFormatter
    this.dataLabelRotationDegrees = dataLabelRotationDegrees
    decorations?.also(::setDecorations)
    persistentMarkers?.also(::setPersistentMarkers)
}
