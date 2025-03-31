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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.common.DrawingContext
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.half
import com.patrykandpatrick.vico.multiplatform.common.shape.Shape

/**
 * Draws [Shape]s.
 *
 * @property fill the fill.
 * @property shape the [Shape].
 * @property margins the margins.
 * @property strokeFill the stroke fill.
 * @property strokeThickness the stroke thickness.
 */
public open class ShapeComponent(
  public val fill: Fill = Fill.Black,
  public val shape: Shape = Shape.Rectangle,
  protected val margins: Insets = Insets.Zero,
  public val strokeFill: Fill = Fill.Transparent,
  protected val strokeThickness: Dp = 0.dp,
) : Component {
  private val paint = Paint().apply { color = fill.color }
  private val strokePaint =
    Paint().apply {
      this.color = strokeFill.color
      style = PaintingStyle.Stroke
    }

  protected val path: Path = Path()

  internal val effectiveStrokeFill: Fill
    get() = if (strokeFill.color.alpha == 0f) fill else strokeFill

  init {
    require(strokeThickness >= 0.dp) { "`strokeThickness` must be nonnegative." }
  }

  protected fun applyBrushes(size: Size) {
    fill.brush?.applyTo(size = size, p = paint, alpha = 1f)
    strokeFill.brush?.applyTo(size = size, p = strokePaint, alpha = 1f)
  }

  override fun draw(context: DrawingContext, left: Float, top: Float, right: Float, bottom: Float) {
    with(context) {
      var adjustedLeft = left + margins.getLeft(context)
      var adjustedTop = top + margins.top.pixels
      var adjustedRight = right - margins.getRight(context)
      var adjustedBottom = bottom - margins.bottom.pixels
      if (adjustedLeft >= adjustedRight || adjustedTop >= adjustedBottom) return
      val strokeThickness = strokeThickness.pixels
      if (strokeThickness != 0f) {
        adjustedLeft += strokeThickness.half
        adjustedTop += strokeThickness.half
        adjustedRight -= strokeThickness.half
        adjustedBottom -= strokeThickness.half
        if (adjustedLeft > adjustedRight || adjustedTop > adjustedBottom) return
      }
      path.rewind()
      val width = right - left
      val height = bottom - top
      applyBrushes(Size(width, height))
      shape.outline(this, path, 0f, 0f, width, height)
      canvas.withSave {
        canvas.translate(left, top)
        canvas.drawPath(path, paint)
        if (strokeThickness == 0f || strokeFill.color.alpha == 0f) return@withSave
        strokePaint.strokeWidth = strokeThickness
        canvas.drawPath(path, strokePaint)
      }
    }
  }

  /** Creates a new [ShapeComponent] based on this one. */
  public open fun copy(
    fill: Fill = this.fill,
    shape: Shape = this.shape,
    margins: Insets = this.margins,
    strokeFill: Fill = this.strokeFill,
    strokeThickness: Dp = this.strokeThickness,
  ): ShapeComponent = ShapeComponent(fill, shape, margins, strokeFill, strokeThickness)

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is ShapeComponent &&
        fill == other.fill &&
        shape == other.shape &&
        margins == other.margins &&
        strokeFill == other.strokeFill &&
        strokeThickness == other.strokeThickness

  override fun hashCode(): Int {
    var result = fill.hashCode()
    result = 31 * result + shape.hashCode()
    result = 31 * result + margins.hashCode()
    result = 31 * result + strokeFill.hashCode()
    result = 31 * result + strokeThickness.hashCode()
    return result
  }
}
