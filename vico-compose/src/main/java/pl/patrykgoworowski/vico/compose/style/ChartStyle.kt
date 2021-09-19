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
import pl.patrykgoworowski.vico.core.Colors
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders

data class ChartStyle(
    val axis: Axis,
    val columnChart: ColumnChart,
    val lineChart: LineChart,
) {
    data class Axis(
        val axisLabelBackground: ShapeComponent<Shape>? = null,
        val axisLabelColor: Color,
        val axisLabelTextSize: TextUnit = Dimens.AXIS_LABEL_SIZE.sp,
        val axisLabelLineCount: Int = Dimens.AXIS_LABEL_MAX_LINES,
        val axisLabelVerticalPadding: Dp = Dimens.AXIS_LABEL_VERTICAL_PADDING.dp,
        val axisLabelHorizontalPadding: Dp = Dimens.AXIS_LABEL_HORIZONTAL_PADDING.dp,
        val axisGuidelineColor: Color,
        val axisGuidelineWidth: Dp = Dimens.AXIS_GUIDELINE_WIDTH.dp,
        val axisGuidelineShape: @Composable () -> Shape = {
            dashedShape(
                shape = Shapes.rectShape,
                dashLength = Dimens.DASH_LENGTH.dp,
                gapLength = Dimens.DASH_GAP.dp,
            )
        },
        val axisLineColor: Color,
        val axisLineWidth: Dp = Dimens.AXIS_LINE_WIDTH.dp,
        val axisLineShape: @Composable () -> Shape = { Shapes.rectShape },
        val axisTickColor: Color = axisLineColor,
        val axisTickWidth: Dp = axisLineWidth,
        val axisTickShape: @Composable () -> Shape = { Shapes.rectShape },
        val axisTickLength: Dp = Dimens.AXIS_TICK_LENGTH.dp,
        val axisValueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter()
    )

    data class ColumnChart(
        val getColumns: @Composable () -> List<LineComponent>,
        val outsideSpacing: Dp = Dimens.COLUMN_OUTSIDE_SPACING.dp,
        val innerSpacing: Dp = Dimens.COLUMN_INSIDE_SPACING.dp,
    )

    data class LineChart(
        val getPoint: @Composable () -> Component? = { null },
        val pointSize: Dp,
        val spacing: Dp,
        val lineWidth: Dp,
        val lineColor: Color,
        val lineBackgroundShader: DynamicShader? = null,
    )
}

object LocalChartStyle {

    private val LocalLightStyle: ProvidableCompositionLocal<ChartStyle> =
        compositionLocalOf { getChartStyle(Colors.Light) }

    private val LocalDarkStyle: ProvidableCompositionLocal<ChartStyle> =
        compositionLocalOf { getChartStyle(Colors.Dark) }

    private fun getChartStyle(colors: Colors): ChartStyle = ChartStyle(
        axis = ChartStyle.Axis(
            axisLabelColor = Color(colors.axisLabelColor),
            axisGuidelineColor = Color(colors.axisGuidelineColor),
            axisLineColor = Color(colors.axisLineColor),
        ),
        columnChart = ChartStyle.ColumnChart(
            getColumns = {
                listOf(
                    lineComponent(
                        color = Color(colors.column1Color),
                        thickness = Dimens.COLUMN_WIDTH.dp,
                        shape = Shapes.roundedCornersShape(
                            allPercent = Dimens.COLUMN_ROUNDNESS_PERCENT
                        )
                    ),
                    lineComponent(
                        color = Color(colors.column2Color),
                        thickness = Dimens.COLUMN_WIDTH.dp,
                        shape = Shapes.roundedCornersShape(
                            allPercent = Dimens.COLUMN_ROUNDNESS_PERCENT
                        )
                    ),
                    lineComponent(
                        color = Color(colors.column3Color),
                        thickness = Dimens.COLUMN_WIDTH.dp,
                        shape = Shapes.roundedCornersShape(
                            allPercent = Dimens.COLUMN_ROUNDNESS_PERCENT
                        )
                    ),
                )
            }
        ),
        lineChart = ChartStyle.LineChart(
            pointSize = Dimens.POINT_SIZE.dp,
            spacing = Dimens.POINT_SPACING.dp,
            lineWidth = Dimens.LINE_WIDTH.dp,
            lineColor = Color(colors.lineColor),
            lineBackgroundShader = DynamicShaders.fromBrush(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(colors.lineColor).copy(alpha = .5f),
                        Color(colors.lineColor).copy(alpha = 0f),
                    ),
                )
            )
        )
    )

    private val LocalProvidedStyle: ProvidableCompositionLocal<ChartStyle?> =
        compositionLocalOf { null }

    public val current: ChartStyle
        @Composable get() = LocalProvidedStyle.current
            ?: if (isSystemInDarkTheme()) LocalDarkStyle.current else LocalLightStyle.current

    public infix fun provides(chartStyle: ChartStyle): ProvidedValue<ChartStyle?> =
        LocalProvidedStyle.provides(chartStyle)
}

val currentChartStyle: ChartStyle
    @Composable get() = LocalChartStyle.current
