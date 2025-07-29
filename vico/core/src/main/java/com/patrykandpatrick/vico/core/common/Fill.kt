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

package com.patrykandpatrick.vico.core.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.ColorScale
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import kotlin.math.ceil

/**
 * Stores fill properties.
 *
 * @property color the color. If [shaderProvider] is not null, this is [Color.BLACK].
 * @property shaderProvider the [ShaderProvider].
 */
public class Fill
private constructor(
  public val color: Int,
  public val shaderProvider: ShaderProvider?,
  public val colorScale: ColorScale?,
) {
  /** Creates a color [Fill]. */
  public constructor(color: Int) : this(color = color, shaderProvider = null, colorScale = null)

  /** Creates a [ShaderProvider]&#0020;[Fill]. */
  public constructor(shaderProvider: ShaderProvider) : this(Color.BLACK, shaderProvider, null)

  /**
   * Creates a [ColorScale]&#0020;[Fill].
   *
   * @param colors TODO
   * @param alpha TODO
   * @param verticalAxisPosition TODO
   */
  public constructor(
    colors: (ExtraStore) -> Map<Double, Int>,
    alpha: (ExtraStore) -> Float = { 1f },
    verticalAxisPosition: Axis.Position.Vertical? = null,
  ) : this(Color.BLACK, null, ColorScale(colors, alpha, verticalAxisPosition))

  internal fun applyShader(paint: Paint, context: CartesianDrawingContext, bounds: RectF) {
    applyShader(paint, context, bounds.left, bounds.top, bounds.right, bounds.bottom)
  }

  internal fun applyShader(
    paint: Paint,
    context: CartesianDrawingContext,
    left: Float = context.layerBounds.left,
    top: Float = context.layerBounds.top,
    right: Float = context.layerBounds.right,
    bottom: Float = context.layerBounds.bottom,
  ) {
    paint.shader =
      shaderProvider?.getShader(context, left, top, right, bottom)
        ?: colorScale?.getColorScaleShader(context)
  }

  internal fun applyShader(
    paint: Paint,
    context: DrawingContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ) {
    paint.shader =
      shaderProvider?.getShader(context, left, top, right, bottom)
        ?: if (context is CartesianDrawingContext) {
          colorScale?.getColorScaleShader(context)
        } else {
          null
        }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is Fill && color == other.color && shaderProvider == other.shaderProvider

  override fun hashCode(): Int = 31 * color + shaderProvider?.hashCode().orZero

  /** Houses [Fill] singletons. */
  public companion object {
    /** A black [Fill]. */
    public val Black: Fill = Fill(Color.BLACK)

    /** A transparent [Fill]. */
    public val Transparent: Fill = Fill(Color.TRANSPARENT)
  }
}

private val canvas = Canvas()
private val paint = Paint()
private val cacheKeyNamespace = CacheStore.KeyNamespace()

internal fun Fill.extractColor(
  context: DrawingContext,
  significantY: Float,
  width: Float,
  height: Float,
  side: Int = 1,
): Int =
  if (shaderProvider != null || colorScale != null) {
    val bitmap = context.getBitmap(cacheKeyNamespace)
    canvas.setBitmap(bitmap)
    val correctedHeight = if (height <= 0f) canvas.height.toFloat() else height.coerceAtLeast(1f)
    val correctedWidth = if (width <= 0f) canvas.width.toFloat() else width.coerceAtLeast(1f)
    var bitmapY = if (side == 1) 0 else (correctedHeight - 1).toInt()
    if (shaderProvider != null) {
      paint.shader = shaderProvider.getShader(context, 0f, 0f, correctedWidth, correctedHeight)
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
