package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.path.Shape

public abstract class Component(
    color: Int
) {

    protected val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val drawBounds: RectF = RectF()
    protected val path: Path = Path()

    public var color by paint::color

    init {
        paint.color = color
    }

    open fun draw(
        canvas: Canvas,
        shape: Shape,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        drawBounds.set(left, top, right, bottom)
        path.reset()
        shape.drawShape(canvas, paint, path, drawBounds)
    }

    open fun draw(
        canvas: Canvas,
        shape: Shape,
        bounds: RectF
    ) {
        draw(canvas, shape, bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

}