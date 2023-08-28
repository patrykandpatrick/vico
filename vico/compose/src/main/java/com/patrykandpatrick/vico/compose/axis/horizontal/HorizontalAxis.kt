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

package com.patrykandpatrick.vico.compose.axis.horizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.axisTickComponent
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.horizontal.createHorizontalAxis
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
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
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Top> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
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
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Bottom> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
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
@Deprecated(
    "Use `rememberTopAxis` instead.",
    ReplaceWith(
        """
            rememberTopAxis(
                label = label,
                axis = axis,
                tick = tick,
                tickLength = tickLength,
                guideline = guideline,
                valueFormatter = valueFormatter,
                sizeConstraint = sizeConstraint,
                labelRotationDegrees = labelRotationDegrees,
                titleComponent = titleComponent,
                title = title,
                itemPlacer = itemPlacer,
            )
        """,
        "com.patrykandpatrick.vico.compose.axis.horizontal.rememberTopAxis",
    ),
)
public fun topAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Top> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    itemPlacer: AxisItemPlacer.Horizontal = remember { AxisItemPlacer.Horizontal.default() },
): HorizontalAxis<AxisPosition.Horizontal.Top> = rememberTopAxis(
    label,
    axis,
    tick,
    tickLength,
    guideline,
    valueFormatter,
    sizeConstraint,
    labelRotationDegrees,
    titleComponent,
    title,
    itemPlacer,
)

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
@Deprecated(
    "Use `rememberBottomAxis` instead.",
    ReplaceWith(
        """
            rememberBottomAxis(
                label = label,
                axis = axis,
                tick = tick,
                tickLength = tickLength,
                guideline = guideline,
                valueFormatter = valueFormatter,
                sizeConstraint = sizeConstraint,
                titleComponent = titleComponent,
                title = title,
                labelRotationDegrees = labelRotationDegrees,
                itemPlacer = itemPlacer,
            )
        """,
        "com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis",
    ),
)
public fun bottomAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Bottom> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    itemPlacer: AxisItemPlacer.Horizontal = remember { AxisItemPlacer.Horizontal.default() },
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = rememberBottomAxis(
    label,
    axis,
    tick,
    tickLength,
    guideline,
    valueFormatter,
    sizeConstraint,
    titleComponent,
    title,
    labelRotationDegrees,
    itemPlacer,
)

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
 * @param labelSpacing how often labels (and their corresponding ticks and guidelines) should be drawn.
 * @param labelOffset the number of labels (and, for [HorizontalLayout.FullWidth], their corresponding ticks and
 * guidelines) to skip from the start.
 */
@Composable
@Deprecated(
    """
        `topAxis` is being replaced by `rememberTopAxis`. Also, `labelSpacing` and `labelOffset` are being replaced by
        `AxisItemPlacer.Horizontal`. Create a base `AxisItemPlacer.Horizontal` implementation with the desired spacing
        and offset via `AxisItemPlacer.Horizontal.default`, and use the `itemPlacer` parameter of `rememberTopAxis` to
        apply it to the `HorizontalAxis` being created.
    """,
    ReplaceWith(
        """
            rememberTopAxis(
                label = label,
                axis = axis,
                tick = tick,
                tickLength = tickLength,
                guideline = guideline,
                valueFormatter = valueFormatter,
                sizeConstraint = sizeConstraint,
                labelRotationDegrees = labelRotationDegrees,
                titleComponent = titleComponent,
                title = title,
                itemPlacer = remember {
                    AxisItemPlacer.Horizontal.default(spacing = labelSpacing, offset = labelOffset)
                },
            )
        """,
        "androidx.compose.runtime.remember",
        "com.patrykandpatrick.vico.compose.axis.horizontal.rememberTopAxis",
        "com.patrykandpatrick.vico.core.axis.AxisItemPlacer",
    ),
)
public fun topAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Top> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    labelSpacing: Int = 1,
    labelOffset: Int = 0,
): HorizontalAxis<AxisPosition.Horizontal.Top> = rememberTopAxis(
    label,
    axis,
    tick,
    tickLength,
    guideline,
    valueFormatter,
    sizeConstraint,
    labelRotationDegrees,
    titleComponent,
    title,
    remember { AxisItemPlacer.Horizontal.default(labelSpacing, labelOffset) },
)

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
 * @param labelSpacing how often labels (and their corresponding ticks and guidelines) should be drawn.
 * @param labelOffset the number of labels (and, for [HorizontalLayout.FullWidth], their corresponding ticks and
 * guidelines) to skip from the start.
 */
@Composable
@Deprecated(
    """
        `bottomAxis` is being replaced by `rememberBottomAxis`. Also, `labelSpacing` and `labelOffset` are being
        replaced by `AxisItemPlacer.Horizontal`. Create a base `AxisItemPlacer.Horizontal` implementation with the
        desired spacing and offset via `AxisItemPlacer.Horizontal.default`, and use the `itemPlacer` parameter of
        `rememberBottomAxis` to apply it to the `HorizontalAxis` being created.
    """,
    ReplaceWith(
        """
            rememberBottomAxis(
                label = label,
                axis = axis,
                tick = tick,
                tickLength = tickLength,
                guideline = guideline,
                valueFormatter = valueFormatter,
                sizeConstraint = sizeConstraint,
                titleComponent = titleComponent,
                title = title,
                labelRotationDegrees = labelRotationDegrees,
                itemPlacer = remember {
                    AxisItemPlacer.Horizontal.default(spacing = labelSpacing, offset = labelOffset)
                },
            )
        """,
        "androidx.compose.runtime.remember",
        "com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis",
        "com.patrykandpatrick.vico.core.axis.AxisItemPlacer",
    ),
)
public fun bottomAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Bottom> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    labelSpacing: Int = 1,
    labelOffset: Int = 0,
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = rememberBottomAxis(
    label,
    axis,
    tick,
    tickLength,
    guideline,
    valueFormatter,
    sizeConstraint,
    titleComponent,
    title,
    labelRotationDegrees,
    remember { AxisItemPlacer.Horizontal.default(labelSpacing, labelOffset) },
)
