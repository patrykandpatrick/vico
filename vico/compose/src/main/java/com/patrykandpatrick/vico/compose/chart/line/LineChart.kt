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

package com.patrykandpatrick.vico.compose.chart.line

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.patrykandpatrick.vico.core.marker.Marker

/**
 * Creates a [LineChart].
 *
 * @param lines the [LineChart.LineSpec]s to use for the lines. This list is iterated through as many times as there
 * are lines.
 * @param decorations the list of [Decoration]s that will be added to the [LineChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param pointPosition the horizontal position of each point in its corresponding segment.
 * @param axisValuesOverrider overrides the minimum and maximum x-axis and y-axis values.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 *
 * @see CartesianChartHost
 * @see ColumnChart
 */
@Composable
public fun lineChart(
    lines: List<LineSpec> = currentChartStyle.lineChart.lines,
    spacing: Dp = currentChartStyle.lineChart.spacing,
    pointPosition: LineChart.PointPosition = LineChart.PointPosition.Center,
    decorations: List<Decoration>? = null,
    persistentMarkers: Map<Float, Marker>? = null,
    axisValuesOverrider: AxisValuesOverrider<ChartEntryModel>? = null,
    targetVerticalAxisPosition: AxisPosition.Vertical? = null,
): LineChart = remember { LineChart() }.apply {
    this.lines = lines
    this.spacingDp = spacing.value
    this.pointPosition = pointPosition
    this.axisValuesOverrider = axisValuesOverrider
    this.targetVerticalAxisPosition = targetVerticalAxisPosition
    decorations?.also(::setDecorations)
    persistentMarkers?.also(::setPersistentMarkers)
}

/**
 * Creates a [LineChart].
 *
 * @param lines the [LineChart.LineSpec]s to use for the lines. This list is iterated through as many times as there
 * are lines.
 * @param minX the minimum value shown on the x-axis. If not null, this overrides [ChartEntryModel.minX].
 * @param maxX the maximum value shown on the x-axis. If not null, this overrides [ChartEntryModel.maxX].
 * @param minY the minimum value shown on the y-axis. If not null, this overrides [ChartEntryModel.minY].
 * @param maxY the maximum value shown on the y-axis. If not null, this overrides [ChartEntryModel.maxY].
 * @param decorations the list of [Decoration]s that will be added to the [LineChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 * @param pointPosition the horizontal position of each point in its corresponding segment.
 *
 * @see CartesianChartHost
 * @see ColumnChart
 */
@Deprecated(message = "Axis values should be overridden via `AxisValuesOverrider`.")
@Suppress("DEPRECATION")
@Composable
public fun lineChart(
    lines: List<LineSpec> = currentChartStyle.lineChart.lines,
    spacing: Dp = currentChartStyle.lineChart.spacing,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
    decorations: List<Decoration>? = null,
    persistentMarkers: Map<Float, Marker>? = null,
    targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    pointPosition: LineChart.PointPosition = LineChart.PointPosition.Center,
): LineChart = remember { LineChart() }.apply {
    this.lines = lines
    this.spacingDp = spacing.value
    this.minX = minX
    this.maxX = maxX
    this.minY = minY
    this.maxY = maxY
    this.targetVerticalAxisPosition = targetVerticalAxisPosition
    this.pointPosition = pointPosition
    decorations?.also(::setDecorations)
    persistentMarkers?.also(::setPersistentMarkers)
}

/**
 * Creates a [LineChart.LineSpec] for use in [LineChart]s.
 *
 * @param lineColor the color of the line.
 * @param lineThickness the thickness of the line.
 * @param lineBackgroundShader an optional [DynamicShader] to use for the area below the line.
 * @param lineCap the stroke cap for the line.
 * @param point an optional [Component] that can be drawn at a given point on the line.
 * @param pointSize the size of the [point].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels in degrees.
 * @param pointConnector the [LineSpec.PointConnector] for the line.
 *
 * @see LineChart
 * @see LineChart.LineSpec
 */
public fun lineSpec(
    lineColor: Color,
    lineThickness: Dp = DefaultDimens.LINE_THICKNESS.dp,
    lineBackgroundShader: DynamicShader? = DynamicShaders.fromBrush(
        brush = Brush.verticalGradient(
            listOf(
                lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_END),
            ),
        ),
    ),
    lineCap: StrokeCap = StrokeCap.Round,
    point: Component? = null,
    pointSize: Dp = DefaultDimens.POINT_SIZE.dp,
    dataLabel: TextComponent? = null,
    dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
    dataLabelRotationDegrees: Float = 0f,
    pointConnector: LineSpec.PointConnector = DefaultPointConnector(),
): LineSpec = LineSpec(
    lineColor = lineColor.toArgb(),
    lineThicknessDp = lineThickness.value,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap.paintCap,
    point = point,
    pointSizeDp = pointSize.value,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)

/**
 * Creates a [LineChart.LineSpec] for use in [LineChart]s.
 *
 * @param lineColor the color of the line.
 * @param lineThickness the thickness of the line.
 * @param lineBackgroundShader an optional [DynamicShader] to use for the area below the line.
 * @param lineCap the stroke cap for the line.
 * @param cubicStrength the strength of the cubic bezier curve between each key point on the line.
 * @param point an optional [Component] that can be drawn at a given point on the line.
 * @param pointSize the size of the [point].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels in degrees.
 *
 * @see LineChart
 * @see LineChart.LineSpec
 */
@Deprecated(
    message = """Rather than using this `lineSpec` function and its `cubicStrength` parameter, use the `lineSpec`
        function with the `pointConnector` parameter and provide a `DefaultPointConnector` instance with a custom
        `cubicStrength` via the `pointConnector` parameter.""",
    replaceWith = ReplaceWith(
        expression = """lineSpec(
                lineColor = lineColor,
                lineThickness = lineThickness,
                lineBackgroundShader = lineBackgroundShader,
                lineCap = lineCap,
                point = point,
                pointSize = pointSize,
                dataLabel = dataLabel,
                dataLabelVerticalPosition = dataLabelVerticalPosition,
                dataLabelValueFormatter = dataLabelValueFormatter,
                dataLabelRotationDegrees = dataLabelRotationDegrees,
                pointPosition = pointPosition,
                pointConnector = DefaultPointConnector(cubicStrength = cubicStrength),
            )""",
        imports = arrayOf("com.patrykandpatrick.vico.core.chart.DefaultPointConnector"),
    ),
    level = DeprecationLevel.ERROR,
)
public fun lineSpec(
    lineColor: Color,
    lineThickness: Dp = DefaultDimens.LINE_THICKNESS.dp,
    lineBackgroundShader: DynamicShader? = DynamicShaders.fromBrush(
        brush = Brush.verticalGradient(
            listOf(
                lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_END),
            ),
        ),
    ),
    lineCap: StrokeCap = StrokeCap.Round,
    cubicStrength: Float,
    point: Component? = null,
    pointSize: Dp = DefaultDimens.POINT_SIZE.dp,
    dataLabel: TextComponent? = null,
    dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
    dataLabelRotationDegrees: Float = 0f,
): LineSpec = LineSpec(
    lineColor = lineColor.toArgb(),
    lineThicknessDp = lineThickness.value,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap.paintCap,
    point = point,
    pointSizeDp = pointSize.value,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = DefaultPointConnector(cubicStrength = cubicStrength),
)

private val StrokeCap.paintCap: Paint.Cap
    get() = when (this) {
        StrokeCap.Butt -> Paint.Cap.BUTT
        StrokeCap.Round -> Paint.Cap.ROUND
        StrokeCap.Square -> Paint.Cap.SQUARE
        else -> throw IllegalArgumentException("Not `StrokeCap.Butt`, `StrokeCap.Round`, or `StrokeCap.Square`.")
    }
