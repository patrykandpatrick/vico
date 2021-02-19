package pl.patrykgoworowski.liftchart_common.data_set.bar.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.AnyEntry

abstract class CornerShape(
    private val topLeft: Float = 0f,
    private val topRight: Float = 0f,
    private val bottomRight: Float = 0f,
    private val bottomLeft: Float = 0f
) : Shape {

    private var tL = 0f
    private var tR = 0f
    private var bR = 0f
    private var bL = 0f

    private val minHeight by lazy {
        getMinimumHeight(topLeft, topRight, bottomRight, bottomLeft)
    }

    override fun drawEntryShape(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        drawBounds: RectF,
        barBounds: RectF,
        entry: AnyEntry
    ) {
       drawShape(
           canvas,
           paint,
           barPath,
           barBounds
       )
    }

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    ) {
        when {
            bounds.height() == 0f -> return
            bounds.height() < minHeight -> {
                val scale = bounds.height() / minHeight
                tL = topLeft * scale
                tR = topRight * scale
                bR = bottomRight * scale
                bL = bottomLeft * scale
            }
            else -> {
                tL = topLeft
                tR = topRight
                bR = bottomRight
                bL = bottomLeft
            }
        }
        drawBarPathWithCorners(
            canvas,
            paint,
            path,
            bounds,
            tL,
            tR,
            bR,
            bL
        )
    }

    abstract fun drawBarPathWithCorners(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        barBounds: RectF,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    )

}