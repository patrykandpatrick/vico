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

package pl.patrykgoworowski.vico.core.component.shape.cornered

import android.graphics.Paint
import android.graphics.Path
import kotlin.math.absoluteValue
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.context.DrawContext

/**
 * An implementation of generic [Shape] allowing to specify look and size of each corner
 * with [Corner]. It also allows to intercept drawing of each of [Shape]’s sides
 */

public open class CorneredShape(
    public val topLeft: Corner = Corner.Sharp,
    public val topRight: Corner = Corner.Sharp,
    public val bottomRight: Corner = Corner.Sharp,
    public val bottomLeft: Corner = Corner.Sharp,
) : Shape {

    public fun getCornerScale(width: Float, height: Float, density: Float): Float {
        val availableSize = minOf(width, height)
        val tL = topLeft.getCornerSize(availableSize, density)
        val tR = topRight.getCornerSize(availableSize, density)
        val bR = bottomRight.getCornerSize(availableSize, density)
        val bL = bottomLeft.getCornerSize(availableSize, density)
        return minOf(
            width / (tL + tR),
            width / (bL + bR),
            height / (tL + bL),
            height / (tR + bR),
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
        val width = right - left
        val height = bottom - top
        if (width == 0f || height == 0f) return

        val size = minOf(width, height).absoluteValue
        val scale = getCornerScale(width, height, context.density).coerceAtMost(1f)

        val tL = topLeft.getCornerSize(size, context.density) * scale
        val tR = topRight.getCornerSize(size, context.density) * scale
        val bR = bottomRight.getCornerSize(size, context.density) * scale
        val bL = bottomLeft.getCornerSize(size, context.density) * scale

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
