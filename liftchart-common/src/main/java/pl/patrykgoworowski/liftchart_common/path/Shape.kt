package pl.patrykgoworowski.liftchart_common.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.AnyEntry

interface Shape {
    fun drawEntryShape(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        drawBounds: RectF,
        barBounds: RectF,
        entry: AnyEntry
    )

    fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    )

    fun getMinHeight(barBounds: RectF): Float = 0f

    fun getMinimumHeight(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    ): Float = maxOf(topLeft + bottomLeft, topRight + bottomRight)

    fun overrideBoundsWithMinSize(
        bounds: RectF,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    ) {
        bounds.top = minOf(
            bounds.top,
            bounds.bottom - getMinimumHeight(topLeft, topRight, bottomRight, bottomLeft)
        )
    }
}