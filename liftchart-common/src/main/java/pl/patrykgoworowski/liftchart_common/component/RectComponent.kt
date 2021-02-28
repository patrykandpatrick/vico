package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape

public open class RectComponent(
    color: Int,
    public var thickness: Float = 2f,
    shape: Shape = RectShape(),
) : Component(shape, color) {

    var thicknessScale: Float = 1f

    val scaledThickness: Float
        get() = thickness * thicknessScale

    val shouldDraw: Boolean
        get() = thickness > 0f

    public open fun drawHorizontal(
        canvas: Canvas,
        left: Float,
        right: Float,
        centerY: Float,
    ) {
        draw(
            canvas = canvas,
            left = left,
            top = centerY - (scaledThickness / 2),
            right = right,
            bottom =centerY + (scaledThickness / 2)
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
            left = centerX - (scaledThickness / 2),
            top = top,
            right = centerX + (scaledThickness / 2),
            bottom =bottom
        )
    }

}