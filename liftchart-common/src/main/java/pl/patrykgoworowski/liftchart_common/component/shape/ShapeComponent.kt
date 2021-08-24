package pl.patrykgoworowski.liftchart_common.component.shape

import android.graphics.*
import pl.patrykgoworowski.liftchart_common.DEF_SHADOW_COLOR
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.path.Shape

open class ShapeComponent<T : Shape>(
    var shape: T,
    color: Int = Color.BLACK,
    var dynamicShader: DynamicShader? = null
) : Component() {

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val drawBounds: RectF = RectF()
    protected val path: Path = Path()

    val parentBounds = RectF()

    var color by paint::color

    init {
        paint.color = color
    }

    fun setParentBounds(bounds: RectF) {
        parentBounds.set(bounds)
    }

    fun setParentBounds(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        parentBounds.set(left, top, right, bottom)
    }

    override fun draw(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        if (left == right || top == bottom) return //Skip drawing shape that will be invisible.
        updateDrawBounds(left, top, right, bottom)
        path.reset()
        applyShader(left, top, right, bottom)
        shape.drawShape(canvas, paint, path, drawBounds)
    }

    protected fun applyShader(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        dynamicShader
            ?.provideShader(left, top, right, bottom, parentBounds)
            ?.let { shader -> paint.shader = shader }
    }

    open fun updateDrawBounds(
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
    }

    open fun fitsIn(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        boundingBox: RectF
    ): Boolean = boundingBox.contains(left, top, right, bottom)

    open fun intersects(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        boundingBox: RectF
    ): Boolean = boundingBox.intersects(left, top, right, bottom)

    fun setShadow(
        radius: Float,
        dx: Float = 0f,
        dy: Float = 0f,
        color: Int = DEF_SHADOW_COLOR,
    ) {
        paint.setShadowLayer(radius, dx, dy, color)
    }

}