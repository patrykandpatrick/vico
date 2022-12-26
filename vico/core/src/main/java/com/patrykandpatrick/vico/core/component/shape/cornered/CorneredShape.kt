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

package com.patrykandpatrick.vico.core.component.shape.cornered

import android.graphics.Paint
import android.graphics.Path
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.component.shape.Shape
import com.patrykandpatrick.vico.core.context.DrawContext
import kotlin.math.absoluteValue

/**
 * A [Shape] implementation with customizable corners.
 *
 * @param topLeft specifies a [Corner] for the top left of the [Shape].
 * @param topRight specifies a [Corner] for the top right of the [Shape].
 * @param bottomLeft specifies a [Corner] for the bottom left of the [Shape].
 * @param bottomRight specifies a [Corner] for the bottom right of the [Shape].
 */
public open class CorneredShape(
    public val topLeft: Corner = Corner.Sharp,
    public val topRight: Corner = Corner.Sharp,
    public val bottomRight: Corner = Corner.Sharp,
    public val bottomLeft: Corner = Corner.Sharp,
) : Shape {

    private val Float.nonZero: Float
        get() = if (this == 0f) 1f else this

    /**
     * Returns a scale factor for the corner size, which will prevent graphical glitches in case the size of the
     * corners is larger than the shape’s dimensions.
     *
     * @param width the width of the [Shape].
     * @param height the height of the [Shape].
     * @param density the pixel density of the screen (used in pixel size calculation).
     */
    public fun getCornerScale(width: Float, height: Float, density: Float): Float {
        val availableSize = minOf(width, height)
        val tL = topLeft.getCornerSize(availableSize, density)
        val tR = topRight.getCornerSize(availableSize, density)
        val bR = bottomRight.getCornerSize(availableSize, density)
        val bL = bottomLeft.getCornerSize(availableSize, density)
        return minOf(
            width / (tL + tR).nonZero,
            width / (bL + bR).nonZero,
            height / (tL + bL).nonZero,
            height / (tR + bR).nonZero,
        )
    }

    override fun drawShape(
        context: DrawContext,
        paint: Paint,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        createPath(context, path, left, top, right, bottom)
        context.canvas.drawPath(path, paint)
    }

    @LongParameterListDrawFunction
    protected open fun createPath(
        context: DrawContext,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        createPath(
            density = context.density,
            path = path,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
        )
    }

    /**
     * Adds a contour of this [CorneredShape] to the provided [Path].
     *
     * @param density the screen density.
     * @param path the [Path] to use.
     * @param left the left edge of the [CorneredShape].
     * @param top the top edge of the [CorneredShape].
     * @param right the right edge of the [CorneredShape].
     * @param bottom the bottom edge of the [CorneredShape].
     */
    @LongParameterListDrawFunction
    public open fun createPath(
        density: Float,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        val width = right - left
        val height = bottom - top
        if (width == 0f || height == 0f) return

        val size = minOf(width, height).absoluteValue
        val scale = getCornerScale(width, height, density).coerceAtMost(1f)

        val tL = topLeft.getCornerSize(size, density) * scale
        val tR = topRight.getCornerSize(size, density) * scale
        val bR = bottomRight.getCornerSize(size, density) * scale
        val bL = bottomLeft.getCornerSize(size, density) * scale

        path.moveTo(left, top + tL)
        topLeft.cornerTreatment.createCorner(
            x1 = left,
            y1 = top + tL,
            x2 = left + tL,
            y2 = top,
            cornerLocation = CornerLocation.TopLeft,
            path,
        )

        path.lineTo(right - tR, top)
        topRight.cornerTreatment.createCorner(
            x1 = right - tR,
            y1 = top,
            x2 = right,
            y2 = top + tR,
            cornerLocation = CornerLocation.TopRight,
            path,
        )

        path.lineTo(right, bottom - bR)
        bottomRight.cornerTreatment.createCorner(
            x1 = right,
            y1 = bottom - bR,
            x2 = right - bR,
            y2 = bottom,
            cornerLocation = CornerLocation.BottomRight,
            path,
        )

        path.lineTo(left + bL, bottom)
        bottomLeft.cornerTreatment.createCorner(
            x1 = left + bL,
            y1 = bottom,
            x2 = left,
            y2 = bottom - bL,
            cornerLocation = CornerLocation.BottomLeft,
            path,
        )
        path.close()
    }
}
