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
import android.graphics.Shader
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.alpha
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.core.common.withOpacity
import kotlin.properties.Delegates

/**
 * [ShapeComponent] is a [Component] that draws a shape.
 *
 * @param shape the [Shape] that will be drawn.
 * @param color the color of the shape.
 * @param dynamicShader an optional [Shader] provider used as the shape’s background.
 * @param margins the [Component]’s margins.
 * @param strokeWidthDp the width of the shape’s stroke (in dp).
 * @param strokeColor the color of the stroke.
 */
public open class ShapeComponent(
  public val shape: Shape = Shape.Rectangle,
  color: Int = Color.BLACK,
  public val dynamicShader: DynamicShader? = null,
  override val margins: Dimensions = Dimensions.Empty,
  public val strokeWidthDp: Float = 0f,
  strokeColor: Int = Color.TRANSPARENT,
) : PaintComponent<ShapeComponent>(), Component {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
  private val strokePaint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      this.color = strokeColor
      style = Paint.Style.STROKE
    }

  protected val path: Path = Path()

  /** The color of the shape. */
  public var color: Int by Delegates.observable(color) { _, _, value -> paint.color = value }

  /** The color of the stroke. */
  public var strokeColor: Int by
    Delegates.observable(strokeColor) { _, _, value -> strokePaint.color = value }

  init {
    require(strokeWidthDp >= 0) { "`strokeWidthDp` must be nonnegative." }
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
      val strokeWidth = strokeWidthDp.pixels
      if (strokeWidth != 0f) {
        adjustedLeft += strokeWidth.half
        adjustedTop += strokeWidth.half
        adjustedRight -= strokeWidth.half
        adjustedBottom -= strokeWidth.half
        if (adjustedLeft > adjustedRight || adjustedTop > adjustedBottom) return
      }
      path.rewind()
      applyShader(this, left, top, right, bottom)
      componentShadow.maybeUpdateShadowLayer(this, paint, color, opacity)
      paint.withOpacity(opacity) { paint ->
        shape.draw(this, paint, path, adjustedLeft, adjustedTop, adjustedRight, adjustedBottom)
      }
      if (strokeWidth == 0f || strokeColor.alpha == 0) return
      strokePaint.strokeWidth = strokeWidth
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
    dynamicShader?.provideShader(context, left, top, right, bottom)?.let { shader ->
      paint.shader = shader
    }
  }
}
