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

package pl.patrykgoworowski.vico.core.path.corner

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.DEF_MARKER_TICK_SIZE
import pl.patrykgoworowski.vico.core.annotation.LongParameterListDrawFunction
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

    constructor(
        all: Corner,
        tickSize: Float = DEF_MARKER_TICK_SIZE,
    ) : this(all, all, all, all, tickSize)

    constructor(
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
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF,
        contentBounds: RectF,
        tickX: Float,
    ) {
        createPath(path = path, bounds = bounds)
        val availableCornerSize = minOf(bounds.width(), bounds.height())

        val minLeft = contentBounds.left + bottomLeft.getCornerSize(availableCornerSize)
        val maxLeft =
            contentBounds.right - (bottomRight.getCornerSize(availableCornerSize) + (tickSize * 2))

        val tickTopLeft = (tickX - tickSize).between(minLeft, maxLeft)
        path.moveTo(tickTopLeft, bounds.bottom)
        path.lineTo(tickX, bounds.bottom + tickSize)
        path.lineTo(tickTopLeft + (tickSize * 2), bounds.bottom)
        path.close()
        drawShape(canvas, paint, path, bounds)
    }
}
