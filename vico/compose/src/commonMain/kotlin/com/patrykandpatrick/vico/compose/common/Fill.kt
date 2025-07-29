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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.ColorScale
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.common.data.CacheStore
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.ceil

/**
 * Stores fill properties.
 *
 * @property color the color. If [brush] is not null, this is [Color.Black].
 * @property brush the [Brush].
 */
public class Fill
private constructor(
  public val color: Color,
  public val brush: Brush?,
  public val colorScale: ColorScale?,
) {
  /** Creates a color [Fill]. */
  public constructor(color: Color) : this(color = color, brush = null, colorScale = null)

  /** Creates a [Brush]&#0020;[Fill]. */
  public constructor(brush: Brush) : this(Color.Black, brush, null)

  /**
   * Creates a [ColorScale]&#0020;[Fill].
   *
   * @param verticalAxisPosition the position of the [Axis] whose coordinate system to use.
   * @param colors maps y-values to colors.
   * @param alpha returns the alpha.
   */
  public constructor(
    colors: (ExtraStore) -> Map<Number, Color>,
    alpha: (ExtraStore) -> Float = { 1f },
    verticalAxisPosition: Axis.Position.Vertical? = null,
  ) : this(Color.Black, null, ColorScale(colors, alpha, verticalAxisPosition))

  internal fun applyShader(paint: Paint, context: CartesianDrawingContext, size: Size) {
    when {
      brush != null -> brush.applyTo(size = size, p = paint, alpha = 1f)
      colorScale != null -> paint.shader = colorScale.getColorScaleShader(context)
      else -> paint.shader = null
    }
  }

  internal fun applyShader(
    paint: Paint,
    context: DrawingContext,
    size: Size,
    translationY: Float = 0f,
  ) {
    when {
      brush != null -> brush.applyTo(size = size, p = paint, alpha = 1f)
      colorScale != null && context is CartesianDrawingContext ->
        paint.shader = colorScale.getColorScaleShader(context, translationY)
      else -> paint.shader = null
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is Fill &&
        color == other.color &&
        brush == other.brush &&
        colorScale == other.colorScale

  override fun hashCode(): Int {
    var result = color.toArgb()
    result = 31 * result + brush?.hashCode().orZero
    result = 31 * result + colorScale.hashCode()
    return result
  }

  /** Houses [Fill] singletons. */
  public companion object {
    /** A black [Fill]. */
    public val Black: Fill = Fill(Color.Black)

    /** A transparent [Fill]. */
    public val Transparent: Fill = Fill(Color.Transparent)
  }
}

private val paint = Paint()
private val cacheKeyNamespace = CacheStore.KeyNamespace()

internal fun Fill.extractColor(
  context: DrawingContext,
  significantY: Float,
  width: Float,
  height: Float,
  side: Int = 1,
): Color =
  if (brush != null || colorScale != null) {
    val (bitmap, canvas) = context.getBitmap(cacheKeyNamespace)
    val correctedHeight = if (height <= 0f) bitmap.height.toFloat() else height.coerceAtLeast(1f)
    val correctedWidth = if (width <= 0f) bitmap.width.toFloat() else width.coerceAtLeast(1f)
    var bitmapY = if (side == 1) 0 else (correctedHeight - 1).toInt()
    if (brush != null) {
      brush.applyTo(size = Size(correctedWidth, correctedHeight), p = paint, alpha = 1f)
      canvas.drawRect(0f, 0f, correctedWidth, correctedHeight, paint)
    } else {
      require(context is CartesianDrawingContext) {
        "Color scale can only be used in cartesian charts with `CartesianDrawingContext`."
      }
      val columnTop = ceil(if (side == 1) significantY else significantY - correctedHeight - 1)
      paint.shader = colorScale?.getColorScaleShader(context)
      canvas.drawRect(0f, columnTop, correctedWidth, columnTop + correctedHeight, paint)
      bitmapY += columnTop.toInt()
    }
    bitmap.getPixel(correctedWidth.half.toInt(), bitmapY)
  } else {
    color
  }
