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

package com.patrykandpatrick.vico.compose.axis.vertical

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.axisTickComponent
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DEF_LABEL_COUNT
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.axis.vertical.createVerticalAxis
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent

/**
 * Creates and remembers a start axis (i.e., a [VerticalAxis] with [AxisPosition.Vertical.Start]).
 *
 * @param label the [TextComponent] to use for the labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for the ticks.
 * @param tickLength the length of the ticks.
 * @param guideline the [LineComponent] to use for the guidelines.
 * @param valueFormatter formats the labels.
 * @param sizeConstraint defines how the [VerticalAxis] is to size itself.
 * @param horizontalLabelPosition the horizontal position of the labels.
 * @param verticalLabelPosition the vertical position of the labels.
 * @param itemPlacer determines for what _y_ values the [VerticalAxis] is to display labels, ticks, and guidelines.
 * @param labelRotationDegrees the rotation of the axis labels (in degrees).
 * @param titleComponent an optional [TextComponent] to use as the axis title.
 * @param title the axis title.
 */
@Composable
public fun rememberStartAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Vertical.Start> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    horizontalLabelPosition: VerticalAxis.HorizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition = VerticalAxis.VerticalLabelPosition.Center,
    itemPlacer: AxisItemPlacer.Vertical = remember { AxisItemPlacer.Vertical.default() },
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
): VerticalAxis<AxisPosition.Vertical.Start> = remember { createVerticalAxis<AxisPosition.Vertical.Start>() }.apply {
    this.label = label
    axisLine = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    tickLengthDp = tickLength.value
    this.sizeConstraint = sizeConstraint
    this.horizontalLabelPosition = horizontalLabelPosition
    this.verticalLabelPosition = verticalLabelPosition
    this.itemPlacer = itemPlacer
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
}

/**
 * Creates and remembers an end axis (i.e., a [VerticalAxis] with [AxisPosition.Vertical.End]).
 *
 * @param label the [TextComponent] to use for the labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for the ticks.
 * @param tickLength the length of the ticks.
 * @param guideline the [LineComponent] to use for the guidelines.
 * @param valueFormatter formats the labels.
 * @param sizeConstraint defines how the [VerticalAxis] is to size itself.
 * @param horizontalLabelPosition the horizontal position of the labels.
 * @param verticalLabelPosition the vertical position of the labels.
 * @param itemPlacer determines for what _y_ values the [VerticalAxis] is to display labels, ticks, and guidelines.
 * @param labelRotationDegrees the rotation of the axis labels (in degrees).
 * @param titleComponent an optional [TextComponent] to use as the axis title.
 * @param title the axis title.
 */
@Composable
public fun rememberEndAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Vertical.End> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    horizontalLabelPosition: VerticalAxis.HorizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition = VerticalAxis.VerticalLabelPosition.Center,
    itemPlacer: AxisItemPlacer.Vertical = remember { AxisItemPlacer.Vertical.default() },
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
): VerticalAxis<AxisPosition.Vertical.End> = remember { createVerticalAxis<AxisPosition.Vertical.End>() }.apply {
    this.label = label
    axisLine = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    tickLengthDp = tickLength.value
    this.sizeConstraint = sizeConstraint
    this.horizontalLabelPosition = horizontalLabelPosition
    this.verticalLabelPosition = verticalLabelPosition
    this.itemPlacer = itemPlacer
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
}

/**
 * Creates and remembers a start axis (i.e., a [VerticalAxis] with [AxisPosition.Vertical.Start]).
 *
 * @param label the [TextComponent] to use for the labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for the ticks.
 * @param tickLength the length of the ticks.
 * @param guideline the [LineComponent] to use for the guidelines.
 * @param valueFormatter formats the labels.
 * @param sizeConstraint defines how the [VerticalAxis] is to size itself.
 * @param horizontalLabelPosition the horizontal position of the labels.
 * @param verticalLabelPosition the vertical position of the labels.
 * @param maxLabelCount the maximum label count.
 * @param labelRotationDegrees the rotation of the axis labels (in degrees).
 * @param titleComponent an optional [TextComponent] to use as the axis title.
 * @param title the axis title.
 */
@Composable
@Deprecated(
    """
        `startAxis` is being replaced by `rememberStartAxis`. Also, `maxLabelCount` is being replaced by
        `AxisItemPlacer.Vertical`. If using `maxLabelCount`, create a base `AxisItemPlacer.Vertical` implementation with
        the desired maximum item count via `AxisItemPlacer.Vertical.default`, and use the `itemPlacer` parameter of
        `rememberStartAxis` to apply it to the `VerticalAxis` being created.
    """,
)
public fun startAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Vertical.Start> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    horizontalLabelPosition: VerticalAxis.HorizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition = VerticalAxis.VerticalLabelPosition.Center,
    maxLabelCount: Int = DEF_LABEL_COUNT,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
): VerticalAxis<AxisPosition.Vertical.Start> = rememberStartAxis(
    label,
    axis,
    tick,
    tickLength,
    guideline,
    valueFormatter,
    sizeConstraint,
    horizontalLabelPosition,
    verticalLabelPosition,
    remember { AxisItemPlacer.Vertical.default(maxLabelCount) },
    labelRotationDegrees,
    titleComponent,
    title,
)

/**
 * Creates and remembers an end axis (i.e., a [VerticalAxis] with [AxisPosition.Vertical.End]).
 *
 * @param label the [TextComponent] to use for the labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for the ticks.
 * @param tickLength the length of the ticks.
 * @param guideline the [LineComponent] to use for the guidelines.
 * @param valueFormatter formats the labels.
 * @param sizeConstraint defines how the [VerticalAxis] is to size itself.
 * @param horizontalLabelPosition the horizontal position of the labels.
 * @param verticalLabelPosition the vertical position of the labels.
 * @param maxLabelCount the maximum label count.
 * @param labelRotationDegrees the rotation of the axis labels (in degrees).
 * @param titleComponent an optional [TextComponent] to use as the axis title.
 * @param title the axis title.
 */
@Composable
@Deprecated(
    """
        `endAxis` is being replaced by `rememberEndAxis`. Also, `maxLabelCount` is being replaced by
        `AxisItemPlacer.Vertical`. If using `maxLabelCount`, create a base `AxisItemPlacer.Vertical` implementation with
        the desired maximum item count via `AxisItemPlacer.Vertical.default`, and use the `itemPlacer` parameter of
        `rememberEndAxis` to apply it to the `VerticalAxis` being created.
    """,
)
public fun endAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Vertical.End> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    horizontalLabelPosition: VerticalAxis.HorizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition = VerticalAxis.VerticalLabelPosition.Center,
    maxLabelCount: Int = DEF_LABEL_COUNT,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
): VerticalAxis<AxisPosition.Vertical.End> = rememberEndAxis(
    label,
    axis,
    tick,
    tickLength,
    guideline,
    valueFormatter,
    sizeConstraint,
    horizontalLabelPosition,
    verticalLabelPosition,
    remember { AxisItemPlacer.Vertical.default(maxLabelCount) },
    labelRotationDegrees,
    titleComponent,
    title,
)
