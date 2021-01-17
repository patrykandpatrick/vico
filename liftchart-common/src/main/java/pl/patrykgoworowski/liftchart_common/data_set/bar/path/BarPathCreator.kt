package pl.patrykgoworowski.liftchart_common.data_set.bar.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry

interface BarPathCreator {
    fun drawBarPath(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        drawBounds: RectF,
        barBounds: RectF,
        animationOffset: Float,
        entry: AnyEntry
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