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

package com.patrykandpatrick.vico.views.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import com.patrykandpatrick.vico.views.common.data.CacheStore
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import kotlin.math.roundToInt

/** A [MeasuringContext] extension with a [Canvas] reference. */
public interface DrawingContext : MeasuringContext {
  /** The canvas to draw the chart on. */
  public val canvas: Canvas

  /**
   * Updates the value of [DrawingContext.canvas] to [canvas], runs [block], and restores the
   * previous [DrawingContext.canvas] value.
   */
  public fun withCanvas(canvas: Canvas, block: () -> Unit)
}

internal fun DrawingContext(
  canvas: Canvas,
  density: Float = 1f,
  isLtr: Boolean = true,
  spToPx: (Float) -> Float = { it },
): DrawingContext =
  object : DrawingContext {
    override val canvasSize: Size = MutableSize(canvas.width.toFloat(), canvas.height.toFloat())

    override var canvas: Canvas = canvas

    override val density: Float = density

    override val extraStore: ExtraStore = ExtraStore.Empty

    override val isLtr: Boolean = isLtr

    override val cacheStore: CacheStore = CacheStore()

    override fun withCanvas(canvas: Canvas, block: () -> Unit) {
      val originalCanvas = this.canvas
      this.canvas = canvas
      block()
      this.canvas = originalCanvas
    }

    override fun spToPx(sp: Float): Float = spToPx(sp)
  }

internal fun DrawingContext.getBitmap(
  cacheKeyNamespace: CacheStore.KeyNamespace,
  vararg cacheKeyComponents: Any,
): Bitmap {
  val width = canvasSize.width.roundToInt()
  val height = canvasSize.height.roundToInt()
  return cacheStore
    .getOrNull<Bitmap>(cacheKeyNamespace, *cacheKeyComponents, width, height)
    ?.apply { eraseColor(Color.TRANSPARENT) }
    ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
      cacheStore.set(cacheKeyNamespace, *cacheKeyComponents, width, height, value = it)
    }
}
