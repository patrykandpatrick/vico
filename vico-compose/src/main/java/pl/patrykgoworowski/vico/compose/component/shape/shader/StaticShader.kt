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

package pl.patrykgoworowski.vico.compose.component.shape.shader

import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader

class StaticShader(private val brush: Brush) : DynamicShader {

    private var shader: Shader? = null

    override fun provideShader(
        bounds: RectF
    ): Shader = shader ?: kotlin.run {
        val tempPaint = Paint()
        brush.applyTo(
            size = Size(bounds.width(), bounds.height()),
            p = tempPaint,
            alpha = 1f,
        )
        requireNotNull(tempPaint.shader)
    }.also { shader = it }
}

val Brush.dynamic: DynamicShader
    get() = StaticShader(this)
