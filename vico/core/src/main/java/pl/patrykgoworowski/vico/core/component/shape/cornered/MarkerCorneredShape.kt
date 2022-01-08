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
import pl.patrykgoworowski.vico.core.DEF_MARKER_TICK_SIZE
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.extension.between

public open class MarkerCorneredShape(
    topLeft: Corner,
    topRight: Corner,
    bottomRight: Corner,
    bottomLeft: Corner,
    public val tickSizeDp: Float = DEF_MARKER_TICK_SIZE,
) : CorneredShape(
    topLeft, topRight, bottomRight, bottomLeft
) {

    public constructor(
        all: Corner,
        tickSizeDp: Float = DEF_MARKER_TICK_SIZE,
    ) : this(all, all, all, all, tickSizeDp)

    public constructor(
        corneredShape: CorneredShape,
        tickSizeDp: Float = DEF_MARKER_TICK_SIZE,
    ) : this(
        topLeft = corneredShape.topLeft,
        topRight = corneredShape.topRight,
        bottomRight = corneredShape.bottomRight,
        bottomLeft = corneredShape.bottomLeft,
        tickSizeDp = tickSizeDp,
    )

    override fun drawShape(
        context: DrawContext,
        paint: Paint,
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): Unit = with(context) {
        val tickX: Float? = context[tickXKey]
        if (tickX != null) {
            createPath(
                context = context,
                path = path,
                left = left,
                top = top,
                right = right,
                bottom = bottom
            )
            val tickSize = context.toPixels(tickSizeDp)
            val availableCornerSize = minOf(right - left, bottom - top)
            val cornerScale = getCornerScale(right - left, bottom - top, density)

            val minLeft = left + bottomLeft.getCornerSize(availableCornerSize, density) * cornerScale
            val maxLeft = right - (bottomRight.getCornerSize(availableCornerSize, density) * cornerScale + tickSize * 2)

            val tickTopLeft = (tickX - tickSize).between(minLeft, maxLeft)
            path.moveTo(tickTopLeft, bottom)
            path.lineTo(tickX, bottom + tickSize)
            path.lineTo(tickTopLeft + (tickSize * 2), bottom)
            path.close()
            context.canvas.drawPath(path, paint)
        } else {
            super.drawShape(context, paint, path, left, top, right, bottom)
        }
    }

    public companion object {
        public const val tickXKey: String = "tickX"
    }
}
