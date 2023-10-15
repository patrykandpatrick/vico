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

package com.patrykandpatrick.vico.core.chart

import android.graphics.Paint
import com.patrykandpatrick.vico.core.chart.fill.FillStyle
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.formatter.ValueFormatter

/**
 * Creates a new [LineChart.LineSpec] based on this one, updating select properties.
 */
@Deprecated(
    message = "`lineColor` and `lineBackgroundShader` are deprecated. Use `lineFill` and `lineBackgroundFill`",
    replaceWith = ReplaceWith(
        "copy(lineFill = lineFill, lineBackgroundFill = lineBackgroundFill)",
        "com.patrykandpatrick.vico.core.chart.fill.FillStyle",
    ),
)
public fun LineChart.LineSpec.copy(
    lineColor: Int,
    lineThicknessDp: Float = this.lineThicknessDp,
    lineBackgroundShader: DynamicShader? = this.lineBackgroundShader,
    lineCap: Paint.Cap = this.lineCap,
    point: Component? = this.point,
    pointSizeDp: Float = this.pointSizeDp,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelVerticalPosition: VerticalPosition = this.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    pointConnector: LineChart.LineSpec.PointConnector = this.pointConnector,
): LineChart.LineSpec = LineChart.LineSpec(
    lineColor = lineColor,
    lineThicknessDp = lineThicknessDp,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap,
    point = point,
    pointSizeDp = pointSizeDp,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)

/**
 * Creates a new [LineChart.LineSpec] based on this one, updating select properties.
 */
@Deprecated(
    message = "`lineColor` and `lineBackgroundShader` are deprecated. Use `lineFill` and `lineBackgroundFill`",
    replaceWith = ReplaceWith(
        "copy(lineFill = lineFill, lineBackgroundFill = lineBackgroundFill)",
        "com.patrykandpatrick.vico.core.chart.fill.FillStyle",
    ),
)
public fun LineChart.LineSpec.copy(
    lineThicknessDp: Float = this.lineThicknessDp,
    lineBackgroundShader: DynamicShader?,
    lineCap: Paint.Cap = this.lineCap,
    point: Component? = this.point,
    pointSizeDp: Float = this.pointSizeDp,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelVerticalPosition: VerticalPosition = this.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    pointConnector: LineChart.LineSpec.PointConnector = this.pointConnector,
): LineChart.LineSpec = LineChart.LineSpec(
    lineColor = lineColor,
    lineThicknessDp = lineThicknessDp,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap,
    point = point,
    pointSizeDp = pointSizeDp,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)

/**
 * Creates a new [LineChart.LineSpec] based on this one, updating select properties.
 */
public fun LineChart.LineSpec.copy(
    lineFill: FillStyle = this.lineFill,
    lineThicknessDp: Float = this.lineThicknessDp,
    lineBackgroundFill: FillStyle? = this.lineBackgroundFill,
    lineCap: Paint.Cap = this.lineCap,
    point: Component? = this.point,
    pointSizeDp: Float = this.pointSizeDp,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelVerticalPosition: VerticalPosition = this.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    pointConnector: LineChart.LineSpec.PointConnector = this.pointConnector,
): LineChart.LineSpec = LineChart.LineSpec(
    lineFill = lineFill,
    lineThicknessDp = lineThicknessDp,
    lineBackgroundFill = lineBackgroundFill,
    lineCap = lineCap,
    point = point,
    pointSizeDp = pointSizeDp,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)
