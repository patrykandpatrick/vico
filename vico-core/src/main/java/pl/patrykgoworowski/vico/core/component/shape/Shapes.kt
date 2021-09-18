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

package pl.patrykgoworowski.vico.core.component.shape

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import pl.patrykgoworowski.vico.core.extension.setBounds
import pl.patrykgoworowski.vico.core.extension.updateBounds
import pl.patrykgoworowski.vico.core.component.shape.corner.Corner
import pl.patrykgoworowski.vico.core.component.shape.corner.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.corner.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.corner.RoundedCornerTreatment

object Shapes {
    val pillShape = roundedCornersShape(allPercent = 50)

    val rectShape: Shape = object : Shape {
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

    fun roundedCornersShape(allPercent: Int) =
        roundedCornersShape(allPercent, allPercent, allPercent, allPercent)

    fun roundedCornersShape(
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

    fun cutCornerShape(allPercent: Int) =
        cutCornerShape(allPercent, allPercent, allPercent, allPercent)

    fun cutCornerShape(
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

    fun drawableShape(
        drawable: Drawable,
        keepAspectRatio: Boolean = false,
        otherCreator: Shape? = rectShape
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
}
