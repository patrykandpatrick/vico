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

package pl.patrykgoworowski.vico.view.component.shape.shader

import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.shader.CacheableDynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shader.ComponentShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders

fun DynamicShaders.fromComponent(
    component: Component,
    componentSize: Float,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
) = ComponentShader(
    component = component,
    componentSize = componentSize,
    checkeredArrangement = checkeredArrangement,
    tileXMode = tileXMode,
    tileYMode = tileYMode,
)

public fun DynamicShaders.horizontalGradient(
    vararg colors: Int,
) = horizontalGradient(colors)

public fun DynamicShaders.horizontalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(bounds: RectF): Shader =
        LinearGradient(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.top,
            colors,
            positions,
            Shader.TileMode.CLAMP,
        )
}

public fun DynamicShaders.verticalGradient(
    vararg colors: Int,
) = verticalGradient(colors)

public fun DynamicShaders.verticalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(bounds: RectF): Shader =
        LinearGradient(
            bounds.left,
            bounds.top,
            bounds.left,
            bounds.bottom,
            colors,
            positions,
            Shader.TileMode.CLAMP,
        )
}
