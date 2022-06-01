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

package com.patrykandpatryk.vico.compose.component

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.component.shape.chartShape
import com.patrykandpatryk.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.component.Component
import com.patrykandpatryk.vico.core.component.OverlayingComponent
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatryk.vico.core.dimensions.Dimensions
import com.patrykandpatryk.vico.core.dimensions.emptyDimensions

public typealias ChartShape = com.patrykandpatryk.vico.core.component.shape.Shape

/**
 * Creates a [LineComponent] with the specified properties.
 */
public fun lineComponent(
    color: Color = Color.Black,
    thickness: Dp = DefaultDimens.COLUMN_WIDTH.dp,
    shape: Shape = RectangleShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    shape = shape.chartShape(),
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates a [LineComponent] with the specified properties.
 */
public fun lineComponent(
    color: Color = Color.Black,
    thickness: Dp,
    shape: ChartShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thicknessDp = thickness.value,
    shape = shape,
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates a [ShapeComponent] with the specified properties.
 */
public fun shapeComponent(
    shape: Shape,
    color: Color = Color.Black,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent = ShapeComponent(
    shape = shape.chartShape(),
    color = color.toArgb(),
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates a [ShapeComponent] with the specified properties.
 */
public fun shapeComponent(
    shape: ChartShape = Shapes.rectShape,
    color: Color = Color.Black,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent = ShapeComponent(
    shape = shape,
    color = color.toArgb(),
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates a [ShapeComponent] with the specified properties.
 */
public fun shapeComponent(
    shape: ChartShape = Shapes.rectShape,
    color: Color = Color.Black,
    brush: Brush,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent = ShapeComponent(
    shape = shape,
    color = color.toArgb(),
    dynamicShader = brush.toDynamicShader(),
    margins = margins,
    strokeWidthDp = strokeWidth.value,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates an [OverlayingComponent].
 *
 * @param outer the outer (background) [Component].
 * @param inner the inner (foreground) [Component].
 * @param innerPaddingAll the padding between the inner and outer components.
 */
public fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
): OverlayingComponent = OverlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingAllDp = innerPaddingAll.value,
)

/**
 * Creates an [OverlayingComponent].
 *
 * @param outer the outer (background) [Component].
 * @param inner the inner (foreground) [Component].
 * @property innerPaddingStart the start padding between the inner and outer components.
 * @property innerPaddingTop the top padding between the inner and outer components.
 * @property innerPaddingEnd the end padding between the inner and outer components.
 * @property innerPaddingBottom the bottom padding between the inner and outer components.
 */
public fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingStart: Dp = 0.dp,
    innerPaddingTop: Dp = 0.dp,
    innerPaddingBottom: Dp = 0.dp,
    innerPaddingEnd: Dp = 0.dp,
): OverlayingComponent = OverlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingStartDp = innerPaddingStart.value,
    innerPaddingTopDp = innerPaddingTop.value,
    innerPaddingBottomDp = innerPaddingBottom.value,
    innerPaddingEndDp = innerPaddingEnd.value,
)
