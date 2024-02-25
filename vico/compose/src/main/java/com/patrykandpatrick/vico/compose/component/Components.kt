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

package com.patrykandpatrick.vico.compose.component

import android.graphics.Typeface
import android.text.Layout
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatrick.vico.compose.component.shape.toVicoShape
import com.patrykandpatrick.vico.compose.extension.pixelSize
import com.patrykandpatrick.vico.core.Defaults
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.OverlayingComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.dimensions.Dimensions
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions

/**
 * Creates and remembers a [LineComponent] with the specified properties.
 */
@Composable
public fun rememberLineComponent(
    color: Color = Color.Black,
    thickness: Dp = Defaults.COLUMN_WIDTH.dp,
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
    message = "Use `rememberOverlayingComponent`",
    replaceWith =
        ReplaceWith(
            expression =
                "rememberOverlayingComponent(outer, inner, innerPaddingStart, innerPaddingTop, " +
                    "innerPaddingEnd, innerPaddingBottom)",
            imports = arrayOf("com.patrykandpatrick.vico.compose.component.rememberOverlayingComponent"),
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
    rememberOverlayingComponent(
        outer = outer,
        inner = inner,
        innerPaddingStart = innerPaddingStart,
        innerPaddingTop = innerPaddingTop,
        innerPaddingBottom = innerPaddingBottom,
        innerPaddingEnd = innerPaddingEnd,
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
@Composable
public fun rememberOverlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingStart: Dp = 0.dp,
    innerPaddingTop: Dp = 0.dp,
    innerPaddingBottom: Dp = 0.dp,
    innerPaddingEnd: Dp = 0.dp,
): OverlayingComponent =
    remember(
        outer,
        inner,
        innerPaddingStart,
        innerPaddingTop,
        innerPaddingBottom,
        innerPaddingEnd,
    ) {
        OverlayingComponent(
            outer = outer,
            inner = inner,
            innerPaddingStartDp = innerPaddingStart.value,
            innerPaddingTopDp = innerPaddingTop.value,
            innerPaddingBottomDp = innerPaddingBottom.value,
            innerPaddingEndDp = innerPaddingEnd.value,
        )
    }

/**
 * Creates an [OverlayingComponent].
 *
 * @param outer the outer (background) [Component].
 * @param inner the inner (foreground) [Component].
 * @param innerPaddingAll the padding between the inner and outer components.
 */
@Composable
public fun rememberOverlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
): OverlayingComponent =
    rememberOverlayingComponent(
        outer = outer,
        inner = inner,
        innerPaddingStart = innerPaddingAll,
        innerPaddingTop = innerPaddingAll,
        innerPaddingBottom = innerPaddingAll,
        innerPaddingEnd = innerPaddingAll,
    )

/**
 * Creates an [OverlayingComponent].
 *
 * @param outer the outer (background) [Component].
 * @param inner the inner (foreground) [Component].
 * @param innerPaddingAll the padding between the inner and outer components.
 */
@Deprecated(
    message = "Use `rememberOverlayingComponent`",
    replaceWith =
        ReplaceWith(
            expression = "rememberOverlayingComponent(outer, inner, innerPaddingAll)",
            imports = arrayOf("com.patrykandpatrick.vico.compose.component.rememberOverlayingComponent"),
        ),
)
@Composable
public fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
): OverlayingComponent = rememberOverlayingComponent(outer = outer, inner = inner, innerPaddingAll = innerPaddingAll)

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
): TextComponent =
    remember(color, textSize, background, ellipsize, lineCount, padding, margins, typeface, textAlignment) {
        textComponent {
            this.color = color.toArgb()
            textSizeSp = textSize.pixelSize()
            this.ellipsize = ellipsize
            this.lineCount = lineCount
            this.background = background
            this.padding = padding
            this.margins = margins
            this.typeface = typeface
            this.textAlignment = textAlignment
        }
    }
