package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_common.path.rectShape

public open class RectComponent(
    color: Int,
    public var thickness: Float = 2f,
    shape: Shape = rectShape(),
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
            bottom = centerY + (scaledThickness / 2)
        )
    }

    public open fun fitsInHorizontal(
        left: Float,
        right: Float,
        centerY: Float,
        boundingBox: RectF
    ): Boolean = fitsIn(
        left = left,
        top = centerY - (scaledThickness / 2),
        right = right,
        bottom = centerY + (scaledThickness / 2),
        boundingBox = boundingBox
    )

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
            bottom = bottom
        )
    }

    public open fun fitsInVertical(
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: RectF
    ): Boolean = fitsIn(
        left = centerX - (scaledThickness / 2),
        top = top,
        right = centerX + (scaledThickness / 2),
        bottom = bottom,
        boundingBox = boundingBox
    )

}