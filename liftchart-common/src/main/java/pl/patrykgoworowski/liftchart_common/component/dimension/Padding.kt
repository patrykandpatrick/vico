package pl.patrykgoworowski.liftchart_common.component.dimension

import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

public interface Padding {

    public val padding: MutableDimensions

    public fun setPadding(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ) {
        padding.set(start, top, end, bottom)
    }

    public fun setPadding(
        horizontal: Float = 0f,
        vertical: Float = 0f,
    ) {
        padding.set(horizontal, vertical, horizontal, vertical)
    }

    public fun setPadding(
        all: Float = 0f
    ) {
        padding.set(all)
    }

}