package pl.patrykgoworowski.liftchart_common.extension

import kotlin.math.roundToInt

inline val Int.half: Int
    get() = this / 2

inline val Float.half: Float
    get() = this / 2

inline val Float.doubled: Float
    get() = this * 2

inline val Number?.orZeroInt: Int
    get() = this?.toInt() ?: 0

inline val Float?.orZero: Float
    get() = this ?: 0f

inline val Int?.orZero: Int
    get() = this ?: 0

inline val Float.round: Float
    get() = roundToInt().toFloat()

fun Float.between(min: Float, max: Float): Float =
    minOf(maxOf(this, min), max)
