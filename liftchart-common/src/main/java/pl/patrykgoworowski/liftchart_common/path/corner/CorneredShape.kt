package pl.patrykgoworowski.liftchart_common.path.corner

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.path.Shape
import kotlin.math.absoluteValue

public open class CorneredShape(
    public val topLeft: Corner,
    public val topRight: Corner,
    public val bottomRight: Corner,
    public val bottomLeft: Corner,
) : Shape {

    private var tL = 0f
    private var tR = 0f
    private var bR = 0f
    private var bL = 0f

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

        tL = topLeft.getCornerSize(size) * scale
        tR = topRight.getCornerSize(size) * scale
        bR = bottomRight.getCornerSize(size) * scale
        bL = bottomLeft.getCornerSize(size) * scale

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