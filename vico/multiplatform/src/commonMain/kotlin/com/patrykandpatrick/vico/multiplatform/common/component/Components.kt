/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.common.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.multiplatform.common.Defaults
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.shape.Shape

/** Creates and remembers a [LineComponent]. */
@Composable
public fun rememberLineComponent(
  fill: Fill = Fill.Black,
  thickness: Dp = Defaults.LINE_COMPONENT_THICKNESS_DP.dp,
  shape: Shape = Shape.Rectangle,
  margins: Insets = Insets.Zero,
  strokeFill: Fill = Fill.Transparent,
  strokeThickness: Dp = 0.dp,
): LineComponent =
  remember(fill, shape, thickness, margins, strokeFill, strokeThickness) {
    LineComponent(fill, thickness, shape, margins, strokeFill, strokeThickness)
  }

/** Creates and remembers a [ShapeComponent]. */
@Composable
public fun rememberShapeComponent(
  fill: Fill = Fill.Black,
  shape: Shape = Shape.Rectangle,
  margins: Insets = Insets.Zero,
  strokeFill: Fill = Fill.Transparent,
  strokeThickness: Dp = 0.dp,
): ShapeComponent =
  remember(fill, shape, margins, strokeFill, strokeThickness) {
    ShapeComponent(fill, shape, margins, strokeFill, strokeThickness)
  }

/** Creates and remembers a [TextComponent]. */
@Composable
public fun rememberTextComponent(
  style: TextStyle = TextStyle(fontSize = Defaults.TEXT_COMPONENT_TEXT_SIZE.sp),
  lineCount: Int = Defaults.TEXT_COMPONENT_LINE_COUNT,
  overflow: TextOverflow = TextOverflow.Ellipsis,
  margins: Insets = Insets.Zero,
  padding: Insets = Insets.Zero,
  background: Component? = null,
  minWidth: TextComponent.MinWidth = TextComponent.MinWidth.fixed(),
): TextComponent =
  remember(style, lineCount, overflow, margins, padding, background, minWidth) {
    TextComponent(style, lineCount, overflow, margins, padding, background, minWidth)
  }
