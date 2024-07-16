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

package com.patrykandpatrick.vico.core.common.component

import android.graphics.Color
import android.graphics.RectF
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.MeasureContext
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape

/**
 * Draws lines.
 *
 * @param color the fill color.
 * @param shape the [Shape].
 * @param margins the margins.
 * @param strokeColor the stroke color.
 * @param strokeThicknessDp the stroke thickness (in dp).
 * @param shader applied to the fill.
 * @param shadow stores the shadow properties.
 * @property thicknessDp the line thickness (in dp).
 */
public open class LineComponent(
  color: Int,
  public val thicknessDp: Float = Defaults.LINE_COMPONENT_THICKNESS_DP,
  shape: Shape = Shape.Rectangle,
  margins: Dimensions = Dimensions.Empty,
  strokeColor: Int = Color.TRANSPARENT,
  strokeThicknessDp: Float = 0f,
  shader: DynamicShader? = null,
  shadow: Shadow? = null,
) : ShapeComponent(color, shape, margins, strokeColor, strokeThicknessDp, shader, shadow) {
  private val MeasureContext.thickness: Float
    get() = thicknessDp.pixels

  /** [color] if it’s not [Color.TRANSPARENT], and [strokeColor] otherwise. */
  public val solidOrStrokeColor: Int
    get() = if (color == Color.TRANSPARENT) strokeColor else color

  /** A convenience function for [draw] that draws the [LineComponent] horizontally. */
  public open fun drawHorizontal(
    context: DrawContext,
    left: Float,
    right: Float,
    centerY: Float,
    thicknessScale: Float = 1f,
    opacity: Float = 1f,
  ): Unit =
    with(context) {
      draw(
        context,
        left = left,
        top = centerY - thickness * thicknessScale / 2,
        right = right,
        bottom = centerY + thickness * thicknessScale / 2,
        opacity = opacity,
      )
    }

  /**
   * Checks whether the [LineComponent] fits horizontally within the given [boundingBox] with its
   * current [thicknessDp].
   */
  public open fun fitsInHorizontal(
    context: DrawContext,
    left: Float,
    right: Float,
    centerY: Float,
    boundingBox: RectF,
    thicknessScale: Float = 1f,
  ): Boolean =
    with(context) {
      boundingBox.contains(
        left,
        centerY - thickness * thicknessScale / 2,
        right,
        centerY + thickness * thicknessScale / 2,
      )
    }

  /** A convenience function for [draw] that draws the [LineComponent] vertically. */
  public open fun drawVertical(
    context: DrawContext,
    top: Float,
    bottom: Float,
    centerX: Float,
    thicknessScale: Float = 1f,
    opacity: Float = 1f,
  ): Unit =
    with(context) {
      draw(
        context,
        left = centerX - thickness * thicknessScale / 2,
        top = top,
        right = centerX + thickness * thicknessScale / 2,
        bottom = bottom,
        opacity = opacity,
      )
    }

  /**
   * Checks whether the [LineComponent] fits vertically within the given [boundingBox] with its
   * current [thicknessDp].
   */
  public open fun fitsInVertical(
    context: DrawContext,
    top: Float,
    bottom: Float,
    centerX: Float,
    boundingBox: RectF,
    thicknessScale: Float = 1f,
  ): Boolean =
    with(context) {
      boundingBox.contains(
        centerX - thickness * thicknessScale / 2,
        top,
        centerX + thickness * thicknessScale / 2,
        bottom,
      )
    }

  /**
   * Checks whether the [LineComponent] vertically intersects the given [boundingBox] with its
   * current [thicknessDp].
   */
  public open fun intersectsVertical(
    context: DrawContext,
    top: Float,
    bottom: Float,
    centerX: Float,
    boundingBox: RectF,
    thicknessScale: Float = 1f,
  ): Boolean =
    with(context) {
      val left = centerX - thickness * thicknessScale / 2
      val right = centerX + thickness * thicknessScale / 2
      boundingBox.left < right && left < boundingBox.right
    }

  /** Creates a new [LineComponent] based on this one. */
  override fun copy(
    color: Int,
    shape: Shape,
    margins: Dimensions,
    strokeColor: Int,
    strokeThicknessDp: Float,
    shader: DynamicShader?,
    shadow: Shadow?,
  ): LineComponent =
    LineComponent(
      color,
      thicknessDp,
      shape,
      margins,
      strokeColor,
      strokeThicknessDp,
      shader,
      shadow,
    )

  /** Creates a new [LineComponent] based on this one. */
  public open fun copy(
    color: Int = this.color,
    thicknessDp: Float = this.thicknessDp,
    shape: Shape = this.shape,
    margins: Dimensions = this.margins,
    strokeColor: Int = this.strokeColor,
    strokeThicknessDp: Float = this.strokeThicknessDp,
    shader: DynamicShader? = this.shader,
    shadow: Shadow? = this.shadow,
  ): LineComponent =
    LineComponent(
      color,
      thicknessDp,
      shape,
      margins,
      strokeColor,
      strokeThicknessDp,
      shader,
      shadow,
    )
}
