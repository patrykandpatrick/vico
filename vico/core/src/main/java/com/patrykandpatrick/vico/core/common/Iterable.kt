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

import kotlin.math.max
import kotlin.math.min

internal inline fun <T> Iterable<T>.rangeOf(
  selector: (T) -> Double
): ClosedFloatingPointRange<Double> {
  val iterator = iterator()
  var minValue = selector(iterator.next())
  var maxValue = minValue
  while (iterator.hasNext()) {
    val v = selector(iterator.next())
    minValue = min(minValue, v)
    maxValue = max(maxValue, v)
  }
  return minValue..maxValue
}

internal inline fun <T> Iterable<T>.rangeOfPair(
  selector: (T) -> Pair<Double, Double>
): ClosedFloatingPointRange<Double> {
  val iterator = iterator()
  var (minValue, maxValue) = selector(iterator.next())
  while (iterator.hasNext()) {
    val (negValue, posValue) = selector(iterator.next())
    minValue = min(minValue, negValue)
    maxValue = max(maxValue, posValue)
  }
  return minValue..maxValue
}
