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

@file:Suppress("UnusedReceiverParameter")

package com.patrykandpatrick.vico.compose.component.shape.shader

import android.graphics.Shader
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.ColorShader
import com.patrykandpatrick.vico.core.component.shape.shader.ComponentShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.component.shape.shader.LinearGradientShader

/**
 * Creates a [ComponentShader] out of the provided [component].
 *
 * @property component used as a pattern in the [Shader].
 * @property componentSize the size of the [component].
 * @property checkeredArrangement whether the [component] will be arranged in a checkered pattern.
 * @property tileXMode the horizontal tiling mode for the [component].
 * @property tileYMode the vertical tiling mode for the [component].
 */
public fun DynamicShaders.fromComponent(
    component: Component,
    componentSize: Dp,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
): ComponentShader =
    ComponentShader(
        component = component,
        componentSizeDp = componentSize.value,
        checkeredArrangement = checkeredArrangement,
        tileXMode = tileXMode,
        tileYMode = tileYMode,
    )

/**
 * Creates a [BrushShader] using the given [Brush].
 *
 * @see BrushShader
 */
public fun DynamicShaders.fromBrush(brush: Brush): BrushShader = BrushShader(brush)

/**
 * Creates a [ColorShader].
 */
public fun DynamicShaders.color(color: Color): ColorShader = ColorShader(color.toArgb())

/**
 * Creates a [DynamicShader] with a horizontal gradient. [colors] houses the gradient colors, and [positions] specifies
 * the color offsets (between 0 and 1), with `null` producing an even distribution.
 */
public fun DynamicShaders.horizontalGradient(
    colors: Array<Color>,
    positions: FloatArray? = null,
): DynamicShader = LinearGradientShader(IntArray(colors.size) { colors[it].toArgb() }, positions, true)

/**
 * Creates a [DynamicShader] with a vertical gradient. [colors] houses the gradient colors, and [positions] specifies
 * the color offsets (between 0 and 1), with `null` producing an even distribution.
 */
public fun DynamicShaders.verticalGradient(
    colors: Array<Color>,
    positions: FloatArray? = null,
): DynamicShader = LinearGradientShader(IntArray(colors.size) { colors[it].toArgb() }, positions, false)
