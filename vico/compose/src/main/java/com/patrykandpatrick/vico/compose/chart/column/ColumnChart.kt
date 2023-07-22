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

package com.patrykandpatrick.vico.compose.chart.column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.patrykandpatrick.vico.core.marker.Marker

/**
 * Creates a [ColumnChart].
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
 * @param spacing the horizontal padding between the edges of chart segments and the columns they contain.
 * @param innerSpacing the spacing between the columns contained in chart segments. This has no effect on
 * segments that contain a single column only.
 * @param mergeMode defines how columns should be drawn in multi-column segments.
 * @param decorations the list of [Decoration]s that will be added to the [ColumnChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
 * @param axisValuesOverrider overrides the minimum and maximum x-axis and y-axis values.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 *
 * @see CartesianChartHost
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
 * @param mergeMode defines how columns should be drawn in multi-column segments.
 * @param minX the minimum value shown on the x-axis. If not null, this overrides [ChartEntryModel.minX].
 * @param maxX the maximum value shown on the x-axis. If not null, this overrides [ChartEntryModel.maxX].
 * @param minY the minimum value shown on the y-axis. If not null, this overrides [ChartEntryModel.minY].
 * @param maxY the maximum value shown on the y-axis. If not null, this overrides [ChartEntryModel.maxY].
 * @param decorations the list of [Decoration]s that will be added to the [ColumnChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
 *
 * @see CartesianChartHost
 * @see ColumnChart
 */
@Deprecated(message = "Axis values should be overridden `AxisValuesOverrider`.")
@Suppress("DEPRECATION")
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
