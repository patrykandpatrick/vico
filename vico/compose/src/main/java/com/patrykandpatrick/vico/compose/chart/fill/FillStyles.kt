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

package com.patrykandpatrick.vico.compose.chart.fill

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatrick.vico.core.chart.fill.FillStyle
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader

/**
 * Creates a [FillStyle] that fills the area with a solid color.
 */
public fun FillStyle.Companion.solid(color: Color): FillStyle.Solid = FillStyle.Solid(color.toArgb())

/**
 * Creates a [FillStyle] with split colors. The positive color is used for values above the zero line, and the negative
 * color is used for values below the zero line.
 */
public fun FillStyle.Companion.split(positiveColor: Color, negativeColor: Color): FillStyle.Split =
    FillStyle.Split(positiveColor.toArgb(), negativeColor.toArgb())

/**
 * Creates a [FillStyle] with a [DynamicShader].
 */
public fun FillStyle.Companion.shader(shader: DynamicShader): FillStyle.Shader<DynamicShader> =
    FillStyle.Shader(shader)

/**
 * Creates a [FillStyle] with a [DynamicShader] that is split into two shaders. The positive shader is used for values
 * above the zero line, and the negative shader is used for values below the zero line.
 */
public fun FillStyle.Companion.splitShader(
    positiveShader: DynamicShader,
    negativeShader: DynamicShader,
): FillStyle.SplitShader =
    FillStyle.SplitShader(positiveShader, negativeShader)

/**
 * Creates a [FillStyle] with a [Brush].
 */
public fun FillStyle.Companion.brush(brush: Brush): FillStyle.Shader<DynamicShader> =
    FillStyle.Shader(brush.toDynamicShader())
