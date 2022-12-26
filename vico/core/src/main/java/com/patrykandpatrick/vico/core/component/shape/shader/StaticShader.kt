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

import android.graphics.Shader
import com.patrykandpatrick.vico.core.context.DrawContext

/**
 * Creates a [DynamicShader], which always provides the same [Shader] instance.
 *
 * @property shader the [Shader] that will always be provided, regardless of the [provideShader] function’s arguments.
 */
public class StaticShader(private val shader: Shader) : DynamicShader {

    override fun provideShader(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader = shader
}

/**
 * Converts this [Shader] to a [StaticShader] and returns it as a [DynamicShader].
 */
public val Shader.dynamic: DynamicShader
    get() = StaticShader(this)
