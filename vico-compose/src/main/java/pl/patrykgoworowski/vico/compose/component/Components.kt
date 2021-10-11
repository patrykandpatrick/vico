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

package pl.patrykgoworowski.vico.compose.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.component.shape.chartShape
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.OverlayingComponent
import pl.patrykgoworowski.vico.core.component.shape.DashedShape
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions

typealias ChartShape = pl.patrykgoworowski.vico.core.component.shape.Shape

public fun columnComponent(
    color: Color,
    thickness: Dp = Dimens.COLUMN_WIDTH.dp,
    shape: Shape = RectangleShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    shape = shape.chartShape(),
    dynamicShader = dynamicShader,
    margins = margins,
)

public fun columnComponent(
    color: Color,
    thickness: Dp,
    shape: ChartShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    shape = shape,
    dynamicShader = dynamicShader,
    margins = margins,
)

fun shapeComponent(
    shape: Shape,
    color: Color,
    shader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): ShapeComponent<ChartShape> = ShapeComponent(
    shape = shape.chartShape(),
    color = color.toArgb(),
    dynamicShader = shader,
    margins = margins,
)

fun shapeComponent(
    shape: ChartShape,
    color: Color,
    shader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): ShapeComponent<ChartShape> = ShapeComponent(
    shape = shape,
    color = color.toArgb(),
    dynamicShader = shader,
    margins = margins,
)

fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
) = OverlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingAllDp = innerPaddingAll.value,
)

fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingStart: Dp = 0.dp,
    innerPaddingTop: Dp = 0.dp,
    innerPaddingBottom: Dp = 0.dp,
    innerPaddingEnd: Dp = 0.dp,
) = OverlayingComponent(
    outer = outer,
    inner = inner,
    insidePaddingStartDp = innerPaddingStart.value,
    insidePaddingTopDp = innerPaddingTop.value,
    insidePaddingBottomDp = innerPaddingBottom.value,
    insidePaddingEndDp = innerPaddingEnd.value,
)

fun dashedShape(
    shape: Shape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize
) = DashedShape(
    shape = shape.chartShape(),
    dashLengthDp = dashLength.value,
    gapLengthDp = gapLength.value,
    fitStrategy = fitStrategy,
)

fun dashedShape(
    shape: ChartShape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize
) = DashedShape(
    shape = shape,
    dashLengthDp = dashLength.value,
    gapLengthDp = gapLength.value,
    fitStrategy = fitStrategy,
)
