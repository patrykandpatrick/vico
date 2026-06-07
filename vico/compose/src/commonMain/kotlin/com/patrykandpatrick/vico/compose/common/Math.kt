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

internal inline val Dp?.orZero: Dp
  get() = this ?: 0f.dp

internal fun Number.toRadians(): Double = toDouble() * PI / PI_RAD

internal fun Double.format(
  decimalCount: Int = 2,
  decimalSeparator: String = ".",
  thousandsSeparator: String = "",
  prefix: String = "",
  suffix: String = "",
): String {
  val isNegative = this < 0
  val factor = 10.0.pow(decimalCount)
  val truncated = floor(factor * absoluteValue) / factor
  val trimmed = truncated.toString().trimEnd('0').trimEnd('.').replace(".", decimalSeparator)
  val value = if (isNegative) "−$trimmed" else trimmed
  return buildString {
    append(prefix)
    append(value.addThousandsSeparator(decimalSeparator, thousandsSeparator))
    append(suffix)
  }
}

private fun String.addThousandsSeparator(
  decimalSeparator: String,
  thousandsSeparator: String,
): String {
  val parts = split(decimalSeparator)
  val integerPart = parts[0]
  val withCommas = integerPart.reversed().chunked(3).joinToString(thousandsSeparator).reversed()
  return buildString {
    append(withCommas)
    if (parts.size > 1) {
      append(decimalSeparator)
      append(parts[1])
    }
  }
}
