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

package com.patrykandpatrick.vico.compose.component.shape.shader

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import com.patrykandpatrick.vico.core.component.shape.shader.CacheableDynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.context.DrawContext
import kotlin.math.abs

/**
 * Creates a [DynamicShader] out of a [Brush].
 *
 * @property brush the [Brush] to be converted to a [Shader].
 */
public class BrushShader(private val brush: Brush) : CacheableDynamicShader() {

    private val matrix = Matrix()

    override fun createShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader {
        val tempPaint = Paint()
        brush.applyTo(
            size = Size(
                abs(left - right),
                abs(top - bottom),
            ),
            p = tempPaint,
            alpha = 1f,
        )
        return requireNotNull(tempPaint.shader).apply {
            matrix.postTranslate(left, top)
            setLocalMatrix(matrix)
            matrix.reset()
        }
    }
}

/**
 * Converts this [Brush] to a [DynamicShader] using [BrushShader].
 */
public fun Brush.toDynamicShader(): DynamicShader = BrushShader(this)
