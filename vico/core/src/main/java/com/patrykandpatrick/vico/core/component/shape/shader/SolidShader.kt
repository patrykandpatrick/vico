/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.model.Point

/**
 * A [DynamicShader] that fills the area with the solid [color].
 */
public class SolidShader(public val color: Int) : DynamicShader {
    private val shader: Shader = getShaderImplementation()

    private fun getShaderImplementation(): Shader {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RuntimeShader(SHADER_CODE).apply {
                setColorUniform(INPUT_COLOR, color)
            }
        } else {
            val bitmap =
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                    setPixel(0, 0, color)
                }
            BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
    }

    override fun provideShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader = shader

    override fun applyTo(
        paint: Paint,
        drawContext: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        zeroLineYFraction: Float,
    ) {
        paint.shader = shader
    }

    override fun getColorAt(
        point: Point,
        drawContext: DrawContext,
        rectF: RectF,
        zeroLineYFraction: Float,
    ): Int = color

    private companion object {
        private const val INPUT_COLOR = "inputColor"

        private val SHADER_CODE =
            """
            layout(color) uniform vec4 $INPUT_COLOR;
            vec4 main(vec2 canvas_coordinates) {
                return $INPUT_COLOR;
            }
            """.trimIndent()
    }
}
