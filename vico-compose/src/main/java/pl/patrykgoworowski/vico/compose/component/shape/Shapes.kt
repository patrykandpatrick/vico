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
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.corner.Corner
import pl.patrykgoworowski.vico.core.component.shape.corner.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.corner.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.corner.RoundedCornerTreatment
import pl.patrykgoworowski.vico.core.draw.DrawContext
import androidx.compose.ui.graphics.Shape as ComposeShape

private const val RADII_ARRAY_SIZE = 8

fun ComposeShape.chartShape(): Shape = object : Shape {
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

@Suppress("MagicNumber")
@LongParameterListDrawFunction
fun Path.addRoundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    rect: RoundRect,
    radii: FloatArray
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

fun Shapes.roundedCornerShape(
    all: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
    Corner.Absolute(all.value, RoundedCornerTreatment),
)

fun Shapes.roundedCornerShape(
    topLeft: Dp = 0.dp,
    topRight: Dp = 0.dp,
    bottomRight: Dp = 0.dp,
    bottomLeft: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(topLeft.value, RoundedCornerTreatment),
    Corner.Absolute(topRight.value, RoundedCornerTreatment),
    Corner.Absolute(bottomRight.value, RoundedCornerTreatment),
    Corner.Absolute(bottomLeft.value, RoundedCornerTreatment),
)

fun Shapes.cutCornerShape(
    all: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
    Corner.Absolute(all.value, CutCornerTreatment),
)

fun Shapes.cutCornerShape(
    topLeft: Dp = 0.dp,
    topRight: Dp = 0.dp,
    bottomRight: Dp = 0.dp,
    bottomLeft: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(topLeft.value, CutCornerTreatment),
    Corner.Absolute(topRight.value, CutCornerTreatment),
    Corner.Absolute(bottomRight.value, CutCornerTreatment),
    Corner.Absolute(bottomLeft.value, CutCornerTreatment),
)
