package pl.patrykgoworowski.liftchart_common

import android.graphics.RectF

interface BoundsAware {

    val bounds: RectF

    fun setBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    )

    fun setBounds(bounds: RectF) =
        setBounds(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )
}