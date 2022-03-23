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

import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.model.Point
import java.util.TreeMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Returns that of the [Marker.EntryModel]s stored in the [Map] whose x-axis map key is the closest to the [Point.x]
 * value.
 *
 * @see Marker.EntryModel
 */
public fun Map<Float, List<Marker.EntryModel>>.getClosestMarkerEntryModel(
    touchPoint: Point,
): List<Marker.EntryModel>? = keys.findClosestPositiveValue(touchPoint.x)?.let(::get)

/**
 * Returns those of the [Marker.EntryModel]s stored in the [Map] whose [pl.patrykgoworowski.vico.core.entry.ChartEntry.x]
 * is equal to [xValue].
 *
 * @see Marker.EntryModel
 */
public fun Map<Float, List<Marker.EntryModel>>.getEntryModel(
    xValue: Float,
): List<Marker.EntryModel>? = values.mapNotNull { entries ->
    if (entries.firstOrNull()?.entry?.x == xValue) entries else null
}.flatten()
    .takeIf { it.isNotEmpty() }

/**
 * Updates the receiver [TreeMap] with the contents of another [Map].
 */
public fun <K, V> TreeMap<K, MutableList<V>>.updateAll(other: Map<K, List<V>>) {
    other.forEach { (key, value) ->
        put(key, get(key)?.apply { addAll(value) } ?: mutableListOf(value))
    }
}

internal inline fun <K, V> HashMap<K, MutableList<V>>.updateList(
    key: K,
    initialCapacity: Int = 0,
    block: MutableList<V>.() -> Unit,
) {
    block(getOrPut(key) { ArrayList(initialCapacity) })
}
