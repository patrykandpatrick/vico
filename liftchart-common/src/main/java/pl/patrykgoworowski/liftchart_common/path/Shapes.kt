package pl.patrykgoworowski.liftchart_common.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import pl.patrykgoworowski.liftchart_common.extension.setBounds
import pl.patrykgoworowski.liftchart_common.extension.updateBounds
import pl.patrykgoworowski.liftchart_common.path.corner.Corner
import pl.patrykgoworowski.liftchart_common.path.corner.CorneredShape
import pl.patrykgoworowski.liftchart_common.path.corner.CutCornerTreatment
import pl.patrykgoworowski.liftchart_common.path.corner.RoundedCornerTreatment

fun rectShape(): Shape = object : Shape {

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    ) {
        path.moveTo(bounds.left, bounds.top)
        path.lineTo(bounds.right, bounds.top)
        path.lineTo(bounds.right, bounds.bottom)
        path.lineTo(bounds.left, bounds.bottom)
        path.close()
        canvas.drawPath(path, paint)
    }

}

fun roundedCornersShape(all: Float): Shape = roundedCornersShape(all, all, all, all)

fun roundedCornersShape(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f,
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft, RoundedCornerTreatment),
    Corner.Absolute(topRight, RoundedCornerTreatment),
    Corner.Absolute(bottomRight, RoundedCornerTreatment),
    Corner.Absolute(bottomLeft, RoundedCornerTreatment),
)

fun pillShape() = roundedCornersShape(50, 50, 50, 50)

fun roundedCornersShape(
    topLeftPercent: Int = 0,
    topRightPercent: Int = 0,
    bottomRightPercent: Int = 0,
    bottomLeftPercent: Int = 0,
): CorneredShape = CorneredShape(
    Corner.Relative(topLeftPercent, RoundedCornerTreatment),
    Corner.Relative(topRightPercent, RoundedCornerTreatment),
    Corner.Relative(bottomRightPercent, RoundedCornerTreatment),
    Corner.Relative(bottomLeftPercent, RoundedCornerTreatment),
)

fun cutCornerShape(all: Float): Shape = cutCornerShape(all, all, all, all)

fun cutCornerShape(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f
): CorneredShape = CorneredShape(
    Corner.Absolute(topLeft, CutCornerTreatment),
    Corner.Absolute(topRight, CutCornerTreatment),
    Corner.Absolute(bottomRight, CutCornerTreatment),
    Corner.Absolute(bottomLeft, CutCornerTreatment),
)

fun drawableShape(
    drawable: Drawable,
    keepAspectRatio: Boolean = false,
    otherCreator: Shape? = rectShape()
): Shape = object : Shape {

    private val ratio: Float = drawable.intrinsicWidth.coerceAtLeast(1) /
            drawable.intrinsicHeight.coerceAtLeast(1).toFloat()

    override fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    ) {
        if (bounds.height() == 0f) return
        val drawableHeight = if (keepAspectRatio) bounds.width() * ratio else bounds.height()
        val top = minOf(bounds.top, bounds.bottom - drawableHeight)
        drawable.setBounds(bounds.left, top, bounds.right, top + drawableHeight)
        drawable.draw(canvas)
        otherCreator ?: return

        bounds.updateBounds(top = drawable.bounds.bottom.toFloat())
        if (bounds.height() > 0) {
            otherCreator.drawShape(
                canvas,
                paint,
                path,
                bounds
            )
        }
    }

}