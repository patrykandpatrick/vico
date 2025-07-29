/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

@JvmName("copyDouble") internal fun <T> List<List<T>>.copy() = map { it.toList() }

@JvmName("copyTriple") internal fun <T> List<List<List<T>>>.copy() = map { it.copy() }

internal fun <T> MutableList<T>.setAll(other: Collection<T>) {
  clear()
  addAll(other)
}

internal fun <T> ArrayList<ArrayList<T>>.copy(): List<List<T>> =
  List(size) { index -> ArrayList(get(index)) }

internal fun <T> Collection<T>.averageOf(selector: (T) -> Float): Float =
  fold(0f) { sum, element -> sum + selector(element) } / size

internal fun <T> mutableListOf(sourceCollection: Collection<T>): MutableList<T> =
  ArrayList<T>(sourceCollection.size).apply { addAll(sourceCollection) }

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun <T> List<T>.getRepeating(index: Int): T {
  if (isEmpty()) throw IllegalStateException(ERR_REPEATING_COLLECTION_EMPTY)
  return get(index % size.coerceAtLeast(1))
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun <K1, V1, K2, V2> Map<K1, V1>.mapKeysAndValues(
  transform: (K1, V1) -> Pair<K2, V2>
): Map<K2, V2> =
  buildMap(size) {
    this@mapKeysAndValues.forEach { (key, value) ->
      val (newKey, newValue) = transform(key, value)
      put(newKey, newValue)
    }
  }
