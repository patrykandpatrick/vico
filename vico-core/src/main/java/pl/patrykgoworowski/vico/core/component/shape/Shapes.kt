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

import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.component.shape.corner.Corner
import pl.patrykgoworowski.vico.core.component.shape.corner.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.corner.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.corner.RoundedCornerTreatment
import pl.patrykgoworowski.vico.core.draw.DrawContext
import pl.patrykgoworowski.vico.core.extension.setBounds

object Shapes {
    val pillShape = roundedCornersShape(allPercent = 50)

    val rectShape: Shape = object : Shape {
        override fun drawShape(
            context: DrawContext,
            paint: Paint,
            path: Path,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ) {
            path.moveTo(left, top)
            path.lineTo(right, top)
            path.lineTo(right, bottom)
            path.lineTo(left, bottom)
            path.close()
            context.canvas.drawPath(path, paint)
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
            context: DrawContext,
            paint: Paint,
            path: Path,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ) {
            if (bottom - top == 0f) return
            val drawableHeight = if (keepAspectRatio) (right - left) * ratio else bottom - top
            val top = minOf(top, bottom - drawableHeight)
            drawable.setBounds(left, top, right, top + drawableHeight)
            drawable.draw(context.canvas)
            otherCreator ?: return

            val drawableBottom = drawable.bounds.bottom.toFloat()
            if (bottom - drawableBottom > 0) {
                otherCreator.drawShape(
                    context = context,
                    paint = paint,
                    path = path,
                    left = left,
                    top = drawableBottom,
                    right = right,
                    bottom = bottom,
                )
            }
        }
    }

    fun dashedShape(
        shape: Shape = rectShape,
        dashLength: Float = Dimens.DASH_LENGTH,
        gapLength: Float = Dimens.DASH_GAP,
    ): Shape = DashedShape(
        shape = shape,
        dashLengthDp = dashLength,
        gapLengthDp = gapLength,
    )
}
