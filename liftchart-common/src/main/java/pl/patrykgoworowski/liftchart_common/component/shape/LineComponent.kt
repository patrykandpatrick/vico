package pl.patrykgoworowski.liftchart_common.component.shape

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_common.path.rectShape

open class LineComponent(
    color: Int,
    var thickness: Float = 2f,
    shape: Shape = rectShape(),
    dynamicShader: DynamicShader? = null
) : ShapeComponent<Shape>(shape, color, dynamicShader) {

    var thicknessScale: Float = 1f

    val scaledThickness: Float
        get() = thickness * thicknessScale

    open fun drawHorizontal(
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

    open fun fitsInHorizontal(
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

    open fun drawVertical(
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

    open fun fitsInVertical(
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

    open fun intersectsVertical(
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: RectF
    ): Boolean = intersects(
        left = centerX - (scaledThickness / 2),
        top = top,
        right = centerX + (scaledThickness / 2),
        bottom = bottom,
        boundingBox = boundingBox
    )

    override fun updateDrawBounds(left: Float, top: Float, right: Float, bottom: Float) {
        val centerX = left + ((right - left) / 2)
        val centerY = top + ((bottom - top) / 2)
        drawBounds.set(
            minOf(left + margins.start, centerX),
            minOf(top + margins.top, centerY),
            maxOf(right - margins.end, centerX),
            maxOf(bottom - margins.bottom, centerY)
        )
    }

}