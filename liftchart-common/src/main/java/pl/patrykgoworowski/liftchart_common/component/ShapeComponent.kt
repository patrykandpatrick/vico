package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.path.Shape

public abstract class ShapeComponent(
    public var shape: Shape,
    color: Int,
) : Component() {

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val drawBounds: RectF = RectF()
    protected val path: Path = Path()

    public var color by paint::color

    init {
        paint.color = color
    }

    override fun draw(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        drawBounds.set(left, top, right, bottom)
        path.reset()
        shape.drawShape(canvas, paint, path, drawBounds)
    }

    open fun fitsIn(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        boundingBox: RectF
    ): Boolean = boundingBox.contains(left, top, right, bottom)
}