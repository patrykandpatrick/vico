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

package pl.patrykgoworowski.vico.core.component.shape.corner

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.component.shape.Shape
import kotlin.math.absoluteValue

public open class CorneredShape(
    public val topLeft: Corner,
    public val topRight: Corner,
    public val bottomRight: Corner,
    public val bottomLeft: Corner,
) : Shape {

    private fun getCornerScale(width: Float, height: Float): Float {
        val availableSize = minOf(width, height)
        val tL = topLeft.getCornerSize(availableSize)
        val tR = topRight.getCornerSize(availableSize)
        val bR = bottomRight.getCornerSize(availableSize)
        val bL = bottomLeft.getCornerSize(availableSize)
        return minOf(
            width / (tL + tR),
            width / (bL + bR),
            height / (tL + bL),
            height / (tR + bR),
        )
    }

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    ) {
        createPath(path, bounds)
        canvas.drawPath(path, paint)
    }

    protected open fun createPath(
        path: Path,
        bounds: RectF,
    ) {
        val width = bounds.width()
        val height = bounds.height()
        if (width == 0f || height == 0f) return

        val size = minOf(width, height).absoluteValue
        val scale = getCornerScale(width, height).coerceAtMost(1f)

        val tL = topLeft.getCornerSize(size) * scale
        val tR = topRight.getCornerSize(size) * scale
        val bR = bottomRight.getCornerSize(size) * scale
        val bL = bottomLeft.getCornerSize(size) * scale

        path.moveTo(bounds.left, bounds.top + tL)
        topLeft.cornerTreatment.createCorner(
            x1 = bounds.left,
            y1 = bounds.top + tL,
            x2 = bounds.left + tL,
            y2 = bounds.top,
            cornerLocation = CornerLocation.TopLeft,
            path
        )

        path.lineTo(bounds.right - tR, bounds.top)
        topRight.cornerTreatment.createCorner(
            x1 = bounds.right - tR,
            y1 = bounds.top,
            x2 = bounds.right,
            y2 = bounds.top + tR,
            cornerLocation = CornerLocation.TopRight,
            path
        )

        path.lineTo(bounds.right, bounds.bottom - bR)
        bottomRight.cornerTreatment.createCorner(
            x1 = bounds.right,
            y1 = bounds.bottom - bR,
            x2 = bounds.right - bR,
            y2 = bounds.bottom,
            cornerLocation = CornerLocation.BottomRight,
            path
        )

        path.lineTo(bounds.left + bL, bounds.bottom)
        bottomLeft.cornerTreatment.createCorner(
            x1 = bounds.left + bL,
            y1 = bounds.bottom,
            x2 = bounds.left,
            y2 = bounds.bottom - bL,
            cornerLocation = CornerLocation.BottomLeft,
            path
        )
        path.close()
    }
}
