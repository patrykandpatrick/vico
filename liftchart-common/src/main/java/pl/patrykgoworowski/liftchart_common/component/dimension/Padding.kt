package pl.patrykgoworowski.liftchart_common.component.dimension

import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

interface Padding {

    val padding: MutableDimensions

    fun setPadding(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ) {
        padding.set(start, top, end, bottom)
    }

    fun setPadding(
        horizontal: Float = 0f,
        vertical: Float = 0f,
    ) {
        padding.set(horizontal, vertical, horizontal, vertical)
    }

    fun setPadding(
        all: Float = 0f
    ) {
        padding.set(all)
    }

}