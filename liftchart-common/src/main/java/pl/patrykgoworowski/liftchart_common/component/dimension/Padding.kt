package pl.patrykgoworowski.liftchart_common.component.dimension

import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

public interface Padding {

    public val padding: MutableDimensions<Float>

    public fun setPadding(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ) {
        padding.set(start, top, end, bottom)
    }

    public fun setPadding(
        all: Float = 0f
    ) {
        padding.set(all)
    }

}