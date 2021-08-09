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

fun RectF.updateBy(
    left: Float = 0f,
    top: Float = 0f,
    right: Float = 0f,
    bottom: Float = 0f
) {
    set(
        left = this.left + left,
        top = this.top + top,
        right = this.right + right,
        bottom = this.bottom + bottom)
}

fun RectF.set(
    left: Number,
    top: Number,
    right: Number,
    bottom: Number
) {
    set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}

val RectF.isNotEmpty: Boolean
    get() = left != 0f && top != 0f && right != 0f && bottom != 0f

fun RectF.clear() {
    set(0, 0, 0, 0)
}

fun RectF.set(
    isLTR: Boolean,
    left: Number = this.left,
    top: Number = this.top,
    right: Number = this.right,
    bottom: Number = this.bottom,
) {
    set(
        if (isLTR) left.toFloat() else right.toFloat(),
        top.toFloat(),
        if (isLTR) right.toFloat() else left.toFloat(),
        bottom.toFloat()
    )
}

fun RectF.start(isLTR: Boolean): Float = if (isLTR) left else right

fun RectF.end(isLTR: Boolean): Float = if (isLTR) right else left

operator fun RectF.component1(): Float = left
operator fun RectF.component2(): Float = top
operator fun RectF.component3(): Float = right
operator fun RectF.component4(): Float = bottom