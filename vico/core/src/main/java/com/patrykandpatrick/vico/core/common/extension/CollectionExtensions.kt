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

package com.patrykandpatrick.vico.core.common.extension

import com.patrykandpatrick.vico.core.common.ERR_REPEATING_COLLECTION_EMPTY
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

/**
 * Returns the largest value among all values produced by [selector] function
 * applied to each element in the collection or `null` if there are no elements.
 */
public inline fun <T, R : Comparable<R>> Iterable<T>.maxOfOrNullIndexed(selector: (Int, T) -> R): R? {
    val iterator = iterator()
    var index = 0
    if (!iterator.hasNext()) return null
    var maxValue = selector(index++, iterator.next())
    while (iterator.hasNext()) {
        val v = selector(index++, iterator.next())
        if (maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

internal inline fun <T, R> Iterable<T>.mapWithPrevious(action: (previous: T?, current: T) -> R): List<R> {
    val result = mutableListOf<R>()
    var previous: T? = null
    for (element in this) {
        result.add(action(previous, element))
        previous = element
    }
    return result
}
