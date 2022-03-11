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

package pl.patrykgoworowski.vico.compose.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.vico.compose.component.dashedShape
import pl.patrykgoworowski.vico.compose.component.shape.lineComponent
import pl.patrykgoworowski.vico.compose.component.shape.shader.fromBrush
import pl.patrykgoworowski.vico.core.DefaultAlpha
import pl.patrykgoworowski.vico.core.DefaultColors
import pl.patrykgoworowski.vico.core.DefaultDimens
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders

/**
 * Defines the appearance of charts.
 * @property axis the appearance of chart axes.
 * @property columnChart the appearance of column charts.
 * @property lineChart the appearance of line charts.
 * @property marker the appearance of chart markers.
 * @property elevationOverlayColor the color used for elevation overlays.
 */
public data class ChartStyle(
    val axis: Axis,
    val columnChart: ColumnChart,
    val lineChart: LineChart,
    val marker: Marker,
    val elevationOverlayColor: Color,
) {
    /**
     * Defines the appearance of chart axes.
     * @property axisLabelBackground an optional [ShapeComponent] to display behind the text of axis labels.
     * @property axisLabelColor the text color for axis labels.
     * @property axisLabelTextSize the text size for axis labels.
     * @property axisLabelLineCount the line count for axis labels.
     * @property axisLabelVerticalPadding the amount of vertical padding between the background and the text of axis
     * labels.
     * @property axisLabelHorizontalPadding the amount of horizontal padding between the background and the text of axis
     * labels.
     * @property axisLabelVerticalMargin the vertical margin around the backgrounds of axis labels.
     * @property axisLabelHorizontalMargin the horizontal margin around the backgrounds of axis labels.
     * @property axisLabelRotationDegrees the number of degrees by which axis labels are rotated.
     * @property axisGuidelineColor the color of axis guidelines.
     * @property axisGuidelineWidth the width of axis guidelines.
     * @property axisGuidelineShape the [Shape] used for axis guidelines.
     * @property axisLineColor the color of axis lines.
     * @property axisLineWidth the width of axis lines.
     * @property axisLineShape the [Shape] used for axis lines.
     * @property axisTickColor the color of axis ticks.
     * @property axisTickWidth the width of axis ticks.
     * @property axisTickShape the [Shape] used for axis ticks.
     * @property axisTickLength the length of axis ticks.
     * @property axisValueFormatter the [AxisValueFormatter] to use for axis labels.
     */
    public data class Axis(
        val axisLabelBackground: ShapeComponent? = null,
        val axisLabelColor: Color,
        val axisLabelTextSize: TextUnit = DefaultDimens.AXIS_LABEL_SIZE.sp,
        val axisLabelLineCount: Int = DefaultDimens.AXIS_LABEL_MAX_LINES,
        val axisLabelVerticalPadding: Dp = DefaultDimens.AXIS_LABEL_VERTICAL_PADDING.dp,
        val axisLabelHorizontalPadding: Dp = DefaultDimens.AXIS_LABEL_HORIZONTAL_PADDING.dp,
        val axisLabelVerticalMargin: Dp = DefaultDimens.AXIS_LABEL_VERTICAL_MARGIN.dp,
        val axisLabelHorizontalMargin: Dp = DefaultDimens.AXIS_LABEL_HORIZONTAL_MARGIN.dp,
        val axisLabelRotationDegrees: Float = DefaultDimens.AXIS_LABEL_ROTATION_DEGREES,
        val axisGuidelineColor: Color,
        val axisGuidelineWidth: Dp = DefaultDimens.AXIS_GUIDELINE_WIDTH.dp,
        val axisGuidelineShape: Shape = dashedShape(
            shape = Shapes.rectShape,
            dashLength = DefaultDimens.DASH_LENGTH.dp,
            gapLength = DefaultDimens.DASH_GAP.dp,
        ),
        val axisLineColor: Color,
        val axisLineWidth: Dp = DefaultDimens.AXIS_LINE_WIDTH.dp,
        val axisLineShape: Shape = Shapes.rectShape,
        val axisTickColor: Color = axisLineColor,
        val axisTickWidth: Dp = axisLineWidth,
        val axisTickShape: Shape = Shapes.rectShape,
        val axisTickLength: Dp = DefaultDimens.AXIS_TICK_LENGTH.dp,
        val axisValueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter(),
    )

    /**
     * Defines the appearance of column charts.
     * @property columns the [LineComponent] instances to use for columns. This list is iterated through as many times
     * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
     * @property outsideSpacing the horizontal padding between the edges of chart segments and the columns they contain.
     * @property innerSpacing the spacing between the columns contained in chart segments. This has no effect on
     * segments that contain a single column only.
     */
    public data class ColumnChart(
        val columns: List<LineComponent>,
        val outsideSpacing: Dp = DefaultDimens.COLUMN_OUTSIDE_SPACING.dp,
        val innerSpacing: Dp = DefaultDimens.COLUMN_INSIDE_SPACING.dp,
    )

    public data class LineChart(
        val getPoint: Component? = null,
        val pointSize: Dp = DefaultDimens.POINT_SIZE.dp,
        val spacing: Dp = DefaultDimens.POINT_SPACING.dp,
        val lineWidth: Dp = DefaultDimens.LINE_THICKNESS.dp,
        val lineColor: Color,
        val lineBackgroundShader: DynamicShader? = null,
    )

    public data class Marker(
        val indicatorSize: Dp = DefaultDimens.MARKER_INDICATOR_SIZE.dp,
        val horizontalPadding: Dp = DefaultDimens.MARKER_HORIZONTAL_PADDING.dp,
        val verticalPadding: Dp = DefaultDimens.MARKER_VERTICAL_PADDING.dp,
    )

    public companion object {

        /**
         * Creates a base implementation of [ChartStyle] using the provided colors.
         */
        @Suppress("LongParameterList")
        public fun fromColors(
            axisLabelColor: Color,
            axisGuidelineColor: Color,
            axisLineColor: Color,
            columnColors: List<Color>,
            lineColor: Color,
            elevationOverlayColor: Color,
        ): ChartStyle = ChartStyle(
            axis = Axis(
                axisLabelColor = axisLabelColor,
                axisGuidelineColor = axisGuidelineColor,
                axisLineColor = axisLineColor,
            ),
            columnChart = ColumnChart(
                columns = columnColors.map { columnColor ->
                    lineComponent(
                        color = columnColor,
                        thickness = DefaultDimens.COLUMN_WIDTH.dp,
                        shape = Shapes.roundedCornerShape(
                            allPercent = DefaultDimens.COLUMN_ROUNDNESS_PERCENT,
                        ),
                    )
                },
            ),
            lineChart = LineChart(
                lineColor = lineColor,
                lineBackgroundShader = DynamicShaders.fromBrush(
                    brush = Brush.verticalGradient(
                        listOf(
                            lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                            lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_END),
                        ),
                    ),
                ),
            ),
            marker = Marker(),
            elevationOverlayColor = elevationOverlayColor,
        )

        internal fun fromDefaultColors(defaultColors: DefaultColors) = fromColors(
            axisLabelColor = Color(defaultColors.axisLabelColor),
            axisGuidelineColor = Color(defaultColors.axisGuidelineColor),
            axisLineColor = Color(defaultColors.axisLineColor),
            columnColors = listOf(
                defaultColors.column1Color,
                defaultColors.column2Color,
                defaultColors.column3Color,
            ).map { Color(it) },
            lineColor = Color(defaultColors.lineColor),
            elevationOverlayColor = Color(defaultColors.elevationOverlayColor),
        )
    }
}

/**
 * Provides a [ChartStyle] instance.
 */
public object LocalChartStyle {

    internal val default: ChartStyle
        @Composable
        get() = ChartStyle.fromDefaultColors(
            defaultColors = if (isSystemInDarkTheme()) DefaultColors.Dark else DefaultColors.Light,
        )

    private val LocalProvidedStyle: ProvidableCompositionLocal<ChartStyle?> =
        compositionLocalOf { null }

    /**
     * The [ChartStyle] instance provided by [LocalChartStyle] at the call site.
     */
    public val current: ChartStyle
        @Composable get() = LocalProvidedStyle.current ?: default

    /**
     * Provides a [ChartStyle] instance via [LocalChartStyle].
     *
     * @param chartStyle the [ChartStyle] instance to provide.
     */
    public infix fun provides(chartStyle: ChartStyle): ProvidedValue<ChartStyle?> =
        LocalProvidedStyle.provides(chartStyle)
}

/**
 * Offers quick access to [LocalChartStyle.current].
 */
public val currentChartStyle: ChartStyle
    @Composable get() = LocalChartStyle.current

/**
 * Provides a [ChartStyle] instance to [content] via [LocalChartStyle].
 *
 * @param chartStyle the [ChartStyle] instance to provide.
 */
@Composable
public fun ProvideChartStyle(
    chartStyle: ChartStyle = LocalChartStyle.default,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalChartStyle provides chartStyle,
        content = content,
    )
}
