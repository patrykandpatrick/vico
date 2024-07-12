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

package com.patrykandpatrick.vico.compose.common.component

import android.graphics.Typeface
import android.text.Layout
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.common.pixelSize
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape

/** Creates and remembers a [LineComponent]. */
@Composable
public fun rememberLineComponent(
  color: Color = Color.Black,
  thickness: Dp = Defaults.LINE_COMPONENT_THICKNESS_DP.dp,
  shape: Shape = Shape.Rectangle,
  margins: Dimensions = Dimensions.Empty,
  strokeColor: Color = Color.Transparent,
  strokeThickness: Dp = 0.dp,
  shader: DynamicShader? = null,
): LineComponent =
  remember(color, shape, thickness, margins, strokeColor, strokeThickness, shader) {
    LineComponent(
      color.toArgb(),
      thickness.value,
      shape,
      margins,
      strokeColor.toArgb(),
      strokeThickness.value,
      shader,
    )
  }

/** Creates and remembers a [ShapeComponent]. */
@Composable
public fun rememberShapeComponent(
  color: Color = Color.Black,
  shape: Shape = Shape.Rectangle,
  margins: Dimensions = Dimensions.Empty,
  strokeColor: Color = Color.Transparent,
  strokeThickness: Dp = 0.dp,
  shader: DynamicShader? = null,
): ShapeComponent =
  remember(color, shape, margins, strokeColor, strokeThickness, shader) {
    ShapeComponent(
      color.toArgb(),
      shape,
      margins,
      strokeColor.toArgb(),
      strokeThickness.value,
      shader,
    )
  }

/** Creates and remembers a [LayeredComponent]. */
@Composable
public fun rememberLayeredComponent(
  rear: Component,
  front: Component,
  padding: Dimensions = Dimensions.Empty,
  margins: Dimensions = Dimensions.Empty,
): LayeredComponent =
  remember(rear, front, padding, margins) { LayeredComponent(rear, front, padding, margins) }

/** Creates and remembers a [TextComponent]. */
@Composable
public fun rememberTextComponent(
  color: Color = Color.Black,
  typeface: Typeface = Typeface.DEFAULT,
  textSize: TextUnit = Defaults.TEXT_COMPONENT_TEXT_SIZE.sp,
  textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
  lineCount: Int = Defaults.TEXT_COMPONENT_LINE_COUNT,
  truncateAt: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
  margins: Dimensions = Dimensions.Empty,
  padding: Dimensions = Dimensions.Empty,
  background: Component? = null,
  minWidth: TextComponent.MinWidth = TextComponent.MinWidth.fixed(),
): TextComponent =
  remember { TextComponent() }
    .apply {
      this.color = color.toArgb()
      this.typeface = typeface
      this.textSizeSp = textSize.pixelSize()
      this.textAlignment = textAlignment
      this.lineCount = lineCount
      this.truncateAt = truncateAt
      this.padding = padding
      this.margins = margins
      this.background = background
      this.minWidth = minWidth
    }

/** A [Dp] version of [TextComponent.MinWidth.fixed]. */
@Stable
public fun TextComponent.MinWidth.Companion.fixed(value: Dp = 0.dp): TextComponent.MinWidth =
  fixed(value.value)

/**
 * Applies a drop shadow to this [ShapeComponent].
 *
 * @param radius the blur radius.
 * @param dx the horizontal offset.
 * @param dy the vertical offset.
 * @param color the shadow color.
 */
@Suppress("UNCHECKED_CAST")
public fun <T : ShapeComponent> T.setShadow(
  radius: Dp,
  dx: Dp = 0.dp,
  dy: Dp = 0.dp,
  color: Color = Color(Defaults.SHADOW_COLOR),
): T = setShadow(radius.value, dx.value, dy.value, color.toArgb()) as T
