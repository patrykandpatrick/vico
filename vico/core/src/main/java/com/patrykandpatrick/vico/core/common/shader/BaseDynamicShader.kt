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

package com.patrykandpatrick.vico.core.common.shader

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.data.CacheStore
import kotlin.math.roundToInt

/** A base [DynamicShader] implementation. This overrides [getColorAt]. */
public abstract class BaseDynamicShader : DynamicShader {
  private val cacheKeyNamespace = CacheStore.KeyNamespace()

  override fun getColorAt(point: Point, context: DrawContext, bounds: RectF): Int =
    context.cacheStore
      .getOrSet(cacheKeyNamespace) { toBitmap(context, bounds) }
      .getPixel(
        (point.x - bounds.left).toInt().coerceIn(0, bounds.width().toInt() - 1),
        (point.y - bounds.top).toInt().coerceIn(0, bounds.height().toInt() - 1),
      )
}

private fun DynamicShader.toBitmap(context: DrawContext, bounds: RectF): Bitmap {
  val width = bounds.width().roundToInt()
  val height = bounds.height().roundToInt()
  val paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = provideShader(context, 0f, 0f, width.toFloat(), height.toFloat())
    }
  return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
    Canvas(it).drawPaint(paint)
  }
}
