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

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.DEF_MARKER_TICK_SIZE
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
import pl.patrykgoworowski.vico.core.draw.DrawContext
import pl.patrykgoworowski.vico.core.extension.between

public open class MarkerCorneredShape(
    topLeft: Corner,
    topRight: Corner,
    bottomRight: Corner,
    bottomLeft: Corner,
    public val tickSize: Float = DEF_MARKER_TICK_SIZE,
) : CorneredShape(
    topLeft, topRight, bottomRight, bottomLeft
) {

    public constructor(
        all: Corner,
        tickSize: Float = DEF_MARKER_TICK_SIZE,
    ) : this(all, all, all, all, tickSize)

    public constructor(
        corneredShape: CorneredShape,
        tickSize: Float = DEF_MARKER_TICK_SIZE,
    ) : this(
        topLeft = corneredShape.topLeft,
        topRight = corneredShape.topRight,
        bottomRight = corneredShape.bottomRight,
        bottomLeft = corneredShape.bottomLeft,
        tickSize = tickSize,
    )

    @LongParameterListDrawFunction
    public fun drawMarker(
        context: DrawContext,
        paint: Paint,
        path: Path,
        bounds: RectF,
        contentBounds: RectF,
        tickX: Float,
    ) {
        createPath(context = context, path = path, left = bounds.left, top = bounds.top,
        right = bounds.right, bottom = bounds.bottom)
        val tickSize = context.toPixels(tickSize)
        val availableCornerSize = minOf(bounds.width(), bounds.height())

        val minLeft = contentBounds.left +
                bottomLeft.getCornerSize(availableCornerSize, context.density)
        val maxLeft = contentBounds.right -
                (bottomRight.getCornerSize(availableCornerSize, context.density) + tickSize * 2)

        val tickTopLeft = (tickX - tickSize).between(minLeft, maxLeft)
        path.moveTo(tickTopLeft, bounds.bottom)
        path.lineTo(tickX, bounds.bottom + tickSize)
        path.lineTo(tickTopLeft + (tickSize * 2), bounds.bottom)
        path.close()
        drawShape(
            context = context,
            paint = paint,
            path = path,
            left = bounds.left,
            top = bounds.top,
            right = bounds.right,
            bottom = bounds.bottom
        )
    }
}
