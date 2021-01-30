package pl.patrykgoworowski.liftchart_common.data_set.bar.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.extension.setBounds
import pl.patrykgoworowski.liftchart_common.extension.updateBounds


fun DefaultBarPath(): BarPathCreator = object : BarPathCreator {

    override fun drawBarPath(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        drawBounds: RectF,
        barBounds: RectF,
        animationOffset: Float,
        entry: AnyEntry
    ) {
        barPath.moveTo(barBounds.left, barBounds.top)
        barPath.lineTo(barBounds.right, barBounds.top)
        barPath.lineTo(barBounds.right, barBounds.bottom)
        barPath.lineTo(barBounds.left, barBounds.bottom)
        barPath.close()
        canvas.drawPath(barPath, paint)
    }

    override fun getMinHeight(barBounds: RectF): Float = 0f
}

fun RoundedCornerBarPath(all: Float): BarPathCreator = RoundedCornerBarPath(all, all, all, all)

fun RoundedCornerBarPath(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f,
): BarPathCreator = object : CornerBarPathCreator(topLeft, topRight, bottomRight, bottomLeft) {

    private val radii = FloatArray(8)

    override fun drawBarPathWithCorners(
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
    ) {
        if (barBounds.height() == 0f) return
        radii[0] = topLeft
        radii[1] = topLeft
        radii[2] = topRight
        radii[3] = topRight
        radii[4] = bottomRight
        radii[5] = bottomRight
        radii[6] = bottomLeft
        radii[7] = bottomLeft
        overrideBoundsWithMinSize(barBounds, topLeft, topRight, bottomRight, bottomLeft)
        barPath.addRoundRect(barBounds, radii, Path.Direction.CCW)
        canvas.drawPath(barPath, paint)
    }

    override fun getMinHeight(barBounds: RectF): Float =
        getMinimumHeight(topLeft, topRight, bottomRight, bottomLeft)

}

fun CutCornerBarPath(all: Float): BarPathCreator = CutCornerBarPath(all, all, all, all)

fun CutCornerBarPath(
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomRight: Float = 0f,
    bottomLeft: Float = 0f
): BarPathCreator = object : CornerBarPathCreator(topLeft, topRight, bottomRight, bottomLeft) {

    private val minHeight = getMinimumHeight(topLeft, topRight, bottomRight, bottomLeft)

    override fun drawBarPathWithCorners(
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
    ) {
        barPath.moveTo(barBounds.left, barBounds.top + topLeft)
        barPath.lineTo(barBounds.left + topLeft, barBounds.top)
        barPath.lineTo(barBounds.right - topRight, barBounds.top)
        barPath.lineTo(barBounds.right, barBounds.top + topRight)
        barPath.lineTo(barBounds.right, barBounds.bottom - bottomRight)
        barPath.lineTo(barBounds.right - bottomRight, barBounds.bottom)
        barPath.lineTo(barBounds.left + bottomLeft, barBounds.bottom)
        barPath.lineTo(barBounds.left, barBounds.bottom - bottomLeft)
        barPath.close()
        canvas.drawPath(barPath, paint)
    }

    override fun getMinHeight(barBounds: RectF): Float = minHeight


}

fun SkewedBarPath(strength: Float = 1f): BarPathCreator = object : BarPathCreator {

    override fun drawBarPath(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        drawBounds: RectF,
        barBounds: RectF,
        animationOffset: Float,
        entry: AnyEntry
    ) {
        val skewedX =
            barBounds.left + (barBounds.width() * strength * (barBounds.height() / drawBounds.height()))
        barPath.moveTo(skewedX, barBounds.top)
        barPath.lineTo(skewedX + barBounds.width(), barBounds.top)

        barPath.lineTo(barBounds.right, barBounds.bottom)
        barPath.lineTo(barBounds.left, barBounds.bottom)
        barPath.close()
        canvas.drawPath(barPath, paint)
    }

    override fun getMinHeight(barBounds: RectF): Float = 0f

}

fun DrawableBarPath(
    drawable: Drawable,
    otherCreator: BarPathCreator? = DefaultBarPath()
): BarPathCreator = object : BarPathCreator {

    private val ratio: Float = drawable.intrinsicWidth.coerceAtLeast(1) /
            drawable.intrinsicHeight.coerceAtLeast(1).toFloat()

    override fun drawBarPath(
        canvas: Canvas,
        paint: Paint,
        barPath: Path,
        drawBounds: RectF,
        barBounds: RectF,
        animationOffset: Float,
        entry: AnyEntry
    ) {
        if (barBounds.height() == 0f) return
        val drawableHeight = barBounds.width() * ratio
        val top = minOf(barBounds.top, barBounds.bottom - drawableHeight)
        drawable.setBounds(barBounds.left, top, barBounds.right, top + drawableHeight)
        drawable.draw(canvas)
        otherCreator ?: return

        barBounds.updateBounds(top = drawable.bounds.bottom.toFloat())
        if (barBounds.height() > otherCreator.getMinHeight(barBounds)) {
            otherCreator.drawBarPath(
                canvas,
                paint,
                barPath,
                drawBounds,
                barBounds,
                animationOffset,
                entry
            )
        }
    }

    override fun getMinHeight(barBounds: RectF): Float = barBounds.width() * ratio

}