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
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.dimensions.dimensionsOf
import pl.patrykgoworowski.vico.compose.component.shape.chartShape
import pl.patrykgoworowski.vico.compose.component.shape.shader.BrushShader
import pl.patrykgoworowski.vico.compose.extension.pixelSize
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.component.text.buildTextComponent
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions

public typealias ChartShape = pl.patrykgoworowski.vico.core.component.shape.Shape

/**
 * Creates a label to be displayed on chart axes.
 *
 * @param color the text color.
 * @param textSize the text size.
 * @param background a [ShapeComponent] to be displayed behind the text.
 * @param ellipsize the text truncation behavior.
 * @param lineCount the line count.
 * @param verticalPadding the vertical padding between the text and the background.
 * @param horizontalPadding the horizontal padding between the text and the background.
 * @param verticalMargin the vertical margin around the background.
 * @param horizontalMargin the horizontal margin around the background.
 * @param rotationDegrees the rotation of the component in degrees.
 */
@Composable
public fun axisLabelComponent(
    color: Color = currentChartStyle.axis.axisLabelColor,
    textSize: TextUnit = currentChartStyle.axis.axisLabelTextSize,
    background: ShapeComponent? = currentChartStyle.axis.axisLabelBackground,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = currentChartStyle.axis.axisLabelLineCount,
    verticalPadding: Dp = currentChartStyle.axis.axisLabelVerticalPadding,
    horizontalPadding: Dp = currentChartStyle.axis.axisLabelHorizontalPadding,
    verticalMargin: Dp = currentChartStyle.axis.axisLabelVerticalMargin,
    horizontalMargin: Dp = currentChartStyle.axis.axisLabelHorizontalMargin,
    rotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
): TextComponent = buildTextComponent {
    this.color = color.toArgb()
    this.textSizeSp = textSize.pixelSize()
    this.ellipsize = ellipsize
    this.lineCount = lineCount
    this.background = background
    this.padding = dimensionsOf(
        vertical = verticalPadding,
        horizontal = horizontalPadding,
    )
    this.margins = dimensionsOf(
        vertical = verticalMargin,
        horizontal = horizontalMargin,
    )
    this.rotationDegrees = rotationDegrees
}

/**
 * Creates an axis line.
 * @param color the background color.
 * @param thickness the line thickness.
 * @param shape the [ChartShape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the line.
 * @param margins the margins of the line.
 */
@Composable
public fun axisLineComponent(
    color: Color = currentChartStyle.axis.axisLineColor,
    thickness: Dp = currentChartStyle.axis.axisLineWidth,
    shape: ChartShape = currentChartStyle.axis.axisLineShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

@Composable
public fun axisLineComponent(
    color: Color,
    thickness: Dp = currentChartStyle.axis.axisLineWidth,
    shape: Shape = RectangleShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    brush: Brush? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = brush?.let(::BrushShader),
    shape = shape.chartShape(),
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates an axis tick.
 * @param color the background color.
 * @param thickness the thickness of the tick.
 * @param shape the [ChartShape] to use for the tick.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the tick.
 */
@Composable
public fun axisTickComponent(
    color: Color = currentChartStyle.axis.axisTickColor,
    thickness: Dp = currentChartStyle.axis.axisTickWidth,
    shape: ChartShape = currentChartStyle.axis.axisTickShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

@Composable
public fun axisTickComponent(
    color: Color,
    thickness: Dp = currentChartStyle.axis.axisTickWidth,
    shape: Shape = RectangleShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    brush: Brush? = null,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = brush?.let(::BrushShader),
    shape = shape.chartShape(),
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

@Composable
public fun axisGuidelineComponent(
    color: Color = currentChartStyle.axis.axisGuidelineColor,
    thickness: Dp = currentChartStyle.axis.axisGuidelineWidth,
    shape: ChartShape = currentChartStyle.axis.axisGuidelineShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)
