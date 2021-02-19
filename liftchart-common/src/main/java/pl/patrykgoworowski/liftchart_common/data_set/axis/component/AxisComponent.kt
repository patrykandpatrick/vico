package pl.patrykgoworowski.liftchart_common.data_set.axis.component

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.component.Component

public open class AxisComponent(
    color: Int,
    var thickness: Float = 2f
) : Component(color) {

    val shouldDraw: Boolean
        get() = thickness > 0f

    public open fun drawHorizontal(
        canvas: Canvas,
        left: Float,
        right: Float,
        centerY: Float
    ) {
        draw(
            canvas = canvas,
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
        centerX: Float
    ) {
        draw(
            canvas = canvas,
            left = centerX - (thickness / 2),
            top = top,
            right = centerX + (thickness / 2),
            bottom =bottom
        )
    }

}