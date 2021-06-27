package pl.patrykgoworowski.liftchart_common.path.corner

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.path.Shape
import kotlin.math.absoluteValue

public open class CorneredShape(
    private val topLeft: Corner,
    private val topRight: Corner,
    private val bottomRight: Corner,
    private val bottomLeft: Corner,
) : Shape {

    private var tL = 0f
    private var tR = 0f
    private var bR = 0f
    private var bL = 0f

    private val minHeight by lazy {
        getMinimumHeight(
            topLeft.absoluteSize,
            topRight.absoluteSize,
            bottomRight.absoluteSize,
            bottomLeft.absoluteSize
        )
    }

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    ) {
        val height = bounds.height().absoluteValue
        when {
            height == 0f -> return
            height < minHeight -> {
                val scale = height / minHeight
                tL = topLeft.absoluteSize * scale
                tR = topRight.absoluteSize * scale
                bR = bottomRight.absoluteSize * scale
                bL = bottomLeft.absoluteSize * scale
            }
            else -> {
                val halfOfSmallerSide = minOf(bounds.width(), bounds.height()).half
                tL = topLeft.getCornerSize(halfOfSmallerSide)
                tR = topRight.getCornerSize(halfOfSmallerSide)
                bR = bottomRight.getCornerSize(halfOfSmallerSide)
                bL = bottomLeft.getCornerSize(halfOfSmallerSide)
            }
        }

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
        onShapePathCreated(path, bounds)

        canvas.drawPath(path, paint)
    }

    protected open fun onShapePathCreated(
            path: Path,
            bounds: RectF,
    ) {}

}