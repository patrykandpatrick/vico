package pl.patrykgoworowski.liftchart_common.path.corner

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.DEF_MARKER_TICK_SIZE
import pl.patrykgoworowski.liftchart_common.extension.between

open class MarkerCorneredShape(
    topLeft: Corner,
    topRight: Corner,
    bottomRight: Corner,
    bottomLeft: Corner,
    val tickSize: Float = DEF_MARKER_TICK_SIZE,
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

    fun drawMarker(
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