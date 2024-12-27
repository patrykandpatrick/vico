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
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Shader
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.half

internal data class ComponentShaderProvider(
  private val component: Component,
  private val componentSizeDp: Float,
  private val checker: Boolean = true,
  private val xTileMode: Shader.TileMode = Shader.TileMode.REPEAT,
  private val yTileMode: Shader.TileMode = xTileMode,
) : CachingShaderProvider() {
  override fun createShader(
    context: DrawingContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ): Shader =
    with(context) {
      val size = componentSizeDp.pixels.toInt() * if (checker) 2 else 1
      val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

      withCanvas(Canvas(bitmap)) {
        if (checker) {
          val halfSize = componentSizeDp.pixels.half
          with(component) {
            draw(context, -halfSize, -halfSize, componentSizeDp.pixels)
            draw(context, -halfSize, size - halfSize, componentSizeDp.pixels)
            draw(context, size - halfSize, -halfSize, componentSizeDp.pixels)
            draw(context, size - halfSize, size - halfSize, componentSizeDp.pixels)
            draw(context, halfSize, halfSize, componentSizeDp.pixels)
          }
        } else {
          component.draw(context, 0f, 0f, componentSizeDp.pixels, componentSizeDp.pixels)
        }
      }
      return BitmapShader(bitmap, xTileMode, yTileMode)
    }

  private fun Component.draw(context: DrawingContext, x: Float, y: Float, size: Float) {
    draw(context, x, y, x + size, y + size)
  }
}
