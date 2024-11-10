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

package com.patrykandpatrick.vico.core.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.common.data.CacheStore

/** A [MeasuringContext] extension with a [Canvas] reference. */
public interface DrawingContext : MeasuringContext {
  /** The canvas to draw the chart on. */
  public val canvas: Canvas

  /**
   * Updates the value of [DrawingContext.canvas] to [canvas], runs [block], and restores the
   * previous [DrawingContext.canvas] value.
   */
  public fun withOtherCanvas(canvas: Canvas, block: () -> Unit)
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun DrawingContext(
  canvas: Canvas,
  density: Float = 1f,
  isLtr: Boolean = true,
  canvasBounds: RectF = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()),
  spToPx: (Float) -> Float = { it },
): DrawingContext =
  object : DrawingContext {
    override val canvasBounds: RectF = canvasBounds

    override var canvas: Canvas = canvas

    override val density: Float = density

    override val isLtr: Boolean = isLtr

    override val cacheStore: CacheStore = CacheStore()

    override fun withOtherCanvas(canvas: Canvas, block: () -> Unit) {
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
) =
  cacheStore
    .getOrNull<Bitmap>(cacheKeyNamespace, *cacheKeyComponents, canvas.width, canvas.height)
    ?.apply { eraseColor(Color.TRANSPARENT) }
    ?: Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888).also {
      cacheStore.set(
        cacheKeyNamespace,
        *cacheKeyComponents,
        canvas.width,
        canvas.height,
        value = it,
      )
    }
