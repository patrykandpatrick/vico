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

package com.patrykandpatrick.vico.multiplatform.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore

/** A [MeasuringContext] extension with a [Canvas] reference. */
public interface DrawingContext : MeasuringContext {
  /** The [Canvas]. */
  public val canvas: Canvas

  /**
   * Updates the value of [DrawingContext.canvas] to [canvas], runs [block], and restores the
   * previous [DrawingContext.canvas] value.
   */
  public fun withCanvas(canvas: Canvas, block: () -> Unit)
}

internal fun DrawingContext.saveLayer(opacity: Float) =
  canvas.saveLayer(Rect(Offset.Zero, canvasSize), Paint().apply { alpha = opacity })

private val clearPaint = Paint().apply { blendMode = BlendMode.Clear }

internal fun DrawingContext.getBitmap(
  cacheKeyNamespace: CacheStore.KeyNamespace,
  vararg cacheKeyComponents: Any,
) =
  cacheStore
    .getOrNull<Pair<ImageBitmap, Canvas>>(cacheKeyNamespace, *cacheKeyComponents, canvasSize)
    ?.apply { second.drawRect(canvasSize.toRect(), clearPaint) }
    ?: ImageBitmap(canvasSize.width.toInt(), canvasSize.height.toInt())
      .let { it to Canvas(it) }
      .also { pair ->
        cacheStore.set(cacheKeyNamespace, *cacheKeyComponents, canvasSize, value = pair)
      }
