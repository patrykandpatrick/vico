/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.extension

import pl.patrykgoworowski.vico.core.constants.ERR_REPEATING_COLLECTION_EMPTY
import kotlin.math.abs

internal fun <T> ArrayList<T>.getOrDefault(index: Int, getDefault: () -> T): T =
    getOrNull(index) ?: getDefault().also { add(it) }

internal fun <T> List<T>.getRepeating(index: Int): T {
    if (isEmpty()) throw IllegalStateException(ERR_REPEATING_COLLECTION_EMPTY)
    return get(index % size.coerceAtLeast(1))
}

public fun <T> MutableList<T>.setAll(other: Collection<T>) {
    clear()
    addAll(other)
}

public fun <T> MutableList<T>.setAll(other: Array<out T>) {
    clear()
    addAll(other)
}

public inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

public inline fun <T> Iterable<T>.forEachIndexedExtended(
    selector: (index: Int, isFirst: Boolean, isLast: Boolean, value: T) -> Unit
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

internal fun <T> Collection<T>.averageOf(selector: (T) -> Float): Float =
    fold(0f) { sum, element ->
        sum + selector(element)
    } / size

public inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

public fun <T> mutableListOf(sourceCollection: Collection<T>): MutableList<T> =
    ArrayList<T>(sourceCollection.size).apply { addAll(sourceCollection) }
