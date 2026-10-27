/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*
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

internal inline val Float.half: Float
  get() = this / 2

internal inline val Double.half: Double
  get() = this / 2

internal inline val Float.doubled: Float
  get() = 2 * this

internal inline val Double.doubled: Double
  get() = 2 * this

internal inline val Float?.orZero: Float
  get() = this ?: 0f

internal inline val Dp?.orZero: Dp
  get() = this ?: 0f.dp

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

internal fun Int.hasFlag(flag: Int): Boolean = this and flag == flag

internal fun firstNonNegativeOf(vararg floats: Float): Float? = floats.firstOrNull { it >= 0f }

internal fun Float.rangeWith(other: Float): ClosedFloatingPointRange<Float> =
  if (other > this) this..other else other..this

internal fun Float.lerp(to: Float, fraction: Float): Float = this + (to - this) * fraction

internal fun Number.toRadians(): Double = toDouble() * PI / PI_RAD

/** The number of significant digits that any decimal retains in a round trip through [Double]. */
private const val SIGNIFICANT_DIGITS = 15

/**
 * Formats this value with up to [decimalCount] decimal digits, rounding half away from zero. The
 * value is first rounded to [SIGNIFICANT_DIGITS] significant digits, which recovers the decimal
 * that it represents (e.g., 1.005 rather than 1.00499999999999989…). The digits are computed
 * arithmetically, as [Double.toString] differs between platforms (e.g., in its use of scientific
 * notation).
 */
internal fun Double.format(
  decimalCount: Int = 2,
  decimalSeparator: String = ".",
  thousandsSeparator: String = "",
  prefix: String = "",
  suffix: String = "",
): String {
  if (!isFinite()) return "$prefix${if (this < 0) "−" else ""}$absoluteValue$suffix"
  val (integerDigits, fractionDigits) = absoluteValue.toDecimalDigits(decimalCount.coerceAtLeast(0))
  val isNegative = this < 0 && (integerDigits != "0" || fractionDigits.isNotEmpty())
  return buildString {
    append(prefix)
    if (isNegative) append('−')
    append(integerDigits.withThousandsSeparator(thousandsSeparator))
    if (fractionDigits.isNotEmpty()) {
      append(decimalSeparator)
      append(fractionDigits)
    }
    append(suffix)
  }
}

/**
 * Returns the integer and fractional digits of this finite, nonnegative value rounded to
 * [SIGNIFICANT_DIGITS] significant digits and then half up to [decimalCount] decimal places. The
 * fractional digits have no trailing zeros.
 */
private fun Double.toDecimalDigits(decimalCount: Int): Pair<String, String> {
  if (this == 0.0) return "0" to ""
  val exponent = floor(log10(this)).toInt()
  // Values below 10^(−decimalCount − 1) round to zero.
  if (exponent < -decimalCount - 1) return "0" to ""
  // `this` ≈ `significand` × 10^−`scale`, where `significand` has `SIGNIFICANT_DIGITS` digits.
  var scale = SIGNIFICANT_DIGITS - 1 - exponent
  var significand = timesPowerOfTen(scale).roundToLong()
  // `log10` may underestimate, and rounding may carry into an extra digit.
  if (significand >= longPowerOfTen(SIGNIFICANT_DIGITS)) {
    scale--
    significand = timesPowerOfTen(scale).roundToLong()
  }
  if (scale > decimalCount) {
    val divisor = longPowerOfTen(scale - decimalCount)
    significand = (significand + divisor / 2) / divisor
    scale = decimalCount
  }
  val digits = significand.toString()
  return when {
    scale <= 0 -> digits + "0".repeat(-scale) to ""
    digits.length > scale -> digits.dropLast(scale) to digits.takeLast(scale).trimEnd('0')
    else -> "0" to digits.padStart(scale, '0').trimEnd('0')
  }
}

/** Multiplies this value by 10^[exponent], splitting the multiplication to avoid overflow. */
private fun Double.timesPowerOfTen(exponent: Int): Double =
  if (exponent >= 0) {
    this * 10.0.pow(exponent / 2) * 10.0.pow(exponent - exponent / 2)
  } else {
    this / 10.0.pow(-exponent)
  }

private fun longPowerOfTen(exponent: Int): Long {
  var result = 1L
  repeat(exponent) { result *= 10 }
  return result
}

private fun String.withThousandsSeparator(separator: String): String =
  if (separator.isEmpty()) {
    this
  } else {
    reversed().chunked(3).asReversed().joinToString(separator) { it.reversed() }
  }
