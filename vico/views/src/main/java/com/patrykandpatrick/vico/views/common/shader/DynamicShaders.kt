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

package com.patrykandpatrick.vico.views.common.shader

import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shader.DynamicShaders
import com.patrykandpatrick.vico.core.common.shader.LinearGradientShader

/**
 * Creates a [DynamicShader] in the form of a horizontal gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 */
public fun DynamicShaders.horizontalGradient(vararg colors: Int): DynamicShader = horizontalGradient(colors)

/**
 * Creates a [DynamicShader] in the form of a horizontal gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 * @param positions controls the position of each color on the gradient line. Each element of the array should belong to
 * the interval [[0, 1]], where 0 corresponds to the start of the gradient line, and 1 corresponds to the end of the
 * gradient line. If `null` (the default value) is passed, the colors will be distributed evenly along the gradient
 * line.
 */
public fun DynamicShaders.horizontalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = LinearGradientShader(colors, positions, true)

/**
 * Creates a [DynamicShader] in the form of a vertical gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 */
public fun DynamicShaders.verticalGradient(vararg colors: Int): DynamicShader = verticalGradient(colors)

/**
 * Creates a [DynamicShader] in the form of a vertical gradient.
 *
 * @param colors the sRGB colors to be distributed along the gradient line.
 * @param positions controls the position of each color on the gradient line. Each element of the array should belong to
 * the interval [[0, 1]], where 0 corresponds to the start of the gradient line, and 1 corresponds to the end of the
 * gradient line. If `null` (the default value) is passed, the colors will be distributed evenly along the gradient
 * line.
 */
public fun DynamicShaders.verticalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
): DynamicShader = LinearGradientShader(colors, positions, false)
