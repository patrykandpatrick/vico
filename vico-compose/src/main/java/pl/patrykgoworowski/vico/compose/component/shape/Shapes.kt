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

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.extension.pixels
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.corner.Corner
import pl.patrykgoworowski.vico.core.component.shape.corner.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.corner.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.corner.RoundedCornerTreatment
import androidx.compose.ui.graphics.Shape as ComposeShape

private const val RADII_ARRAY_SIZE = 8

@Composable
fun ComposeShape.chartShape(): Shape = object : Shape {

    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current

    private val radii by lazy { FloatArray(RADII_ARRAY_SIZE) }
    private val matrix: Matrix by lazy { Matrix() }

    override fun drawShape(canvas: Canvas, paint: Paint, path: Path, bounds: RectF) {
        val outline = createOutline(
            size = Size(
                width = bounds.width(),
                height = bounds.height()
            ),
            layoutDirection = layoutDirection,
            density = density,
        )
        when (outline) {
            is Outline.Rectangle -> path.addRect(
                bounds.left,
                bounds.top,
                bounds.right,
                bounds.bottom,
                Path.Direction.CCW
            )
            is Outline.Rounded -> path.addRoundRect(bounds, outline.roundRect, radii)
            is Outline.Generic -> {
                matrix.setTranslate(bounds.left, bounds.top)
                path.addPath(outline.path.asAndroidPath(), matrix)
            }
        }
        canvas.drawPath(path, paint)
    }
}

@Suppress("MagicNumber")
fun Path.addRoundRect(bounds: RectF, rect: RoundRect, radii: FloatArray) {
    radii[0] = rect.topLeftCornerRadius.x
    radii[1] = rect.topLeftCornerRadius.y
    radii[2] = rect.topRightCornerRadius.x
    radii[3] = rect.topRightCornerRadius.y
    radii[4] = rect.bottomRightCornerRadius.x
    radii[5] = rect.bottomRightCornerRadius.y
    radii[6] = rect.bottomLeftCornerRadius.x
    radii[7] = rect.bottomLeftCornerRadius.y
    addRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radii, Path.Direction.CCW)
}

@Composable
fun Shapes.roundedCornerShape(
    all: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(all.pixels, RoundedCornerTreatment),
    Corner.Absolute(all.pixels, RoundedCornerTreatment),
    Corner.Absolute(all.pixels, RoundedCornerTreatment),
    Corner.Absolute(all.pixels, RoundedCornerTreatment),
)

@Composable
fun Shapes.roundedCornerShape(
    topLeft: Dp = 0.dp,
    topRight: Dp = 0.dp,
    bottomRight: Dp = 0.dp,
    bottomLeft: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(topLeft.pixels, RoundedCornerTreatment),
    Corner.Absolute(topRight.pixels, RoundedCornerTreatment),
    Corner.Absolute(bottomRight.pixels, RoundedCornerTreatment),
    Corner.Absolute(bottomLeft.pixels, RoundedCornerTreatment),
)

@Composable
fun Shapes.cutCornerShape(
    all: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(all.pixels, CutCornerTreatment),
    Corner.Absolute(all.pixels, CutCornerTreatment),
    Corner.Absolute(all.pixels, CutCornerTreatment),
    Corner.Absolute(all.pixels, CutCornerTreatment),
)

@Composable
fun Shapes.cutCornerShape(
    topLeft: Dp = 0.dp,
    topRight: Dp = 0.dp,
    bottomRight: Dp = 0.dp,
    bottomLeft: Dp = 0.dp,
) = CorneredShape(
    Corner.Absolute(topLeft.pixels, CutCornerTreatment),
    Corner.Absolute(topRight.pixels, CutCornerTreatment),
    Corner.Absolute(bottomRight.pixels, CutCornerTreatment),
    Corner.Absolute(bottomLeft.pixels, CutCornerTreatment),
)
