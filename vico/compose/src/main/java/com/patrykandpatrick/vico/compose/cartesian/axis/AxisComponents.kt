/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.patrykandpatrick.vico.compose.cartesian.axis

import android.graphics.Typeface
import android.text.Layout
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.BrushShader
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.compose.common.shape.toVicoShape
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape

/**
 * Creates and remembers a [TextComponent] to be used for axis labels.
 *
 * @param color the text color.
 * @param textSize the text size.
 * @param background an optional [ShapeComponent] to be displayed behind the text.
 * @param ellipsize the text truncation behavior.
 * @param lineCount the line count.
 * @param padding the padding between the text and the background.
 * @param margins the margins around the background.
 * @param typeface the [Typeface] for the text.
 * @param textAlignment the text alignment.
 */
@Composable
public fun rememberAxisLabelComponent(
  color: Color = vicoTheme.textColor,
  textSize: TextUnit = Defaults.AXIS_LABEL_SIZE.sp,
  background: ShapeComponent? = null,
  ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
  lineCount: Int = Defaults.AXIS_LABEL_MAX_LINES,
  padding: Dimensions =
    Dimensions.of(
      Defaults.AXIS_LABEL_HORIZONTAL_PADDING.dp,
      Defaults.AXIS_LABEL_VERTICAL_PADDING.dp,
    ),
  margins: Dimensions =
    Dimensions.of(Defaults.AXIS_LABEL_HORIZONTAL_MARGIN.dp, Defaults.AXIS_LABEL_VERTICAL_MARGIN.dp),
  typeface: Typeface = Typeface.MONOSPACE,
  textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
): TextComponent =
  rememberTextComponent(
    color,
    textSize,
    background,
    ellipsize,
    lineCount,
    padding,
    margins,
    typeface,
    textAlignment,
  )

/**
 * Creates and remembers a [LineComponent] styled as an axis line.
 *
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param brush an optional [Brush] to apply to the line.
 * @param margins the margins of the line.
 */
@Composable
public fun rememberAxisLineComponent(
  color: Color = vicoTheme.lineColor,
  thickness: Dp = Defaults.AXIS_LINE_WIDTH.dp,
  shape: Shape = Shape.Rectangle,
  strokeWidth: Dp = 0.dp,
  strokeColor: Color = Color.Transparent,
  brush: Brush? = null,
  margins: Dimensions = Dimensions.Empty,
): LineComponent =
  rememberLineComponent(
    color = color,
    thickness = thickness,
    shape = shape,
    dynamicShader = brush?.let(::BrushShader),
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
  )

/**
 * Creates and remembers a [LineComponent] styled as a tick line.
 *
 * @param color the background color.
 * @param thickness the thickness of the tick.
 * @param shape the [Shape] to use for the tick.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the tick.
 */
@Composable
public fun rememberAxisTickComponent(
  color: Color = vicoTheme.lineColor,
  thickness: Dp = Defaults.AXIS_LINE_WIDTH.dp,
  shape: Shape = Shape.Rectangle,
  strokeWidth: Dp = 0.dp,
  strokeColor: Color = Color.Transparent,
  dynamicShader: DynamicShader? = null,
): LineComponent =
  rememberLineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = dynamicShader,
    shape = shape,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
  )

/**
 * Creates and remembers a [LineComponent] styled as a tick line.
 *
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [androidx.compose.ui.graphics.Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param brush an optional [Brush] to apply to the line.
 */
@Deprecated(
  message =
    "Use the `rememberAxisTickComponent` overload that takes a " +
      "`com.patrykandpatrick.vico.core.component.shape.Shape`. " +
      "Convert the Compose shape using `androidx.compose.ui.graphics.Shape.toVicoShape()`."
)
@Composable
public fun rememberAxisTickComponent(
  color: Color,
  thickness: Dp = Defaults.AXIS_LINE_WIDTH.dp,
  shape: androidx.compose.ui.graphics.Shape = RectangleShape,
  strokeWidth: Dp = 0.dp,
  strokeColor: Color = Color.Transparent,
  brush: Brush? = null,
): LineComponent =
  rememberLineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = brush?.let(::BrushShader),
    shape = shape.toVicoShape(),
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
  )

/**
 * Creates and remembers a [LineComponent] styled as a guideline.
 *
 * @param color the background color.
 * @param thickness the line thickness.
 * @param shape the [Shape] to use for the guideline.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the guideline.
 * @param margins the margins of the guideline.
 */
@Composable
public fun rememberAxisGuidelineComponent(
  color: Color = vicoTheme.lineColor,
  thickness: Dp = Defaults.AXIS_GUIDELINE_WIDTH.dp,
  shape: Shape = Shape.dashed(Shape.Rectangle, Defaults.DASH_LENGTH.dp, Defaults.DASH_GAP.dp),
  strokeWidth: Dp = 0.dp,
  strokeColor: Color = Color.Transparent,
  dynamicShader: DynamicShader? = null,
  margins: Dimensions = Dimensions.Empty,
): LineComponent =
  rememberLineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
  )
