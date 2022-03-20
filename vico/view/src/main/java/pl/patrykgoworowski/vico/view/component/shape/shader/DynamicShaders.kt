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

@file:Suppress("unused")

package pl.patrykgoworowski.vico.view.component.shape.shader

import android.graphics.LinearGradient
import android.graphics.Shader
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.shader.CacheableDynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shader.ComponentShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders
import pl.patrykgoworowski.vico.core.context.DrawContext

/**
 * Creates a [ComponentShader] out of given [component].
 *
 * @param component used as a pattern in the [Shader].
 * @param componentSizeDp the size of the [component] in dp unit.
 * @param checkeredArrangement whether the [component] will have checkered arrangement in the [Shader].
 * @param tileXMode The tiling mode for x to draw the [component] in.
 * @param tileYMode The tiling mode for y to draw the [component] in.
 */
public fun DynamicShaders.fromComponent(
    component: Component,
    componentSizeDp: Float,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
): ComponentShader = ComponentShader(
    component = component,
    componentSizeDp = componentSizeDp,
    checkeredArrangement = checkeredArrangement,
    tileXMode = tileXMode,
    tileYMode = tileYMode,
)

/**
 * Creates a [DynamicShader] in form of a horizontal gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 */
public fun DynamicShaders.horizontalGradient(
    vararg colors: Int,
): DynamicShader = horizontalGradient(colors)

/**
 * Creates a [DynamicShader] in form of a horizontal gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 * @param positions May be null. The relative positions [0..1] of each corresponding color in the colors array.
 * If this is null, the the colors are distributed evenly along the gradient line.
 */
public fun DynamicShaders.horizontalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader =
        LinearGradient(
            left,
            top,
            right,
            top,
            colors,
            positions,
            Shader.TileMode.CLAMP,
        )

    override fun createKey(left: Float, top: Float, right: Float, bottom: Float): String =
        "%s,%s".format(left, right)
}

/**
 * Creates a [DynamicShader] in form of a vertical gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 */
public fun DynamicShaders.verticalGradient(
    vararg colors: Int,
): DynamicShader = verticalGradient(colors)

/**
 * Creates a [DynamicShader] in form of a vertical gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 * @param positions May be null. The relative positions [0..1] of each corresponding color in the colors array.
 * If this is null, the the colors are distributed evenly along the gradient line.
 */
public fun DynamicShaders.verticalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = object : CacheableDynamicShader() {

    override fun createShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader =
        LinearGradient(
            left,
            top,
            left,
            bottom,
            colors,
            positions,
            Shader.TileMode.CLAMP,
        )

    override fun createKey(left: Float, top: Float, right: Float, bottom: Float): String =
        "%s,%s".format(top, bottom)
}
