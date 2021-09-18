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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.vico.compose.foundation.isSystemInDarkTheme
import pl.patrykgoworowski.vico.core.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.shape.Shape
import pl.patrykgoworowski.vico.core.shape.Shapes

data class ChartStyle(
    val axis: Axis,
) {
    data class Axis(
        val axisLabelBackground: ShapeComponent<Shape>? = null,
        val axisLabelColor: Color,
        val axisLabelTextSize: TextUnit = 12.sp,
        val axisLabelLineCount: Int = 1,
        val axisLabelVerticalPadding: Dp = 2.dp,
        val axisLabelHorizontalPadding: Dp = 4.dp,
        val axisGuidelineColor: Color,
        val axisGuidelineWidth: Dp = 1.dp,
        val axisGuidelineShape: Shape = Shapes.rectShape,
        val axisLineColor: Color,
        val axisLineWidth: Dp = 2.dp,
        val axisLineShape: Shape = Shapes.rectShape,
        val axisTickColor: Color = axisLineColor,
        val axisTickWidth: Dp = axisLineWidth,
        val axisTickShape: Shape = Shapes.rectShape,
        val axisTickLength: Dp = 4.dp,
        val axisValueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter()
    )
}

object LocalChartStyle {

    private val LocalLightStyle: ProvidableCompositionLocal<ChartStyle> = compositionLocalOf {
        ChartStyle(
            axis = ChartStyle.Axis(
                axisLabelColor = Color.Black,
                axisGuidelineColor = Color(0xFFAAAAAA),
                axisLineColor = Color(0xFF8A8A8A),
            )
        )
    }

    private val LocalDarkStyle: ProvidableCompositionLocal<ChartStyle> = compositionLocalOf {
        ChartStyle(
            axis = ChartStyle.Axis(
                axisLabelColor = Color.White,
                axisGuidelineColor = Color(0xFF323232),
                axisLineColor = Color(0xFF424242),
            )
        )
    }

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
