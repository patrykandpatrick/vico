/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.component.shape.shader

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Shader
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.half

/**
 * [ComponentShader] creates a [Shader] out of the provided [component].
 *
 * @property component used as a pattern in the [Shader].
 * @property componentSizeDp the size of the [component] (in dp).
 * @property checkeredArrangement whether the [component] should be arranged in a checkered pattern in the [Shader].
 * @property tileXMode the horizontal tiling mode for the [component].
 * @property tileYMode the vertical tiling mode for the [component].
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
