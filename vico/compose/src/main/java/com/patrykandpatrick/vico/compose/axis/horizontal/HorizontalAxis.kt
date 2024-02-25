/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.axis.horizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.compose.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.BaseAxis
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.horizontal.createHorizontalAxis
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent

/**
 * Creates and remembers a top axis (i.e., a [HorizontalAxis] with [AxisPosition.Horizontal.Top]).
 *
 * @param label the [TextComponent] to use for the labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for the ticks.
 * @param tickLength the length of the ticks.
 * @param guideline the [LineComponent] to use for the guidelines.
 * @param valueFormatter formats the labels.
 * @param sizeConstraint defines how the [HorizontalAxis] is to size itself.
 * @param labelRotationDegrees the rotation of the axis labels (in degrees).
 * @param titleComponent an optional [TextComponent] to use as the axis title.
 * @param title the axis title.
 * @param itemPlacer determines for what _x_ values the [HorizontalAxis] is to display labels, ticks, and guidelines.
 */
@Composable
public fun rememberTopAxis(
    label: TextComponent? = rememberAxisLabelComponent(),
    axis: LineComponent? = rememberAxisLineComponent(),
    tick: LineComponent? = rememberAxisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = rememberAxisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Top> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: BaseAxis.SizeConstraint = BaseAxis.SizeConstraint.Auto(),
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    itemPlacer: AxisItemPlacer.Horizontal = remember { AxisItemPlacer.Horizontal.default() },
): HorizontalAxis<AxisPosition.Horizontal.Top> =
    remember { createHorizontalAxis<AxisPosition.Horizontal.Top>() }.apply {
        this.label = label
        axisLine = axis
        this.tick = tick
        this.guideline = guideline
        this.valueFormatter = valueFormatter
        tickLengthDp = tickLength.value
        this.sizeConstraint = sizeConstraint
        this.labelRotationDegrees = labelRotationDegrees
        this.titleComponent = titleComponent
        this.title = title
        this.itemPlacer = itemPlacer
    }

/**
 * Creates and remembers a bottom axis (i.e., a [HorizontalAxis] with [AxisPosition.Horizontal.Bottom]).
 *
 * @param label the [TextComponent] to use for the labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for the ticks.
 * @param tickLength the length of the ticks.
 * @param guideline the [LineComponent] to use for the guidelines.
 * @param valueFormatter formats the labels.
 * @param sizeConstraint defines how the [HorizontalAxis] is to size itself.
 * @param titleComponent an optional [TextComponent] to use as the axis title.
 * @param title the axis title.
 * @param labelRotationDegrees the rotation of the axis labels (in degrees).
 * @param itemPlacer determines for what _x_ values the [HorizontalAxis] is to display labels, ticks, and guidelines.
 */
@Composable
public fun rememberBottomAxis(
    label: TextComponent? = rememberAxisLabelComponent(),
    axis: LineComponent? = rememberAxisLineComponent(),
    tick: LineComponent? = rememberAxisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = rememberAxisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Bottom> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: BaseAxis.SizeConstraint = BaseAxis.SizeConstraint.Auto(),
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    itemPlacer: AxisItemPlacer.Horizontal = remember { AxisItemPlacer.Horizontal.default() },
): HorizontalAxis<AxisPosition.Horizontal.Bottom> =
    remember { createHorizontalAxis<AxisPosition.Horizontal.Bottom>() }.apply {
        this.label = label
        axisLine = axis
        this.tick = tick
        this.guideline = guideline
        this.valueFormatter = valueFormatter
        tickLengthDp = tickLength.value
        this.sizeConstraint = sizeConstraint
        this.labelRotationDegrees = labelRotationDegrees
        this.titleComponent = titleComponent
        this.title = title
        this.itemPlacer = itemPlacer
    }
