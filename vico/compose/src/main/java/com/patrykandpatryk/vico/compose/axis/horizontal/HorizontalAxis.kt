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

package com.patrykandpatryk.vico.compose.axis.horizontal

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.patrykandpatryk.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatryk.vico.compose.axis.axisLabelComponent
import com.patrykandpatryk.vico.compose.axis.axisLineComponent
import com.patrykandpatryk.vico.compose.axis.axisTickComponent
import com.patrykandpatryk.vico.compose.style.currentChartStyle
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.axis.horizontal.createHorizontalAxis
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.text.TextComponent

/**
 * Creates a top axis.
 *
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param tickPosition defines the position of ticks. [HorizontalAxis.TickPosition.Center] allows for using a custom
 * offset and spacing for both ticks and labels.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its height.
 * @param labelRotationDegrees the rotation of axis labels in degrees.
 * @param titleComponent an optional [TextComponent] use as the axis title.
 * @param title the axis title.
 * @param labelPosition defines the position of labels.
 */
@Composable
public fun topAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    tickPosition: HorizontalAxis.TickPosition = HorizontalAxis.TickPosition.Edge,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Top> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    labelPosition: HorizontalAxis.LabelPosition = HorizontalAxis.LabelPosition.Center,
): HorizontalAxis<AxisPosition.Horizontal.Top> = createHorizontalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLengthDp = tickLength.value
    this.tickPosition = tickPosition
    this.sizeConstraint = sizeConstraint
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
    this.labelPosition = labelPosition
}

/**
 * Creates a bottom axis.
 *
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param tickPosition defines the position of ticks. [HorizontalAxis.TickPosition.Center] allows for using a custom
 * offset and spacing for both ticks and labels.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its height.
 * @param labelRotationDegrees the rotation of axis labels in degrees.
 * @param titleComponent an optional [TextComponent] use as the axis title.
 * @param title the axis title.
 * @param labelPosition defines the position of labels.
 */
@Composable
public fun bottomAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    tickPosition: HorizontalAxis.TickPosition = HorizontalAxis.TickPosition.Edge,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Bottom> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    labelPosition: HorizontalAxis.LabelPosition = HorizontalAxis.LabelPosition.Center,
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = createHorizontalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLengthDp = tickLength.value
    this.tickPosition = tickPosition
    this.sizeConstraint = sizeConstraint
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
    this.labelPosition = labelPosition
}
