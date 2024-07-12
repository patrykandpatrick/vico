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
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.alpha
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.core.common.withOpacity
import kotlin.properties.Delegates

/**
 * Draws [Shape]s.
 *
 * @param color the fill color.
 * @param strokeColor the stroke color.
 * @property shape the [Shape].
 * @property margins the margins.
 * @property strokeThicknessDp the stroke thickness (in dp).
 * @property shader applied to the fill.
 */
public open class ShapeComponent(
  color: Int = Color.BLACK,
  public val shape: Shape = Shape.Rectangle,
  override val margins: Dimensions = Dimensions.Empty,
  strokeColor: Int = Color.TRANSPARENT,
  public val strokeThicknessDp: Float = 0f,
  public val shader: DynamicShader? = null,
) : PaintComponent<ShapeComponent>(), Component {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
  private val strokePaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      this.color = strokeColor
      style = Paint.Style.STROKE
    }

  protected val path: Path = Path()

  /** The fill color. */
  public var color: Int by Delegates.observable(color) { _, _, value -> paint.color = value }

  /** The stroke color. */
  public var strokeColor: Int by
    Delegates.observable(strokeColor) { _, _, value -> strokePaint.color = value }

  init {
    require(strokeThicknessDp >= 0) { "`strokeThicknessDp` must be nonnegative." }
  }

  override fun draw(
    context: DrawContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    opacity: Float,
  ) {
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
      componentShadow.updateShadowLayer(this, paint, opacity)
      paint.withOpacity(opacity) { paint ->
        shape.draw(this, paint, path, adjustedLeft, adjustedTop, adjustedRight, adjustedBottom)
      }
      if (strokeThickness == 0f || strokeColor.alpha == 0) return
      strokePaint.strokeWidth = strokeThickness
      shape.draw(this, strokePaint, path, adjustedLeft, adjustedTop, adjustedRight, adjustedBottom)
    }
  }

  protected fun applyShader(
    context: DrawContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    shader?.provideShader(context, left, top, right, bottom)?.let { shader ->
      paint.shader = shader
    }
  }
}
