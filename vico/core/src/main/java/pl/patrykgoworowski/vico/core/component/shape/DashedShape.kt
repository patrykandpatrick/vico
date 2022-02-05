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
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.context.DrawContext

public class DashedShape(
    public val shape: Shape,
    public val dashLengthDp: Float,
    public val gapLengthDp: Float,
    public val fitStrategy: FitStrategy = FitStrategy.Resize,
) : Shape {

    private var drawDashLength = dashLengthDp
    private var drawGapLength = gapLengthDp

    override fun drawShape(
        context: DrawContext,
        paint: Paint,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        if (right - left > bottom - top) {
            drawHorizontalDashes(context, paint, path, left, top, right, bottom)
        } else {
            drawVerticalDashes(context, paint, path, left, top, right, bottom)
        }
    }

    @LongParameterListDrawFunction
    private fun drawHorizontalDashes(
        context: DrawContext,
        paint: Paint,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        calculateDrawLengths(context, right - left)

        var index = 0
        var drawnLength = 0f
        while (right - left - drawnLength > 0) {
            drawnLength += if (index % 2 == 0) {
                path.reset()
                shape.drawShape(
                    context = context,
                    paint = paint,
                    path = path,
                    left = left + drawnLength,
                    top = top,
                    right = left + drawnLength + drawDashLength,
                    bottom = bottom,
                )
                drawDashLength
            } else {
                drawGapLength
            }
            index++
        }
    }

    @LongParameterListDrawFunction
    private fun drawVerticalDashes(
        context: DrawContext,
        paint: Paint,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        calculateDrawLengths(context, bottom - top)

        var index = 0
        var drawnLength = 0f
        while (bottom - top - drawnLength > 0) {
            drawnLength += if (index % 2 == 0) {
                path.reset()
                shape.drawShape(
                    context = context,
                    paint = paint,
                    path = path,
                    left = left,
                    top = top + drawnLength,
                    right = right,
                    bottom = top + drawnLength + drawDashLength,
                )
                drawDashLength
            } else {
                drawGapLength
            }
            index++
        }
    }

    private fun calculateDrawLengths(context: DrawContext, length: Float): Unit = with(context) {
        calculateDrawLengths(dashLengthDp.pixels, gapLengthDp.pixels, length)
    }

    private fun calculateDrawLengths(
        dashLength: Float,
        gapLength: Float,
        length: Float,
    ) = when (fitStrategy) {
        FitStrategy.Resize -> when {
            length < dashLength + gapLength -> {
                drawDashLength = length
                drawGapLength = 0f
            }
            else -> {
                var fitWidth = dashLength
                while (length > fitWidth) {
                    fitWidth += gapLength + dashLength
                }
                val ratio = length / fitWidth
                drawDashLength = dashLength * ratio
                drawGapLength = gapLength * ratio
            }
        }
        FitStrategy.Fixed -> {
            drawDashLength = dashLength
            drawGapLength = gapLength
        }
    }

    public enum class FitStrategy {
        Resize,
        Fixed
    }
}
