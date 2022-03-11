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
import pl.patrykgoworowski.vico.core.DefaultDimens
import pl.patrykgoworowski.vico.core.component.shape.cornered.Corner
import pl.patrykgoworowski.vico.core.component.shape.cornered.CorneredShape
import pl.patrykgoworowski.vico.core.component.shape.cornered.CutCornerTreatment
import pl.patrykgoworowski.vico.core.component.shape.cornered.RoundedCornerTreatment
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.extension.setBounds

public object Shapes {

    public val pillShape: CorneredShape = roundedCornerShape(allPercent = 50)

    public val rectShape: Shape = object : Shape {

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

    public fun roundedCornerShape(allPercent: Int): CorneredShape =
        roundedCornerShape(allPercent, allPercent, allPercent, allPercent)

    public fun roundedCornerShape(
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

    public fun cutCornerShape(allPercent: Int): CorneredShape =
        cutCornerShape(allPercent, allPercent, allPercent, allPercent)

    public fun cutCornerShape(
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

    public fun drawableShape(
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
            val drawableHeight = if (keepAspectRatio) (right - left) / ratio else bottom - top
            val topWithoutClipping = minOf(top, bottom - drawableHeight)
            drawable.setBounds(left, topWithoutClipping, right, topWithoutClipping + drawableHeight)
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

    public fun dashedShape(
        shape: Shape = rectShape,
        dashLength: Float = DefaultDimens.DASH_LENGTH,
        gapLength: Float = DefaultDimens.DASH_GAP,
    ): Shape = DashedShape(
        shape = shape,
        dashLengthDp = dashLength,
        gapLengthDp = gapLength,
    )
}
