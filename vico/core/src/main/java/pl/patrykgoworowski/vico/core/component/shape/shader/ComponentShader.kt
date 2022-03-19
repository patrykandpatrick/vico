/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.component.shape.shader

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Shader
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.extension.half

/**
 * [ComponentShader] creates a [Shader] out of provided [component].
 *
 * @property component used as a pattern in the [Shader].
 * @property componentSizeDp the size of the [component] in dp unit.
 * @property checkeredArrangement whether the [component] will have checkered arrangement in the [Shader].
 * @property tileXMode The tiling mode for x to draw the [component] in.
 * @property tileYMode The tiling mode for y to draw the [component] in.
 */
public class ComponentShader(
    private val component: Component,
    private val componentSizeDp: Float,
    private val checkeredArrangement: Boolean = true,
    private val tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    private val tileYMode: Shader.TileMode = tileXMode,
) : CacheableDynamicShader() {

    override fun createShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader = with(context) {
        val size = componentSizeDp.pixels.toInt() * if (checkeredArrangement) 2 else 1
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        context.withOtherCanvas(canvas) {
            if (checkeredArrangement) {
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
        return BitmapShader(bitmap, tileXMode, tileYMode)
    }

    private fun Component.draw(
        context: DrawContext,
        x: Float,
        y: Float,
        size: Float,
    ) {
        draw(context, x, y, x + size, y + size)
    }
}
