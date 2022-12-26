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

/**
 * Calls the [selector] function for each value in the collection and returns the range of the produced values.
 */
public inline fun <T> Iterable<T>.rangeOfOrNull(selector: (T) -> Float): ClosedFloatingPointRange<Float>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var minValue = selector(iterator.next())
    var maxValue = minValue
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        minValue = minOf(minValue, v)
        maxValue = maxOf(maxValue, v)
    }
    return minValue..maxValue
}

/**
 * Calls the [selector] function for each value in the collection and returns the range of the produced values.
 */
public inline fun <T> Iterable<T>.rangeOfPairOrNull(
    selector: (T) -> Pair<Float, Float>,
): ClosedFloatingPointRange<Float>? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    var (minValue, maxValue) = selector(iterator.next())
    while (iterator.hasNext()) {
        val (negValue, posValue) = selector(iterator.next())
        minValue = minOf(minValue, negValue)
        maxValue = maxOf(maxValue, posValue)
    }
    return minValue..maxValue
}
