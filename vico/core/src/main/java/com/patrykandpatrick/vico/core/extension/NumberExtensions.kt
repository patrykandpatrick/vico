/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

import androidx.annotation.RestrictTo
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * The number of degrees equivalent to π radians.
 */
public const val PI_RAD: Float = 180f

internal const val FLOAT_GCD_DECIMALS = 2

private fun Float.round(decimals: Int): Float {
    val multiplier = 10f.pow(n = decimals)
    return (this * multiplier).round / multiplier
}

private fun Float.gcdWithImpl(
    other: Float,
    threshold: Float,
): Float =
    when {
        this < other -> other.gcdWithImpl(other = this, threshold = threshold)
        abs(x = other) < threshold -> this
        else -> other.gcdWithImpl(other = this - (this / other).floor * other, threshold = threshold)
    }

internal fun Float.gcdWith(other: Float): Float =
    gcdWithImpl(
        other = other,
        threshold = 10f.pow(n = -FLOAT_GCD_DECIMALS - 1),
    ).round(decimals = FLOAT_GCD_DECIMALS)

internal fun <T : Comparable<T>> T.isBoundOf(range: ClosedFloatingPointRange<T>) =
    this == range.start || this == range.endInclusive

internal fun ClosedFloatingPointRange<Float>.random(): Float = start + (endInclusive - start) * Random.nextFloat()

internal inline val Int.half: Int
    get() = this / 2

/** @suppress */
public inline val Float.half: Float
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    get() = this / 2

internal inline val Double.half: Double
    get() = this / 2

internal inline val Float.doubled: Float
    get() = this * 2

/** @suppress */
public inline val Float?.orZero: Float
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    get() = this ?: 0f

internal inline val Int?.orZero: Int
    get() = this ?: 0

internal inline val Float.round: Float
    get() = roundToInt().toFloat()

internal inline val Float.piRad: Float
    get() = this * PI_RAD

internal inline val Float.floor: Float
    get() = floor(this)

internal inline val Float.ceil: Float
    get() = ceil(this)

internal inline val ClosedFloatingPointRange<Float>.median: Float
    get() = (endInclusive + start) / 2

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public fun Int.hasFlag(flag: Int): Boolean = this and flag == flag

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public fun firstNonNegativeOf(vararg floats: Float): Float? = floats.firstOrNull { it >= 0f }

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public fun Float.rangeWith(other: Float): ClosedFloatingPointRange<Float> =
    if (other > this) this..other else other..this

internal fun Float.lerp(
    to: Float,
    fraction: Float,
): Float = this + (to - this) * fraction

/**
 * Performs a linear progression between [start] and [end] values.
 * [progress] is a fraction ranged between 0 and 1.
 */
public fun progressValues(
    start: Float,
    end: Float,
    progress: Float,
): Float = start + (end - start) * progress
