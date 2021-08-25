package pl.patrykgoworowski.liftchart_common.component.dimension

import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

interface Margins {

    val margins: MutableDimensions

    fun setMargins(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ) {
        margins.set(start, top, end, bottom)
    }

    fun setMargins(
        all: Float = 0f
    ) {
        margins.set(all)
    }

}