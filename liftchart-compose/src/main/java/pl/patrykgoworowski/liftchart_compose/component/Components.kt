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

package pl.patrykgoworowski.liftchart_compose.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.component.OverlayingComponent
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.emptyDimensions
import pl.patrykgoworowski.liftchart_common.path.DashedShape
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.path.chartShape

typealias ChartShape = pl.patrykgoworowski.liftchart_common.path.Shape

@Composable
public fun rectComponent(
    color: Color,
    thickness: Dp = DEF_BAR_WIDTH.dp,
    shape: Shape = RectangleShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thickness = thickness.pixels,
    shape = shape.chartShape(),
    dynamicShader = dynamicShader,
    margins = margins,
)

@Composable
public fun rectComponent(
    color: Color,
    thickness: Dp,
    shape: ChartShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
): LineComponent = LineComponent(
    color = color.toArgb(),
    thickness = thickness.pixels,
    shape = shape,
    dynamicShader = dynamicShader,
    margins = margins,
)

@Composable
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

@Composable
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

@Composable
fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
) = OverlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingAll = innerPaddingAll.pixels,
)

@Composable
fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingStart: Dp,
    innerPaddingTop: Dp,
    innerPaddingBottom: Dp,
    innerPaddingEnd: Dp,
) = OverlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingStart = innerPaddingStart.pixels,
    innerPaddingTop = innerPaddingTop.pixels,
    innerPaddingBottom = innerPaddingBottom.pixels,
    innerPaddingEnd = innerPaddingEnd.pixels,
)

@Composable
fun dashedShape(
    shape: Shape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize
) = DashedShape(
    shape = shape.chartShape(),
    dashLength = dashLength.pixels,
    gapLength = gapLength.pixels,
    fitStrategy = fitStrategy,
)

@Composable
fun dashedShape(
    shape: ChartShape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize
) = DashedShape(
    shape = shape,
    dashLength = dashLength.pixels,
    gapLength = gapLength.pixels,
    fitStrategy = fitStrategy,
)