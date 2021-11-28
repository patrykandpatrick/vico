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

package pl.patrykgoworowski.vico.compose.axis

import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import pl.patrykgoworowski.vico.compose.component.shape.chartShape
import pl.patrykgoworowski.vico.compose.component.shape.shader.StaticShader
import pl.patrykgoworowski.vico.compose.extension.pixelSize
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions

public typealias ChartShape = pl.patrykgoworowski.vico.core.component.shape.Shape

@Composable
public fun axisLabelComponent(
    color: Color = currentChartStyle.axis.axisLabelColor,
    textSize: TextUnit = currentChartStyle.axis.axisLabelTextSize,
    background: ShapeComponent<ChartShape>? = currentChartStyle.axis.axisLabelBackground,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = currentChartStyle.axis.axisLabelLineCount,
    verticalPadding: Dp = currentChartStyle.axis.axisLabelVerticalPadding,
    horizontalPadding: Dp = currentChartStyle.axis.axisLabelHorizontalPadding,
): TextComponent = TextComponent(
    color = color.toArgb(),
    textSizeSp = textSize.pixelSize(),
    ellipsize = ellipsize,
    lineCount = lineCount,
    background = background,
    padding = MutableDimensions(
        startDp = horizontalPadding.value,
        topDp = verticalPadding.value,
        endDp = horizontalPadding.value,
        bottomDp = verticalPadding.value,
    )
)

@Composable
public fun axisLineComponent(
    color: Color = currentChartStyle.axis.axisLineColor,
    thickness: Dp = currentChartStyle.axis.axisLineWidth,
    shape: ChartShape = currentChartStyle.axis.axisLineShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
)

@Composable
public fun axisLineComponent(
    color: Color,
    thickness: Dp = currentChartStyle.axis.axisLineWidth,
    shape: Shape = RectangleShape,
    brush: Brush? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = brush?.let(::StaticShader),
    shape = shape.chartShape(),
    margins = margins,
)

@Composable
public fun axisTickComponent(
    color: Color = currentChartStyle.axis.axisTickColor,
    thickness: Dp = currentChartStyle.axis.axisTickWidth,
    dynamicShader: DynamicShader? = null,
    shape: ChartShape = currentChartStyle.axis.axisTickShape,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
)

@Composable
public fun axisTickComponent(
    color: Color,
    thickness: Dp = currentChartStyle.axis.axisTickWidth,
    brush: Brush? = null,
    shape: Shape = RectangleShape,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = brush?.let(::StaticShader),
    shape = shape.chartShape(),
)

@Composable
public fun axisGuidelineComponent(
    color: Color = currentChartStyle.axis.axisGuidelineColor,
    thickness: Dp = currentChartStyle.axis.axisGuidelineWidth,
    dynamicShader: DynamicShader? = null,
    shape: ChartShape = currentChartStyle.axis.axisGuidelineShape,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
)
