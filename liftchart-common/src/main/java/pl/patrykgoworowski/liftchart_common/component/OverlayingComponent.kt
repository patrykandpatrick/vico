package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas

class OverlayingComponent(
    public val outer: Component,
    public val inner: Component,
    innerPaddingStart: Float = 0f,
    innerPaddingTop: Float = 0f,
    innerPaddingEnd: Float = 0f,
    innerPaddingBottom: Float = 0f,
) : Component() {

    constructor(
        outer: Component,
        inner: Component,
        innerPaddingAll: Float = 0f,
    ) : this(outer, inner, innerPaddingAll, innerPaddingAll, innerPaddingAll, innerPaddingAll)

    init {
        inner.margins.set(
            innerPaddingStart,
            innerPaddingTop,
            innerPaddingEnd,
            innerPaddingBottom
        )
    }

    override fun draw(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val leftWithMargin = left + margins.start
        val topWithMargin = top + margins.top
        val rightWithMargin = right - margins.end
        val bottomWithMargin = bottom - margins.bottom

        outer.draw(canvas, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)
        inner.draw(canvas, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)
    }

}