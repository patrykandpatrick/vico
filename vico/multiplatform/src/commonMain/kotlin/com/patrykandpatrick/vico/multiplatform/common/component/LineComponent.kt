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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.common.Defaults
import com.patrykandpatrick.vico.multiplatform.common.DrawingContext
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.half
import com.patrykandpatrick.vico.multiplatform.common.shape.Shape

/**
 * Draws lines.
 *
 * @param fill the fill.
 * @param shape the [Shape].
 * @param margins the margins.
 * @param strokeFill the stroke fill.
 * @param strokeThickness the stroke thickness.
 * @property thickness the line thickness.
 */
public open class LineComponent(
  fill: Fill,
  public val thickness: Dp = Defaults.LINE_COMPONENT_THICKNESS_DP.dp,
  shape: Shape = Shape.Rectangle,
  margins: Insets = Insets.Zero,
  strokeFill: Fill = Fill.Transparent,
  strokeThickness: Dp = 0.dp,
) : ShapeComponent(fill, shape, margins, strokeFill, strokeThickness) {
  /** A convenience function for [draw] that draws the [LineComponent] horizontally. */
  public open fun drawHorizontal(
    context: DrawingContext,
    left: Float,
    right: Float,
    y: Float,
    thicknessFactor: Float = 1f,
  ) {
    val halfThickness = (thicknessFactor * context.run { thickness.pixels }).half
    draw(
      context = context,
      left = left,
      top = y - halfThickness,
      right = right,
      bottom = y + halfThickness,
    )
  }

  /** A convenience function for [draw] that draws the [LineComponent] vertically. */
  public open fun drawVertical(
    context: DrawingContext,
    x: Float,
    top: Float,
    bottom: Float,
    thicknessFactor: Float = 1f,
  ) {
    val halfThickness = (thicknessFactor * context.run { thickness.pixels }).half
    draw(
      context = context,
      left = x - halfThickness,
      top = top,
      right = x + halfThickness,
      bottom = bottom,
    )
  }

  /** Creates a new [LineComponent] based on this one. */
  override fun copy(
    fill: Fill,
    shape: Shape,
    margins: Insets,
    strokeFill: Fill,
    strokeThickness: Dp,
  ): LineComponent = LineComponent(fill, thickness, shape, margins, strokeFill, strokeThickness)

  /** Creates a new [LineComponent] based on this one. */
  public open fun copy(
    fill: Fill = this.fill,
    thickness: Dp = this.thickness,
    shape: Shape = this.shape,
    margins: Insets = this.margins,
    strokeFill: Fill = this.strokeFill,
    strokeThickness: Dp = this.strokeThickness,
  ): LineComponent = LineComponent(fill, thickness, shape, margins, strokeFill, strokeThickness)

  override fun equals(other: Any?): Boolean =
    super.equals(other) && other is LineComponent && thickness == other.thickness

  override fun hashCode(): Int = 31 * super.hashCode() + thickness.hashCode()
}

internal fun LineComponent.intersectsVertical(
  context: DrawingContext,
  x: Float,
  bounds: Rect,
  thicknessFactor: Float = 1f,
): Boolean {
  val halfThickness = (thicknessFactor * context.run { thickness.pixels }).half
  val left = x - halfThickness.half
  val right = x + halfThickness.half
  return bounds.left < right && left < bounds.right
}
