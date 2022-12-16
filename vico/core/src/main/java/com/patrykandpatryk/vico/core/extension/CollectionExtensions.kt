/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.core.extension

import com.patrykandpatryk.vico.core.constants.ERR_REPEATING_COLLECTION_EMPTY
import kotlin.math.abs

internal fun <T> List<T>.getRepeating(index: Int): T {
    if (isEmpty()) throw IllegalStateException(ERR_REPEATING_COLLECTION_EMPTY)
    return get(index % size.coerceAtLeast(1))
}

/**
 * Replaces all of the elements of this [MutableList] with the elements of the provided collection.
 */
public fun <T> MutableList<T>.setAll(other: Collection<T>) {
    clear()
    addAll(other)
}

/**
 * Replaces all of the elements in child [ArrayList]s of this [ArrayList] with the elements of the provided [List].
 */
public fun <T> ArrayList<ArrayList<T>>.setToAllChildren(other: List<Collection<T>>) {
    ensureSize(other.size)
    forEachIndexed { index, childArrayList ->
        childArrayList.clear()
        if (other.lastIndex >= index) {
            childArrayList.addAll(other[index])
        }
    }
}

private fun <T> ArrayList<ArrayList<T>>.ensureSize(size: Int) {
    if (this.size >= size) return
    for (index in 0 until size - this.size) {
        add(ArrayList())
    }
}

/**
 * Replaces all of the elements of this [MutableMap] with the elements of the provided map.
 */
public fun <K, V> MutableMap<K, V>.setAll(other: Map<K, V>) {
    clear()
    other.forEach { (key, value) -> set(key, value) }
}

/**
 * Replaces all of the elements of this [MutableList] with the elements of the provided array.
 */
public fun <T> MutableList<T>.setAll(other: Array<out T>) {
    clear()
    addAll(other)
}

/**
 * Calls the [selector] function for each value in the collection and returns the sum of the produced [Float]s.
 */
public inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Calls the [selector] function for each element in the collection, providing the index of the element and [Boolean]s
 * indicating whether the element is the first or last element in the collection.
 */
public inline fun <T> Iterable<T>.forEachIndexedExtended(
    selector: (index: Int, isFirst: Boolean, isLast: Boolean, value: T) -> Unit,
) {
    var index = 0
    val iterator = iterator()
    var next: T
    while (iterator.hasNext()) {
        next = iterator.next()
        selector(index, index == 0, !iterator.hasNext(), next)
        index++
    }
}

internal fun Collection<Float>.findClosestPositiveValue(value: Float): Float? {
    if (isEmpty()) return null
    var closestValue: Float? = null
    forEach { checkedValue ->
        closestValue = when {
            closestValue == null -> checkedValue
            abs(closestValue!! - value) > abs(checkedValue - value) -> checkedValue
            else -> closestValue
        }
    }
    return closestValue
}

/**
 * Calls the [selector] function for each value in the collection and returns the average of the produced values.
 */
internal fun <T> Collection<T>.averageOf(selector: (T) -> Float): Float =
    fold(0f) { sum, element ->
        sum + selector(element)
    } / size

/**
 * Calls the [selector] function for each value in the collection and returns the sum of the produced values.
 */
public inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

/**
 * Creates a [MutableList] containing all elements of the specified source collection.
 */
public fun <T> mutableListOf(sourceCollection: Collection<T>): MutableList<T> =
    ArrayList<T>(sourceCollection.size).apply { addAll(sourceCollection) }
