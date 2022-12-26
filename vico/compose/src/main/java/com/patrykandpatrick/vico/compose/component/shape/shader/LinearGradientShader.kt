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

import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.core.component.shape.shader.CacheableDynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.context.DrawContext

/**
 * Creates a [DynamicShader] in the form of a horizontal gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 * @param positions controls the position of each color on the gradient line. Each element of the array should belong to
 * the interval [[0, 1]], where 0 corresponds to the start of the gradient line, and 1 corresponds to the end of the
 * gradient line. If `null` (the default value) is passed, the colors will be distributed evenly along the gradient
 * line.
 */
public fun horizontalGradient(
    colors: Array<Color>,
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
            IntArray(colors.size) { index -> colors[index].toArgb() },
            positions,
            Shader.TileMode.CLAMP,
        )
}

/**
 * Creates a [DynamicShader] in the form of a vertical gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 * @param positions controls the position of each color on the gradient line. Each element of the array should belong to
 * the interval [[0, 1]], where 0 corresponds to the start of the gradient line, and 1 corresponds to the end of the
 * gradient line. If `null` (the default value) is passed, the colors will be distributed evenly along the gradient
 * line.
 */
public fun verticalGradient(
    colors: Array<Color>,
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
            IntArray(colors.size) { index -> colors[index].toArgb() },
            positions,
            Shader.TileMode.CLAMP,
        )
}
