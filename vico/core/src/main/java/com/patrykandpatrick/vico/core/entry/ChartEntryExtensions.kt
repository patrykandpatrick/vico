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

package com.patrykandpatrick.vico.core.entry

import com.patrykandpatrick.vico.core.extension.gcdWith
import com.patrykandpatrick.vico.core.extension.rangeOfOrNull
import com.patrykandpatrick.vico.core.extension.rangeOfPairOrNull
import kotlin.math.abs

/**
 * Creates a [FloatEntry] instance.
 *
 * @param [x] the [FloatEntry]’s _x_ coordinate.
 * @param [y] the [FloatEntry]’s _y_ coordinate.
 *
 * @see [entriesOf]
 */
public fun entryOf(x: Float, y: Float): FloatEntry = FloatEntry(x, y)

/**
 * Creates a [FloatEntry] instance.

 * @param [x] the [FloatEntry]’s _x_ coordinate. This will be converted to a [Float] instance.
 * @param [y] the [FloatEntry]’s _y_ coordinate. This will be converted to a [Float] instance.
 *
 * @see [entriesOf]
 */
public fun entryOf(x: Number, y: Number): FloatEntry = entryOf(x.toFloat(), y.toFloat())

/**
 * Creates a [List] of [FloatEntry] instances. Each of the provided [Pair]s corresponds to a single [FloatEntry], with
 * the first element of the [Pair] being the [FloatEntry]’s _x_ coordinate, and the second element of the [Pair] being
 * the [FloatEntry]’s _y_ coordinate.
 *
 * Example usage:
 *
 * ```
 *  entriesOf(0 to 1, 1 to 2, 3 to 5)
 * ```
 *
 * The provided [Number] instances will be converted to [Float] instances.
 *
 * @see [entryOf]
 */
public fun entriesOf(vararg pairs: Pair<Number, Number>): List<FloatEntry> =
    pairs.map { (x, y) -> entryOf(x, y) }

/**
 * Creates a [List] of [FloatEntry] instances out of an array of y-axis values.
 *
 * The following are equivalent:
 *
 * ```
 * entriesOf(1, 2, 5)
 * ```
 *
 * ```
 * entriesOf(0 to 1, 1 to 2, 2 to 5)
 * ```
 *
 * An x-axis value will be automatically assigned to each y-axis value from [yValues]. The x-axis value will be equal
 * to the y-axis value’s index in [yValues].
 *
 * The provided [Number] instances will be converted to [Float] instances.
 *
 * @see [entryOf]
 */
public fun entriesOf(vararg yValues: Number): List<FloatEntry> =
    yValues.mapIndexed { index, y -> entryOf(index, y) }

internal inline val Iterable<Iterable<ChartEntry>>.yRange: ClosedFloatingPointRange<Float>
    get() = flatten().rangeOfOrNull { it.y } ?: 0f..0f

internal inline val Iterable<Iterable<ChartEntry>>.xRange: ClosedFloatingPointRange<Float>
    get() = flatten().rangeOfOrNull { it.x } ?: 0f..0f

internal fun Iterable<Iterable<ChartEntry>>.calculateStep(): Float {
    var step: Float? = null
    forEach { entryCollection ->
        val iterator = entryCollection.iterator()
        var currentEntry: ChartEntry
        var previousEntry: ChartEntry? = null
        while (iterator.hasNext()) {
            currentEntry = iterator.next()
            previousEntry?.let { prevEntry ->
                val difference = abs(x = currentEntry.x - prevEntry.x)
                step = step?.gcdWith(other = difference) ?: difference
            }
            previousEntry = currentEntry
        }
        if (step == -1f) step = 1f
    }
    return step ?: 1f
}

internal fun Iterable<Iterable<ChartEntry>>.calculateStackedYRange(): ClosedFloatingPointRange<Float> =
    flatten().fold(HashMap<Float, Pair<Float, Float>>()) { map, entry ->
        val (negY, posY) = map.getOrElse(entry.x) { 0f to 0f }
        map[entry.x] = if (entry.y < 0f) negY + entry.y to posY else negY to posY + entry.y
        map
    }.values.rangeOfPairOrNull { it } ?: 0f..0f
