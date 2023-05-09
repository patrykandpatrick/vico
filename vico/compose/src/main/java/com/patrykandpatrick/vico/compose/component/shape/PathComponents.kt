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

package com.patrykandpatrick.vico.compose.component.shape

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.component.shape.PathComponent
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader

/**
 * Creates and remembers a [PathComponent].
 *
 * @param color the background color.
 * @param dynamicShader an optional [DynamicShader] to apply to the path.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 */
@Composable
public fun pathComponent(
    color: Color = Color.Black,
    dynamicShader: DynamicShader? = null,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): PathComponent = remember {
    PathComponent(
        color = color.toArgb(),
        dynamicShader = dynamicShader,
        strokeWidthDp = strokeWidth.value,
        strokeColor = strokeColor.toArgb(),
    )
}.apply {
    this.color = color.toArgb()
    this.dynamicShader = dynamicShader
    this.strokeWidthDp = strokeWidth.value
    this.strokeColor = strokeColor.toArgb()
}
