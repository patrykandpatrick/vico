package pl.patrykgoworowski.liftchart_common.component

import android.graphics.*
import pl.patrykgoworowski.liftchart_common.DEF_SHADOW_COLOR
import pl.patrykgoworowski.liftchart_common.path.Shape

public open class ShapeComponent<T : Shape>(
    public var shape: T,
    color: Int = Color.BLACK,
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
        drawBounds.set(
            left + margins.start,
            top + margins.top,
            right - margins.end,
            bottom - margins.bottom,
        )
        path.reset()
        shape.drawShape(canvas, paint, path, drawBounds)
    }

    public open fun fitsIn(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        boundingBox: RectF
    ): Boolean = boundingBox.contains(left, top, right, bottom)

    public open fun intersects(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        boundingBox: RectF
    ): Boolean = boundingBox.intersects(left, top, right, bottom)

    public fun setShadow(
        radius: Float,
        dx: Float = 0f,
        dy: Float = 0f,
        color: Int = DEF_SHADOW_COLOR,
    ) {
        paint.setShadowLayer(radius, dx, dy, color)
    }

}