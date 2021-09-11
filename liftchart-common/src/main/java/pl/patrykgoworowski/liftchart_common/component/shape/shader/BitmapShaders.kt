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

package pl.patrykgoworowski.liftchart_common.component.shape.shader

import android.graphics.*
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.extension.half

fun bitmapShader(
    bitmap: Bitmap,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
): DynamicShader = object : CacheableDynamicShader() {
    override fun createShader(bounds: RectF): Shader =
        BitmapShader(bitmap, tileXMode, tileYMode)
}

fun componentShader(
    component: Component,
    componentSize: Float,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
): DynamicShader = object : CacheableDynamicShader() {
    override fun createShader(bounds: RectF): Shader {
        val size = componentSize.toInt() * if (checkeredArrangement) 2 else 1
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        if (checkeredArrangement) {
            val halfSize = componentSize.half
            canvas.clipRect(0f, 0f, size.toFloat(), size.toFloat())
            component.draw(canvas, -halfSize, -halfSize, componentSize)
            component.draw(canvas, -halfSize, size - halfSize, componentSize)
            component.draw(canvas, size - halfSize, -halfSize, componentSize)
            component.draw(canvas, size - halfSize, size - halfSize, componentSize)
            component.draw(canvas, halfSize, halfSize, componentSize)
        } else {
            component.draw(canvas, 0f, 0f, componentSize, componentSize)
        }
        return BitmapShader(bitmap, tileXMode, tileYMode)
    }
}

private fun Component.draw(
    canvas: Canvas,
    x: Float,
    y: Float,
    size: Float
) {
    draw(canvas, x, y, x + size, y + size)
}