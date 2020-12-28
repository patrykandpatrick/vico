package pl.patrykgoworowski.liftchart_core.extension

import android.graphics.RectF

fun RectF.updateBounds(
    left: Float = this.left,
    top: Float = this.top,
    right: Float = this.right,
    bottom: Float = this.bottom
) {
    set(left, top, right, bottom)
}