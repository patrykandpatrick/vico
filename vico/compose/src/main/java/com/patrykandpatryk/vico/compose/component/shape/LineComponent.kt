/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.compose.component.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.shape.Shape
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatryk.vico.core.dimensions.Dimensions
import com.patrykandpatryk.vico.core.dimensions.emptyDimensions

/**
 * Creates a [LineComponent].
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the line.
 * @param margins the margins of the line.
 */
public fun lineComponent(
    color: Color,
    thickness: Dp,
    shape: Shape = Shapes.rectShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates a [LineComponent].
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the line.
 * @param margins the margins of the line.
 */
public fun lineComponent(
    color: Color,
    thickness: Dp,
    shape: androidx.compose.ui.graphics.Shape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape.chartShape(),
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)
