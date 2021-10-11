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

package pl.patrykgoworowski.vico.compose.component.shape

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions

fun lineComponent(
    color: Color,
    thickness: Dp,
    shape: Shape = Shapes.rectShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
) = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
)

fun lineComponent(
    color: Color,
    thickness: Dp,
    shape: androidx.compose.ui.graphics.Shape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
) = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    dynamicShader = dynamicShader,
    shape = shape.chartShape(),
    margins = margins,
)
