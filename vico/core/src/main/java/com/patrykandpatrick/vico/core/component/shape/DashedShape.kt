/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.component.shape

import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.extension.ceil

/**
 * [DashedShape] draws a dashed line by interchangeably drawing the provided [shape] and leaving a gap.
 *
 * @property shape the base [Shape] from which to create the [DashedShape].
 * @property dashLengthDp the dash length in dp.
 * @property gapLengthDp the gap length in dp.
 * @property fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public class DashedShape(
    public val shape: Shape = Shapes.rectShape,
    public val dashLengthDp: Float = DefaultDimens.DASH_LENGTH,
    public val gapLengthDp: Float = DefaultDimens.DASH_GAP,
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
    ) {
        if (dashLength == 0f && gapLength == 0f) {
            drawDashLength = length
            return
        }
        when (fitStrategy) {
            FitStrategy.Resize -> when {
                length < dashLength + gapLength -> {
                    drawDashLength = length
                    drawGapLength = 0f
                }
                else -> {
                    val gapAndDashLength = gapLength + dashLength
                    val ratio = length / (dashLength + (length / gapAndDashLength).ceil * gapAndDashLength)
                    drawDashLength = dashLength * ratio
                    drawGapLength = gapLength * ratio
                }
            }
            FitStrategy.Fixed -> {
                drawDashLength = dashLength
                drawGapLength = gapLength
            }
        }
    }

    /**
     * Defines how a [DashedShape] is to be rendered.
     */
    public enum class FitStrategy {
        /**
         * The [DashedShape] will slightly increase or decrease the [DashedShape.dashLengthDp] and
         * [DashedShape.gapLengthDp] values so that the dashes fit perfectly without being cut off.
         */
        Resize,

        /**
         * The [DashedShape] will use the exact [DashedShape.dashLengthDp] and [DashedShape.gapLengthDp] values
         * provided. As a result, the [DashedShape] may not fit within its bounds or be cut off.
         */
        Fixed,
    }
}
