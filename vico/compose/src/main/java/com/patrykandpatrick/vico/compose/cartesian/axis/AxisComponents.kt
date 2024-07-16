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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape

/** A [rememberTextComponent] alias with defaults for [Axis] labels. */
@Composable
public fun rememberAxisLabelComponent(
  color: Color = vicoTheme.textColor,
  typeface: Typeface = Typeface.DEFAULT,
  textSize: TextUnit = Defaults.AXIS_LABEL_SIZE.sp,
  textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
  lineCount: Int = Defaults.AXIS_LABEL_MAX_LINES,
  truncateAt: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
  margins: Dimensions =
    Dimensions.of(Defaults.AXIS_LABEL_HORIZONTAL_MARGIN.dp, Defaults.AXIS_LABEL_VERTICAL_MARGIN.dp),
  padding: Dimensions =
    Dimensions.of(
      Defaults.AXIS_LABEL_HORIZONTAL_PADDING.dp,
      Defaults.AXIS_LABEL_VERTICAL_PADDING.dp,
    ),
  background: Component? = null,
  minWidth: TextComponent.MinWidth = TextComponent.MinWidth.fixed(),
): TextComponent =
  rememberTextComponent(
    color,
    typeface,
    textSize,
    textAlignment,
    lineCount,
    truncateAt,
    margins,
    padding,
    background,
    minWidth,
  )

/** A [rememberLineComponent] alias with defaults for [Axis] lines. */
@Composable
public fun rememberAxisLineComponent(
  color: Color = vicoTheme.lineColor,
  thickness: Dp = Defaults.AXIS_LINE_WIDTH.dp,
  shape: Shape = Shape.Rectangle,
  margins: Dimensions = Dimensions.Empty,
  strokeColor: Color = Color.Transparent,
  strokeThickness: Dp = 0.dp,
  shader: DynamicShader? = null,
  shadow: Shadow? = null,
): LineComponent =
  rememberLineComponent(
    color,
    thickness,
    shape,
    margins,
    strokeColor,
    strokeThickness,
    shader,
    shadow,
  )

/** A [rememberLineComponent] alias with defaults for [Axis] ticks. */
@Composable
public fun rememberAxisTickComponent(
  color: Color = vicoTheme.lineColor,
  thickness: Dp = Defaults.AXIS_LINE_WIDTH.dp,
  shape: Shape = Shape.Rectangle,
  margins: Dimensions = Dimensions.Empty,
  strokeColor: Color = Color.Transparent,
  strokeThickness: Dp = 0.dp,
  shader: DynamicShader? = null,
  shadow: Shadow? = null,
): LineComponent =
  rememberLineComponent(
    color,
    thickness,
    shape,
    margins,
    strokeColor,
    strokeThickness,
    shader,
    shadow,
  )

/** A [rememberLineComponent] alias with defaults for [Axis] guidelines. */
@Composable
public fun rememberAxisGuidelineComponent(
  color: Color = vicoTheme.lineColor,
  thickness: Dp = Defaults.AXIS_GUIDELINE_WIDTH.dp,
  shape: Shape = Shape.dashed(Shape.Rectangle, Defaults.DASH_LENGTH.dp, Defaults.DASH_GAP.dp),
  margins: Dimensions = Dimensions.Empty,
  strokeColor: Color = Color.Transparent,
  strokeThickness: Dp = 0.dp,
  shader: DynamicShader? = null,
  shadow: Shadow? = null,
): LineComponent =
  rememberLineComponent(
    color,
    thickness,
    shape,
    margins,
    strokeColor,
    strokeThickness,
    shader,
    shadow,
  )
