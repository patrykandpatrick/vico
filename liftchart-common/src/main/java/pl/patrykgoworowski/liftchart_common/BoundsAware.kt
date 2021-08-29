package pl.patrykgoworowski.liftchart_common

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.extension.set

interface BoundsAware {

    val bounds: RectF

    fun setBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    ) {
        bounds.set(left, top, right, bottom)
    }

    fun setBounds(bounds: RectF) =
        setBounds(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )
}