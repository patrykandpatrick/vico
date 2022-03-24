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

@file:Suppress("Unused")

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

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.component.ChartShape
import pl.patrykgoworowski.vico.core.DEF_MARKER_TICK_SIZE
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.component.shape.DashedShape
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.cornered.Corner
import pl.patrykgoworowski.vico.core.component.shape.cornered.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.cornered.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.cornered.MarkerCorneredShape
import pl.patrykgoworowski.vico.core.component.shape.cornered.RoundedCornerTreatment
import pl.patrykgoworowski.vico.core.context.DrawContext
import androidx.compose.ui.graphics.Shape as ComposeShape

private const val RADII_ARRAY_SIZE = 8

/**
 * Converts [androidx.compose.ui.graphics.Shape] to [pl.patrykgoworowski.vico.core.component.shape.Shape].
 */
public fun ComposeShape.chartShape(): Shape = object : Shape {
    private val radii by lazy { FloatArray(RADII_ARRAY_SIZE) }
    private val matrix: Matrix by lazy { Matrix() }

    override fun drawShape(
        context: DrawContext,
        paint: Paint,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        val outline = createOutline(
            size = Size(
                width = right - left,
                height = bottom - top,
            ),
            layoutDirection = if (context.isLtr) LayoutDirection.Ltr else LayoutDirection.Rtl,
            density = Density(context.density, context.fontScale),
        )
        when (outline) {
            is Outline.Rectangle -> path.addRect(
                left,
                top,
                right,
                bottom,
                Path.Direction.CCW
            )
            is Outline.Rounded -> path.addRoundRect(
                left = left,
                top = top,
                right = right,
                bottom = bottom,
                rect = outline.roundRect,
                radii = radii
            )
            is Outline.Generic -> {
                matrix.setTranslate(left, top)
                path.addPath(outline.path.asAndroidPath(), matrix)
            }
        }
        context.canvas.drawPath(path, paint)
    }
}

/**
 * Adds a rounded rectangle to the receiver [Path].
 *
 * @param left the left edge of the rectangle.
 * @param top the top edge of the rectangle.
 * @param right the right edge of the rectangle.
 * @param bottom the bottom edge of the rectangle.
 * @param rect the source rect whose values will be read.
 * @param radii a mutable [FloatArray] that stores the corner radii.
 */
@Suppress("MagicNumber")
@LongParameterListDrawFunction
public fun Path.addRoundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    rect: RoundRect,
    radii: FloatArray,
) {
    radii[0] = rect.topLeftCornerRadius.x
    radii[1] = rect.topLeftCornerRadius.y
    radii[2] = rect.topRightCornerRadius.x
    radii[3] = rect.topRightCornerRadius.y
    radii[4] = rect.bottomRightCornerRadius.x
    radii[5] = rect.bottomRightCornerRadius.y
    radii[6] = rect.bottomLeftCornerRadius.x
    radii[7] = rect.bottomLeftCornerRadius.y
    addRoundRect(left, top, right, bottom, radii, Path.Direction.CCW)
}

/**
 * Creates a [CorneredShape] with rounded corners of the provided size.
 */
public fun Shapes.roundedCornerShape(
    all: Dp = 0.dp,
): CorneredShape = CorneredShape(
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
)

/**
 * Creates a [CorneredShape] with rounded corners of the provided sizes.
 */
public fun Shapes.roundedCornerShape(
    topLeft: Dp = 0.dp,
    topRight: Dp = 0.dp,
    bottomRight: Dp = 0.dp,
    bottomLeft: Dp = 0.dp,
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft.value, RoundedCornerTreatment),
    Corner.Absolute(topRight.value, RoundedCornerTreatment),
    Corner.Absolute(bottomRight.value, RoundedCornerTreatment),
    Corner.Absolute(bottomLeft.value, RoundedCornerTreatment),
)

/**
 * Creates a [CorneredShape] with cut corners of the provided size.
 */
public fun Shapes.cutCornerShape(
    all: Dp = 0.dp,
): CorneredShape = CorneredShape(
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
)

/**
 * Creates a [CorneredShape] with cut corners of the provided sizes.
 */
public fun Shapes.cutCornerShape(
    topLeft: Dp = 0.dp,
    topRight: Dp = 0.dp,
    bottomRight: Dp = 0.dp,
    bottomLeft: Dp = 0.dp,
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft.value, CutCornerTreatment),
    Corner.Absolute(topRight.value, CutCornerTreatment),
    Corner.Absolute(bottomRight.value, CutCornerTreatment),
    Corner.Absolute(bottomLeft.value, CutCornerTreatment),
)

/**
 * Creates a [MarkerCorneredShape].
 *
 * @param topLeft the size and look of the top-left corner.
 * @param topRight the size and look of the top-right corner.
 * @param bottomRight the size and look of the bottom-right corner.
 * @param bottomLeft the size and look of the bottom-left corner.
 * @param tickSizeDp the tick size.
 */
public fun Shapes.markerCorneredShape(
    topLeft: Corner,
    topRight: Corner,
    bottomRight: Corner,
    bottomLeft: Corner,
    tickSizeDp: Dp = DEF_MARKER_TICK_SIZE.dp,
): MarkerCorneredShape = MarkerCorneredShape(
    topLeft = topLeft,
    topRight = topRight,
    bottomRight = bottomRight,
    bottomLeft = bottomLeft,
    tickSizeDp = tickSizeDp.value
)

/**
 * Creates a [MarkerCorneredShape].
 *
 * @param all the size and look of all corners.
 * @param tickSizeDp the tick size.
 */
public fun Shapes.markerCorneredShape(
    all: Corner,
    tickSizeDp: Dp = DEF_MARKER_TICK_SIZE.dp,
): MarkerCorneredShape = MarkerCorneredShape(
    topLeft = all,
    topRight = all,
    bottomRight = all,
    bottomLeft = all,
    tickSizeDp = tickSizeDp.value
)

/**
 * Creates a [MarkerCorneredShape] out of another [CorneredShape].
 *
 * @param corneredShape the source of each corner size and style.
 * @param tickSizeDp the tick size.
 */
public fun Shapes.markerCorneredShape(
    corneredShape: CorneredShape,
    tickSizeDp: Dp = DEF_MARKER_TICK_SIZE.dp,
): MarkerCorneredShape = MarkerCorneredShape(
    topLeft = corneredShape.topLeft,
    topRight = corneredShape.topRight,
    bottomRight = corneredShape.bottomRight,
    bottomLeft = corneredShape.bottomLeft,
    tickSizeDp = tickSizeDp.value
)

/**
 * Creates a [DashedShape].
 * @param shape the base [Shape] from which to create the [DashedShape].
 * @param dashLength the dash length.
 * @param gapLength the gap length.
 * @param fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public fun Shapes.dashedShape(
    shape: androidx.compose.ui.graphics.Shape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize,
): DashedShape = DashedShape(
    shape = shape.chartShape(),
    dashLengthDp = dashLength.value,
    gapLengthDp = gapLength.value,
    fitStrategy = fitStrategy,
)

/**
 * Creates a [DashedShape].
 * @param shape the base [ChartShape] from which to create the [DashedShape].
 * @param dashLength the dash length.
 * @param gapLength the gap length.
 * @param fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public fun Shapes.dashedShape(
    shape: ChartShape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize,
): DashedShape = DashedShape(
    shape = shape,
    dashLengthDp = dashLength.value,
    gapLengthDp = gapLength.value,
    fitStrategy = fitStrategy,
)
