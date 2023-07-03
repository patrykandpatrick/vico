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

package com.patrykandpatrick.vico.compose.component.shape

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.dimensions.Dimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions

/**
 * Creates a [LineComponent].
 *
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the line.
 * @param margins the margins of the line.
 */
@Composable
@Deprecated(
    message = "Use `com.patrykandpatrick.vico.compose.component.lineComponent` instead.",
    level = DeprecationLevel.ERROR,
)
public fun lineComponent(
    color: Color,
    thickness: Dp,
    shape: Shape = Shapes.rectShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = com.patrykandpatrick.vico.compose.component.lineComponent(
    color = color,
    thickness = thickness,
    shape = shape,
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)

/**
 * Creates a [LineComponent].
 *
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the line.
 * @param margins the margins of the line.
 */
@Composable
@Deprecated(
    message = "Use `com.patrykandpatrick.vico.compose.component.lineComponent` instead.",
    level = DeprecationLevel.ERROR,
)
public fun lineComponent(
    color: Color,
    thickness: Dp,
    shape: androidx.compose.ui.graphics.Shape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = com.patrykandpatrick.vico.compose.component.lineComponent(
    color = color,
    thickness = thickness,
    shape = shape,
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)
