package pl.patrykgoworowski.liftchart_common.data_set.axis.component

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape

public open class AxisComponent(
    color: Int,
    public var thickness: Float = 2f,
    public var shape: Shape = RectShape(),
) : Component(color) {

    val shouldDraw: Boolean
        get() = thickness > 0f

    public open fun draw(
        canvas: Canvas,
        bounds: RectF,
    ) {
        draw(canvas, shape, bounds)
    }

    public open fun drawHorizontal(
        canvas: Canvas,
        left: Float,
        right: Float,
        centerY: Float,
    ) {
        draw(
            canvas = canvas,
            shape = shape,
            left = left,
            top = centerY - (thickness / 2),
            right = right,
            bottom =centerY + (thickness / 2)
        )
    }

    public open fun drawVertical(
        canvas: Canvas,
        top: Float,
        bottom: Float,
        centerX: Float,
    ) {
        draw(
            canvas = canvas,
            shape = shape,
            left = centerX - (thickness / 2),
            top = top,
            right = centerX + (thickness / 2),
            bottom =bottom
        )
    }

}