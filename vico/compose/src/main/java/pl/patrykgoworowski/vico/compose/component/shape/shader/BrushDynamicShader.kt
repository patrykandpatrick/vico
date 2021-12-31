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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import kotlin.math.abs
import pl.patrykgoworowski.vico.core.component.shape.shader.CacheableDynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.context.DrawContext

public fun brushShader(
    shader: (
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) -> Brush
): DynamicShader = object : CacheableDynamicShader() {

    private val tempPaint = Paint()

    override fun createShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): android.graphics.Shader {
        shader(left, top, right, bottom)
            .applyTo(
                size = Size(
                    abs(left - right),
                    abs(top - bottom)
                ),
                p = tempPaint,
                alpha = 1f
            )
        return requireNotNull(tempPaint.shader)
    }
}
