package pl.patrykgoworowski.liftchart_common.dimensions

fun floatDimensions(
    start: Float = 0f,
    top: Float = 0f,
    end: Float = 0f,
    bottom: Float = 0f,
) = MutableDimensions(start, top, end, bottom)