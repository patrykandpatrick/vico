package pl.patrykgoworowski.liftchart_common.data_set.bar.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry

abstract class CornerBarPathCreator(
    private val topLeft: Float = 0f,
    private val topRight: Float = 0f,
    private val bottomRight: Float = 0f,
    private val bottomLeft: Float = 0f
) : BarPathCreator {

    private var tL = 0f
    private var tR = 0f
    private var bR = 0f
    private var bL = 0f

    private val minHeight by lazy {
        getMinimumHeight(topLeft, topRight, bottomRight, bottomLeft)
    }

    override fun drawBarPath(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        drawBounds: RectF,
        barBounds: RectF,
        animationOffset: Float,
        entry: AnyEntry
    ) {
        when {
            barBounds.height() == 0f -> return
            barBounds.height() < minHeight -> {
                val scale = barBounds.height() / minHeight
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
            barPath,
            drawBounds,
            barBounds,
            animationOffset,
            entry,
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
        drawBounds: RectF,
        barBounds: RectF,
        animationOffset: Float,
        entry: AnyEntry,
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    )

}