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

package com.patrykandpatrick.vico.core.common

import androidx.annotation.RestrictTo
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.random.Random

internal const val PI_RAD: Float = 180f

internal const val DOUBLE_GCD_DECIMALS = 4

private fun Double.roundToNearest(decimals: Int): Double {
  val multiplier = 10f.pow(decimals)
  return (this * multiplier).roundedToNearest / multiplier
}

private fun Double.gcdWithImpl(other: Double, threshold: Double): Double =
  when {
    this < other -> other.gcdWithImpl(this, threshold)
    abs(other) < threshold -> this
    else -> other.gcdWithImpl(this - floor(this / other) * other, threshold)
  }

internal fun Double.gcdWith(other: Double) =
  gcdWithImpl(other = other, threshold = 10.0.pow(-DOUBLE_GCD_DECIMALS - 1))
    .roundToNearest(DOUBLE_GCD_DECIMALS)

internal fun <T : Comparable<T>> T.isBoundOf(range: ClosedFloatingPointRange<T>) =
  this == range.start || this == range.endInclusive

internal val ClosedFloatingPointRange<Double>.length
  get() = endInclusive - start

internal fun ClosedFloatingPointRange<Double>.random() = start + Random.nextDouble() * length

internal fun Int.getDivisors(includeDividend: Boolean = true) = buildList {
  add(1)
  for (i in 2..sqrt(toFloat()).toInt()) {
    if (this@getDivisors % i == 0) {
      add(i)
      val derived = this@getDivisors / i
      if (derived != i) add(derived)
    }
  }
  if (includeDividend) add(this@getDivisors)
  sort()
}

internal inline val Int.half: Int
  get() = this / 2

/** @suppress */
public inline val Float.half: Float
  @RestrictTo(RestrictTo.Scope.LIBRARY) get() = this / 2

internal inline val Double.half: Double
  get() = this / 2

internal inline val Float.doubled: Float
  get() = 2 * this

internal inline val Double.doubled: Double
  get() = 2 * this

/** @suppress */
public inline val Float?.orZero: Float
  @RestrictTo(RestrictTo.Scope.LIBRARY) get() = this ?: 0f

internal val Double?.orZero: Double
  get() = this ?: 0.0

internal inline val Int?.orZero: Int
  get() = this ?: 0

internal inline val Double.roundedToNearest: Double
  get() {
    val absoluteValue = abs(this)
    return sign * if (absoluteValue % 1 >= 0.5) ceil(absoluteValue) else floor(absoluteValue)
  }

internal inline val Float.piRad: Float
  get() = this * PI_RAD

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

internal fun Float.lerp(to: Float, fraction: Float): Float = this + (to - this) * fraction
