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
import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.alpha
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape

/**
 * Draws [Shape]s.
 *
 * @property color the fill color.
 * @property shape the [Shape].
 * @property margins the margins.
 * @property strokeColor the stroke color.
 * @property strokeThicknessDp the stroke thickness (in dp).
 * @property shader applied to the fill.
 * @property shadow stores the shadow properties.
 */
public open class ShapeComponent(
  public val color: Int = Color.BLACK,
  public val shape: Shape = Shape.Rectangle,
  protected val margins: Dimensions = Dimensions.Empty,
  public val strokeColor: Int = Color.TRANSPARENT,
  protected val strokeThicknessDp: Float = 0f,
  protected val shader: DynamicShader? = null,
  protected val shadow: Shadow? = null,
) : Component {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = this@ShapeComponent.color }
  private val strokePaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      this.color = strokeColor
      style = Paint.Style.STROKE
    }

  protected val path: Path = Path()

  init {
    require(strokeThicknessDp >= 0) { "`strokeThicknessDp` must be nonnegative." }
  }

  protected fun applyShader(
    context: DrawingContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    shader?.provideShader(context, left, top, right, bottom)?.let(paint::setShader)
  }

  override fun draw(context: DrawingContext, left: Float, top: Float, right: Float, bottom: Float) {
    with(context) {
      var adjustedLeft = left + margins.getLeftDp(isLtr).pixels
      var adjustedTop = top + margins.topDp.pixels
      var adjustedRight = right - margins.getRightDp(isLtr).pixels
      var adjustedBottom = bottom - margins.bottomDp.pixels
      if (adjustedLeft >= adjustedRight || adjustedTop >= adjustedBottom) return
      val strokeThickness = strokeThicknessDp.pixels
      if (strokeThickness != 0f) {
        adjustedLeft += strokeThickness.half
        adjustedTop += strokeThickness.half
        adjustedRight -= strokeThickness.half
        adjustedBottom -= strokeThickness.half
        if (adjustedLeft > adjustedRight || adjustedTop > adjustedBottom) return
      }
      path.rewind()
      applyShader(this, left, top, right, bottom)
      shadow?.updateShadowLayer(this, paint)
      shape.outline(this, path, adjustedLeft, adjustedTop, adjustedRight, adjustedBottom)
      canvas.drawPath(path, paint)
      if (strokeThickness == 0f || strokeColor.alpha == 0) return
      strokePaint.strokeWidth = strokeThickness
      canvas.drawPath(path, strokePaint)
    }
  }

  /** Creates a new [ShapeComponent] based on this one. */
  public open fun copy(
    color: Int = this.color,
    shape: Shape = this.shape,
    margins: Dimensions = this.margins,
    strokeColor: Int = this.strokeColor,
    strokeThicknessDp: Float = this.strokeThicknessDp,
    shader: DynamicShader? = this.shader,
    shadow: Shadow? = this.shadow,
  ): ShapeComponent =
    ShapeComponent(color, shape, margins, strokeColor, strokeThicknessDp, shader, shadow)
}
