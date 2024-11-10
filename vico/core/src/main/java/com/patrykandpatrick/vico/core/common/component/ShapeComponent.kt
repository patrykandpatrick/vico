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

import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.alpha
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.shape.Shape

/**
 * Draws [Shape]s.
 *
 * @property fill the fill.
 * @property shape the [Shape].
 * @property margins the margins.
 * @property strokeFill the stroke fill.
 * @property strokeThicknessDp the stroke thickness (in dp).
 * @property shadow stores the shadow properties.
 */
public open class ShapeComponent(
  public val fill: Fill = Fill.Black,
  public val shape: Shape = Shape.Rectangle,
  protected val margins: Dimensions = Dimensions.Empty,
  public val strokeFill: Fill = Fill.Transparent,
  protected val strokeThicknessDp: Float = 0f,
  protected val shadow: Shadow? = null,
) : Component {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill.color }
  private val strokePaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      this.color = strokeFill.color
      style = Paint.Style.STROKE
    }

  protected val path: Path = Path()

  internal val effectiveStrokeFill: Fill
    get() = if (strokeFill.color.alpha == 0) fill else strokeFill

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
    fill.shader?.provideShader(context, left, top, right, bottom)?.let(paint::setShader)
    strokeFill.shader?.provideShader(context, left, top, right, bottom)?.let(strokePaint::setShader)
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
      if (strokeThickness == 0f || strokeFill.color.alpha == 0) return
      strokePaint.strokeWidth = strokeThickness
      canvas.drawPath(path, strokePaint)
    }
  }

  /** Creates a new [ShapeComponent] based on this one. */
  public open fun copy(
    fill: Fill = this.fill,
    shape: Shape = this.shape,
    margins: Dimensions = this.margins,
    strokeFill: Fill = this.strokeFill,
    strokeThicknessDp: Float = this.strokeThicknessDp,
    shadow: Shadow? = this.shadow,
  ): ShapeComponent = ShapeComponent(fill, shape, margins, strokeFill, strokeThicknessDp, shadow)

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ShapeComponent &&
        fill == other.fill &&
        shape == other.shape &&
        margins == other.margins &&
        strokeFill == other.strokeFill &&
        strokeThicknessDp == other.strokeThicknessDp &&
        shadow == other.shadow

  override fun hashCode(): Int {
    var result = fill.hashCode()
    result = 31 * result + shape.hashCode()
    result = 31 * result + margins.hashCode()
    result = 31 * result + strokeFill.hashCode()
    result = 31 * result + strokeThicknessDp.hashCode()
    result = 31 * result + shadow.hashCode()
    return result
  }
}
