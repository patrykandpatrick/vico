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

import android.graphics.RectF
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.shape.Shape

/**
 * Draws lines.
 *
 * @param fill the fill.
 * @param shape the [Shape].
 * @param margins the margins.
 * @param strokeFill the stroke fill.
 * @param strokeThicknessDp the stroke thickness (in dp).
 * @param shadow stores the shadow properties.
 * @property thicknessDp the line thickness (in dp).
 */
public open class LineComponent(
  fill: Fill,
  public val thicknessDp: Float = Defaults.LINE_COMPONENT_THICKNESS_DP,
  shape: Shape = Shape.Rectangle,
  margins: Dimensions = Dimensions.Empty,
  strokeFill: Fill = Fill.Transparent,
  strokeThicknessDp: Float = 0f,
  shadow: Shadow? = null,
) : ShapeComponent(fill, shape, margins, strokeFill, strokeThicknessDp, shadow) {
  private val MeasuringContext.thickness: Float
    get() = thicknessDp.pixels

  /** A convenience function for [draw] that draws the [LineComponent] horizontally. */
  public open fun drawHorizontal(
    context: DrawingContext,
    left: Float,
    right: Float,
    centerY: Float,
    thicknessScale: Float = 1f,
  ): Unit =
    with(context) {
      draw(
        context,
        left = left,
        top = centerY - thickness * thicknessScale / 2,
        right = right,
        bottom = centerY + thickness * thicknessScale / 2,
      )
    }

  /**
   * Checks whether the [LineComponent] fits horizontally within the given [boundingBox] with its
   * current [thicknessDp].
   */
  public open fun fitsInHorizontal(
    context: DrawingContext,
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
    context: DrawingContext,
    top: Float,
    bottom: Float,
    centerX: Float,
    thicknessScale: Float = 1f,
  ): Unit =
    with(context) {
      draw(
        context,
        left = centerX - thickness * thicknessScale / 2,
        top = top,
        right = centerX + thickness * thicknessScale / 2,
        bottom = bottom,
      )
    }

  /**
   * Checks whether the [LineComponent] fits vertically within the given [boundingBox] with its
   * current [thicknessDp].
   */
  public open fun fitsInVertical(
    context: DrawingContext,
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
    context: DrawingContext,
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
    fill: Fill,
    shape: Shape,
    margins: Dimensions,
    strokeFill: Fill,
    strokeThicknessDp: Float,
    shadow: Shadow?,
  ): LineComponent =
    LineComponent(fill, thicknessDp, shape, margins, strokeFill, strokeThicknessDp, shadow)

  /** Creates a new [LineComponent] based on this one. */
  public open fun copy(
    fill: Fill = this.fill,
    thicknessDp: Float = this.thicknessDp,
    shape: Shape = this.shape,
    margins: Dimensions = this.margins,
    strokeFill: Fill = this.strokeFill,
    strokeThicknessDp: Float = this.strokeThicknessDp,
    shadow: Shadow? = this.shadow,
  ): LineComponent =
    LineComponent(fill, thicknessDp, shape, margins, strokeFill, strokeThicknessDp, shadow)

  override fun equals(other: Any?): Boolean =
    super.equals(other) && other is LineComponent && thicknessDp == other.thicknessDp

  override fun hashCode(): Int = 31 * super.hashCode() + thicknessDp.hashCode()
}
