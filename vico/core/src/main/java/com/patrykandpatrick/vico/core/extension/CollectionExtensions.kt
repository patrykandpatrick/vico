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

import com.patrykandpatrick.vico.core.constants.ERR_REPEATING_COLLECTION_EMPTY
import kotlin.math.abs

internal fun <T> List<T>.getRepeating(index: Int): T {
    if (isEmpty()) throw IllegalStateException(ERR_REPEATING_COLLECTION_EMPTY)
    return get(index % size.coerceAtLeast(1))
}

@JvmName("copyDouble")
internal fun <T> List<List<T>>.copy() = map { it.toList() }

@JvmName("copyTriple")
internal fun <T> List<List<List<T>>>.copy() = map { it.copy() }

internal fun <T> MutableList<T>.setAll(other: Collection<T>) {
    clear()
    addAll(other)
}

internal fun <T> ArrayList<ArrayList<T>>.copy(): List<List<T>> = List(size) { index -> ArrayList(get(index)) }

internal fun <K, V> MutableMap<K, V>.setAll(other: Map<K, V>) {
    clear()
    other.forEach { (key, value) -> set(key, value) }
}

internal fun Collection<Float>.findClosestPositiveValue(value: Float): Float? {
    if (isEmpty()) return null
    var closestValue: Float? = null
    forEach { checkedValue ->
        closestValue =
            when {
                closestValue == null -> checkedValue
                abs(closestValue!! - value) > abs(checkedValue - value) -> checkedValue
                else -> closestValue
            }
    }
    return closestValue
}

internal fun <T> Collection<T>.averageOf(selector: (T) -> Float): Float =
    fold(0f) { sum, element ->
        sum + selector(element)
    } / size

internal inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

internal fun <T> mutableListOf(sourceCollection: Collection<T>): MutableList<T> =
    ArrayList<T>(sourceCollection.size).apply { addAll(sourceCollection) }
