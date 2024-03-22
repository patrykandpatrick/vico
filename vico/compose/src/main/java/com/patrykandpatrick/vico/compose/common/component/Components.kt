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
@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.patrykandpatrick.vico.compose.common.component

import android.graphics.Typeface
import android.text.Layout
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.common.pixelSize
import com.patrykandpatrick.vico.compose.common.shader.toDynamicShader
import com.patrykandpatrick.vico.compose.common.shape.toVicoShape
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.OverlayingComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.dimension.Dimensions
import com.patrykandpatrick.vico.core.common.dimension.MutableDimensions
import com.patrykandpatrick.vico.core.common.dimension.emptyDimensions
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.core.common.shape.Shapes

/**
 * Creates and remembers a [LineComponent] with the specified properties.
 */
@Composable
public fun rememberLineComponent(
    color: Color = Color.Black,
    thickness: Dp = Defaults.LINE_COMPONENT_THICKNESS_DP.dp,
    shape: Shape = Shapes.rectShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): LineComponent =
    remember(
        color,
        thickness,
        shape,
        dynamicShader,
        margins,
        strokeWidth,
        strokeColor,
    ) {
        LineComponent(
            color = color.toArgb(),
            thicknessDp = thickness.value,
            shape = shape,
            dynamicShader = dynamicShader,
            margins = margins,
            strokeWidthDp = strokeWidth.value,
            strokeColor = strokeColor.toArgb(),
        )
    }

/**
 * Creates and remembers a [LineComponent] with the specified properties.
 */
@Deprecated(
    message =
        "Use `rememberLineComponent` overload that takes `com.patrykandpatrick.vico.core.component.shape.Shape`. " +
            "Convert the Compose shape using `androidx.compose.ui.graphics.Shape.toVicoShape()`.",
    replaceWith =
        ReplaceWith(
            expression =
                "rememberLineComponent(color, thickness, shape.toVicoShape(), dynamicShader, margins, " +
                    "strokeWidth, strokeColor)",
            imports =
                arrayOf(
                    "com.patrykandpatrick.vico.compose.component.rememberLineComponent",
                    "com.patrykandpatrick.vico.compose.component.shape.toVicoShape",
                ),
        ),
)
@Composable
public fun rememberLineComponent(
    color: Color = Color.Black,
    thickness: Dp = Defaults.COLUMN_WIDTH.dp,
    shape: androidx.compose.ui.graphics.Shape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): LineComponent =
    rememberLineComponent(
        color = color,
        thickness = thickness,
        shape = shape.toVicoShape(),
        dynamicShader = dynamicShader,
        margins = margins,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
    )

/**
 * Creates and remembers a [ShapeComponent] with the specified properties.
 */
@Composable
public fun rememberShapeComponent(
    shape: Shape = Shapes.rectShape,
    color: Color = Color.Black,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent =
    remember(
        shape,
        color,
        dynamicShader,
        margins,
        strokeWidth,
        strokeColor,
    ) {
        ShapeComponent(
            shape = shape,
            color = color.toArgb(),
            dynamicShader = dynamicShader,
            margins = margins,
            strokeWidthDp = strokeWidth.value,
            strokeColor = strokeColor.toArgb(),
        )
    }

/**
 * Creates and remembers a [ShapeComponent] with the specified properties.
 */
@Deprecated(
    message =
        "Use `rememberLineComponent` which uses `com.patrykandpatrick.vico.core.component.shape.Shape`. " +
            "Convert the Compose shape using `androidx.compose.ui.graphics.Shape.toVicoShape()`.",
    replaceWith =
        ReplaceWith(
            expression =
                "rememberShapeComponent(shape = shape.toVicoShape(), color = color, " +
                    "dynamicShader = dynamicShader, margins = margins, strokeWidth = strokeWidth, " +
                    "strokeColor = strokeColor)",
            imports = arrayOf("com.patrykandpatrick.vico.compose.component.shape.toVicoShape"),
        ),
)
@Composable
public fun rememberShapeComponent(
    shape: androidx.compose.ui.graphics.Shape,
    color: Color = Color.Black,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent =
    rememberShapeComponent(
        shape = shape.toVicoShape(),
        color = color,
        dynamicShader = dynamicShader,
        margins = margins,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
    )

/**
 * Creates and remembers a [ShapeComponent] with the specified properties.
 */
@Composable
public fun rememberShapeComponent(
    shape: Shape = Shapes.rectShape,
    color: Color = Color.Black,
    brush: Brush,
    margins: Dimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent =
    rememberShapeComponent(
        shape = shape,
        color = color,
        dynamicShader = brush.toDynamicShader(),
        margins = margins,
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
    )

/**
 * Creates and remembers a [LayeredComponent].
 */
@Composable
public fun rememberLayeredComponent(
    rear: Component,
    front: Component,
    padding: Dimensions = emptyDimensions(),
): LayeredComponent =
    remember(rear, front, padding) {
        LayeredComponent(rear, front, padding)
    }

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
@Deprecated(
    message = "Use `rememberLayeredComponent`.",
    replaceWith =
        ReplaceWith(
            expression =
                "rememberLayeredComponent(rear = outer, front = inner, padding = dimensionsOf(start = " +
                    "innerPaddingStart, top = innerPaddingTop, end = innerPaddingEnd, bottom = innerPaddingBottom))",
            imports =
                arrayOf(
                    "com.patrykandpatrick.vico.compose.component.rememberLayeredComponent",
                    "com.patrykandpatrick.vico.compose.dimensions.dimensionsOf",
                ),
        ),
)
@Composable
public fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingStart: Dp = 0.dp,
    innerPaddingTop: Dp = 0.dp,
    innerPaddingBottom: Dp = 0.dp,
    innerPaddingEnd: Dp = 0.dp,
): OverlayingComponent =
    remember(outer, inner, innerPaddingStart, innerPaddingTop, innerPaddingEnd, innerPaddingBottom) {
        OverlayingComponent(
            outer,
            inner,
            innerPaddingStart.value,
            innerPaddingTop.value,
            innerPaddingEnd.value,
            innerPaddingBottom.value,
        )
    }

/**
 * Creates an [OverlayingComponent].
 *
 * @param outer the outer (background) [Component].
 * @param inner the inner (foreground) [Component].
 * @param innerPaddingAll the padding between the inner and outer components.
 */
@Deprecated(
    message = "Use `rememberLayeredComponent`.",
    replaceWith =
        ReplaceWith(
            expression =
                "rememberLayeredComponent(rear = outer, front = inner, padding = " +
                    "dimensionsOf(all = innerPaddingAll))",
            imports =
                arrayOf(
                    "com.patrykandpatrick.vico.compose.component.rememberLayeredComponent",
                    "com.patrykandpatrick.vico.compose.dimensions.dimensionsOf",
                ),
        ),
)
@Composable
public fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
): OverlayingComponent =
    overlayingComponent(outer, inner, innerPaddingAll, innerPaddingAll, innerPaddingAll, innerPaddingAll)

/**
 * Creates and remembers a [TextComponent].
 *
 * @param color the text color.
 * @param textSize the text size.
 * @param background an optional [ShapeComponent] to be displayed behind the text.
 * @param ellipsize the text truncation behavior.
 * @param lineCount the line count.
 * @param padding the padding between the text and the background.
 * @param margins the margins around the background.
 * @param typeface the [Typeface] for the text.
 * @param textAlignment the text alignment.
 * @param minWidth defines the minimum width.
 */
@Composable
public fun rememberTextComponent(
    color: Color = Color.Black,
    textSize: TextUnit = Defaults.TEXT_COMPONENT_TEXT_SIZE.sp,
    background: ShapeComponent? = null,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = Defaults.LABEL_LINE_COUNT,
    padding: MutableDimensions = emptyDimensions(),
    margins: MutableDimensions = emptyDimensions(),
    typeface: Typeface? = null,
    textAlignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    minWidth: TextComponent.MinWidth = TextComponent.MinWidth.fixed(),
): TextComponent =
    remember(color, textSize, background, ellipsize, lineCount, padding, margins, typeface, textAlignment, minWidth) {
        TextComponent.build {
            this.color = color.toArgb()
            textSizeSp = textSize.pixelSize()
            this.ellipsize = ellipsize
            this.lineCount = lineCount
            this.background = background
            this.padding = padding
            this.margins = margins
            this.typeface = typeface
            this.textAlignment = textAlignment
            this.minWidth = minWidth
        }
    }

/** A [Dp] version of [TextComponent.MinWidth.fixed]. */
@Stable
public fun TextComponent.MinWidth.Companion.fixed(value: Dp = 0.dp): TextComponent.MinWidth = fixed(value.value)
