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
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import com.patrykandpatrick.vico.core.common.DrawContext

/**
 * Splits the shaded area into two parts and applies two other [DynamicShader]s, [topShader] and [bottomShader].
 * [splitY] expresses the distance of the split from the top of the shaded area as a fraction of the area’s height.
 */
public class TopBottomShader(
    public var topShader: DynamicShader,
    public var bottomShader: DynamicShader,
    public var splitY: Float = 0f,
) : CacheableDynamicShader() {
    private val paint = Paint()

    override fun createShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader {
        val width = (right - left).toInt()
        val height = (bottom - top).toInt()
        if (width == 0 || height == 0) return EmptyBitmapShader
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (splitY > 0f) {
            paint.shader =
                topShader.provideShader(
                    context = context,
                    left = left,
                    top = 0f,
                    right = right,
                    bottom = height * splitY,
                )
            canvas.drawRect(0f, 0f, width.toFloat(), height * splitY, paint)
        }
        if (splitY < 1f) {
            paint.shader =
                bottomShader.provideShader(
                    context = context,
                    left = left,
                    top = height * splitY,
                    right = right,
                    bottom = height.toFloat(),
                )
            canvas.drawRect(0f, height * splitY, width.toFloat(), height.toFloat(), paint)
        }
        return BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            .apply { setLocalMatrix(Matrix().apply { postTranslate(left, top) }) }
    }

    override fun createKey(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): String = "${super.createKey(left, top, right, bottom)},${topShader.hashCode()},${bottomShader.hashCode()}"

    override fun equals(other: Any?): Boolean =
        this === other || other is TopBottomShader &&
            topShader == other.topShader && bottomShader == other.bottomShader && splitY == other.splitY

    override fun hashCode(): Int = 31 * topShader.hashCode() + bottomShader.hashCode()

    private companion object {
        val EmptyBitmapShader =
            BitmapShader(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
}
