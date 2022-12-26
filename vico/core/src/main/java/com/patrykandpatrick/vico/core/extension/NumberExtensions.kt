/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.core.extension

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The number of degrees equivalent to π radians.
 */
public const val PI_RAD: Float = 180f

internal const val FLOAT_GCD_DECIMALS = 3

private fun Float.round(decimals: Int): Float {
    val multiplier = 10f.pow(n = decimals)
    return (this * multiplier).round / multiplier
}

private fun Float.gcdWithImpl(other: Float, threshold: Float): Float = when {
    this < other -> other.gcdWithImpl(other = this, threshold = threshold)
    abs(x = other) < threshold -> this
    else -> other.gcdWithImpl(other = this - (this / other).floor * other, threshold = threshold)
}

internal fun Float.gcdWith(other: Float): Float = gcdWithImpl(
    other = other,
    threshold = 10f.pow(n = -FLOAT_GCD_DECIMALS - 1),
).round(decimals = FLOAT_GCD_DECIMALS)

internal fun Float.toPrettyString(): String = if (this < 0f) "−${-this}" else this.toString()

/**
 * Half of this value.
 */
public inline val Int.half: Int
    get() = this / 2

/**
 * Half of this value.
 */
public inline val Float.half: Float
    get() = this / 2

/**
 * Half of this value.
 */
public inline val Double.half: Double
    get() = this / 2

/**
 * Two times this value.
 */
public inline val Float.doubled: Float
    get() = this * 2

/**
 * This value as an integer if this value is not null, or zero otherwise.
 */
public inline val Number?.orZeroInt: Int
    get() = this?.toInt() ?: 0

/**
 * This value as an integer if this value is not null, or zero otherwise.
 */
public inline val Float?.orZero: Float
    get() = this ?: 0f

/**
 * This value as an integer if this value is not null, or zero otherwise.
 */
public inline val Int?.orZero: Int
    get() = this ?: 0

/**
 * This value rounded to the nearest whole [Float].
 */
public inline val Float.round: Float
    get() = roundToInt().toFloat()

/**
 * 180 degrees (π radians) times this value.
 */
public inline val Float.piRad: Float
    get() = this * PI_RAD

/**
 * The largest whole [Float] smaller than or equal to this value.
 */
public inline val Float.floor: Float
    get() = floor(this)

/**
 * The smallest whole [Float] greater than or equal to this value.
 */
public inline val Float.ceil: Float
    get() = ceil(this)

/**
 * The median of the values in this range.
 */
public inline val ClosedFloatingPointRange<Float>.median: Float
    get() = (endInclusive + start) / 2

/**
 * Whether this value contains the provided bit flag.
 */
public fun Int.hasFlag(flag: Int): Boolean = this and flag == flag

/**
 * Whether this value contains any of the provided bit flags.
 */
public fun Int.hasAnyFlagOf(vararg flags: Int): Boolean = flags.any(::hasFlag)

/**
 * The first non-negative value of the values provided, or `null` if none of the values is non-negative.
 */
public fun firstNonNegativeOf(vararg floats: Float): Float? = floats.firstOrNull { it >= 0f }

/**
 * Creates a range from this value to the provided value or from the
 * provided value to this value, depending on which value is larger.
 */
public fun Float.rangeWith(other: Float): ClosedFloatingPointRange<Float> =
    if (other > this) this..other else other..this
