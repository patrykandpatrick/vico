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

package pl.patrykgoworowski.liftchart_common.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import pl.patrykgoworowski.liftchart_common.extension.setBounds
import pl.patrykgoworowski.liftchart_common.extension.updateBounds
import pl.patrykgoworowski.liftchart_common.path.corner.Corner
import pl.patrykgoworowski.liftchart_common.path.corner.CorneredShape
import pl.patrykgoworowski.liftchart_common.path.corner.CutCornerTreatment
import pl.patrykgoworowski.liftchart_common.path.corner.RoundedCornerTreatment

val RectShape: Shape = object : Shape {

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    ) {
        path.moveTo(bounds.left, bounds.top)
        path.lineTo(bounds.right, bounds.top)
        path.lineTo(bounds.right, bounds.bottom)
        path.lineTo(bounds.left, bounds.bottom)
        path.close()
        canvas.drawPath(path, paint)
    }
}

fun RoundedCornersShape(all: Float): Shape = RoundedCornersShape(all, all, all, all)

fun RoundedCornersShape(
    topLeftPercent: Int = 0,
    topRightPercent: Int = 0,
    bottomRightPercent: Int = 0,
    bottomLeftPercent: Int = 0,
): CorneredShape = CorneredShape(
    Corner.Relative(topLeftPercent, RoundedCornerTreatment),
    Corner.Relative(topRightPercent, RoundedCornerTreatment),
    Corner.Relative(bottomRightPercent, RoundedCornerTreatment),
    Corner.Relative(bottomLeftPercent, RoundedCornerTreatment),
)

fun RoundedCornersShape(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f,
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft, RoundedCornerTreatment),
    Corner.Absolute(topRight, RoundedCornerTreatment),
    Corner.Absolute(bottomRight, RoundedCornerTreatment),
    Corner.Absolute(bottomLeft, RoundedCornerTreatment),
)

val PillShape = RoundedCornersShape(50, 50, 50, 50)

fun CutCornerShape(all: Float): Shape = CutCornerShape(all, all, all, all)

fun CutCornerShape(
    topLeftPercent: Int = 0,
    topRightPercent: Int = 0,
    bottomRightPercent: Int = 0,
    bottomLeftPercent: Int = 0,
): CorneredShape = CorneredShape(
    Corner.Relative(topLeftPercent, CutCornerTreatment),
    Corner.Relative(topRightPercent, CutCornerTreatment),
    Corner.Relative(bottomRightPercent, CutCornerTreatment),
    Corner.Relative(bottomLeftPercent, CutCornerTreatment),
)

fun CutCornerShape(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft, CutCornerTreatment),
    Corner.Absolute(topRight, CutCornerTreatment),
    Corner.Absolute(bottomRight, CutCornerTreatment),
    Corner.Absolute(bottomLeft, CutCornerTreatment),
)

fun DrawableShape(
    drawable: Drawable,
    keepAspectRatio: Boolean = false,
    otherCreator: Shape? = RectShape
): Shape = object : Shape {

    private val ratio: Float = drawable.intrinsicWidth.coerceAtLeast(1) /
            drawable.intrinsicHeight.coerceAtLeast(1).toFloat()

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    ) {
        if (bounds.height() == 0f) return
        val drawableHeight = if (keepAspectRatio) bounds.width() * ratio else bounds.height()
        val top = minOf(bounds.top, bounds.bottom - drawableHeight)
        drawable.setBounds(bounds.left, top, bounds.right, top + drawableHeight)
        drawable.draw(canvas)
        otherCreator ?: return

        bounds.updateBounds(top = drawable.bounds.bottom.toFloat())
        if (bounds.height() > 0) {
            otherCreator.drawShape(
                canvas,
                paint,
                path,
                bounds
            )
        }
    }
}
