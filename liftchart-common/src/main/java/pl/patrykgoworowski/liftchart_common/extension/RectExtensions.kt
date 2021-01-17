package pl.patrykgoworowski.liftchart_common.extension

import android.graphics.RectF

fun RectF.updateBounds(
    left: Float = this.left,
    top: Float = this.top,
    right: Float = this.right,
    bottom: Float = this.bottom
) {
    set(left, top, right, bottom)
}

fun RectF.set(
    left: Number,
    top: Number,
    right: Number,
    bottom: Number
) {
    set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}