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

package com.patrykandpatryk.vico.compose.axis.vertical

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.patrykandpatryk.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatryk.vico.compose.axis.axisLabelComponent
import com.patrykandpatryk.vico.compose.axis.axisLineComponent
import com.patrykandpatryk.vico.compose.axis.axisTickComponent
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.DEF_LABEL_COUNT
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.axis.vertical.createVerticalAxis
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.text.TextComponent

/**
 * Creates a start axis.
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its width.
 * @param horizontalLabelPosition the horizontal position of the labels along the axis.
 * @param verticalLabelPosition the vertical position of the labels along the axis.
 * @param maxLabelCount the maximum label count.
 */
@Composable
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
): VerticalAxis<AxisPosition.Vertical.Start> = createVerticalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    tickLengthDp = tickLength.value
    this.sizeConstraint = sizeConstraint
    this.horizontalLabelPosition = horizontalLabelPosition
    this.verticalLabelPosition = verticalLabelPosition
    this.maxLabelCount = maxLabelCount
}

/**
 * Creates an end axis.
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its width.
 * @param horizontalLabelPosition the horizontal position of the labels along the axis.
 * @param verticalLabelPosition the vertical position of the labels along the axis.
 * @param maxLabelCount the maximum label count.
 */
@Composable
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
): VerticalAxis<AxisPosition.Vertical.End> = createVerticalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLengthDp = tickLength.value
    this.sizeConstraint = sizeConstraint
    this.horizontalLabelPosition = horizontalLabelPosition
    this.verticalLabelPosition = verticalLabelPosition
    this.maxLabelCount = maxLabelCount
}
