package pl.patrykgoworowski.liftchart_common.component.shape

import android.graphics.*
import pl.patrykgoworowski.liftchart_common.DEF_SHADOW_COLOR
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.component.dimension.setMargins
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.emptyDimensions
import pl.patrykgoworowski.liftchart_common.path.Shape

public open class ShapeComponent<T : Shape>(
    public var shape: T,
    color: Int = Color.BLACK,
    public var dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
) : Component() {

    public val parentBounds = RectF()
    public val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected val drawBounds: RectF = RectF()
    protected val path: Path = Path()

    public var color by paint::color

    init {
        paint.color = color
        setMargins(margins)
    }

    public fun setParentBounds(bounds: RectF) {
        parentBounds.set(bounds)
    }

    public fun setParentBounds(
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
        applyShader(parentBounds)
        shape.drawShape(canvas, paint, path, drawBounds)
    }

    protected fun applyShader(
        bounds: RectF,
    ) {
        dynamicShader
            ?.provideShader(bounds)
            ?.let { shader -> paint.shader = shader }
    }

    public open fun updateDrawBounds(
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
    ): ShapeComponent<T> {
        paint.setShadowLayer(radius, dx, dy, color)
        return this
    }
}
