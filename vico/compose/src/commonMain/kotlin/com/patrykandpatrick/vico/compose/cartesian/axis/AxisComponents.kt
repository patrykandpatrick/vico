/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.cartesian.axis

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.common.*
import com.patrykandpatrick.vico.compose.common.component.*

/** A [rememberTextComponent] alias with defaults for [Axis] labels. */
@Composable
public fun rememberAxisLabelComponent(
  style: TextStyle = TextStyle(color = vicoTheme.textColor, fontSize = Defaults.AXIS_LABEL_SIZE.sp),
  lineCount: Int = Defaults.AXIS_LABEL_MAX_LINES,
  overflow: TextOverflow = TextOverflow.Ellipsis,
  margins: Insets =
    Insets(Defaults.AXIS_LABEL_HORIZONTAL_MARGIN.dp, Defaults.AXIS_LABEL_VERTICAL_MARGIN.dp),
  padding: Insets =
    Insets(Defaults.AXIS_LABEL_HORIZONTAL_PADDING.dp, Defaults.AXIS_LABEL_VERTICAL_PADDING.dp),
  background: Component? = null,
  minWidth: TextComponent.MinWidth = TextComponent.MinWidth.fixed(),
): TextComponent =
  rememberTextComponent(style, lineCount, overflow, margins, padding, background, minWidth)

/** A [rememberLineComponent] alias with defaults for [Axis] lines. */
@Composable
public fun rememberAxisLineComponent(
  fill: Fill = Fill(vicoTheme.lineColor),
  thickness: Dp = Defaults.AXIS_LINE_WIDTH.dp,
  shape: Shape = RectangleShape,
  margins: Insets = Insets.Zero,
  strokeFill: Fill = Fill.Transparent,
  strokeThickness: Dp = 0.dp,
  shadows: List<Shadow> = emptyList(),
): LineComponent =
  rememberLineComponent(fill, thickness, shape, margins, strokeFill, strokeThickness, shadows)

/** A [rememberLineComponent] alias with defaults for [Axis] ticks. */
@Composable
public fun rememberAxisTickComponent(
  fill: Fill = Fill(vicoTheme.lineColor),
  thickness: Dp = Defaults.AXIS_LINE_WIDTH.dp,
  shape: Shape = RectangleShape,
  margins: Insets = Insets.Zero,
  strokeFill: Fill = Fill.Transparent,
  strokeThickness: Dp = 0.dp,
  shadows: List<Shadow> = emptyList(),
): LineComponent =
  rememberLineComponent(fill, thickness, shape, margins, strokeFill, strokeThickness, shadows)

/** A [rememberLineComponent] alias with defaults for [Axis] guidelines. */
@Composable
public fun rememberAxisGuidelineComponent(
  fill: Fill = Fill(vicoTheme.lineColor),
  thickness: Dp = Defaults.AXIS_GUIDELINE_WIDTH.dp,
  shape: Shape = DashedShape(),
  margins: Insets = Insets.Zero,
  strokeFill: Fill = Fill.Transparent,
  strokeThickness: Dp = 0.dp,
  shadows: List<Shadow> = emptyList(),
): LineComponent =
  rememberLineComponent(fill, thickness, shape, margins, strokeFill, strokeThickness, shadows)
