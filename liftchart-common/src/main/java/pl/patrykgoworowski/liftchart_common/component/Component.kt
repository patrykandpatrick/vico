package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.dimension.DefaultMargins
import pl.patrykgoworowski.liftchart_common.component.dimension.Margins

abstract class Component : Margins by DefaultMargins() {

    abstract fun draw(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    )

    open fun draw(
        canvas: Canvas,
        bounds: RectF
    ) {
        draw(canvas, bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

}