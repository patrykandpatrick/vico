package pl.patrykgoworowski.liftchart_common.component.dimension

import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

public interface Margins {

    public val margins: MutableDimensions

    public fun setMargins(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ) {
        margins.set(start, top, end, bottom)
    }

    public fun setMargins(
        all: Float = 0f
    ) {
        margins.set(all)
    }

}