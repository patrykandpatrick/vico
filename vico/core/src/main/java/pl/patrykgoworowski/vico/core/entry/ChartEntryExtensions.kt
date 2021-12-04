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

package pl.patrykgoworowski.vico.core.entry

/**
 * Conveniently creates an instance of [FloatEntry].
 * @param [x] Position on x-axis of this [FloatEntry].
 * @param [y] Position on y-axis of this [FloatEntry].
 * @return [FloatEntry] with [x] and [y] values.
 *
 * @see [entriesOf]
 */
public fun entryOf(x: Float, y: Float): FloatEntry = FloatEntry(x, y)

/**
 * Conveniently creates an instance of [FloatEntry] out of any [Number].
 * @param [x] Position on x-axis of this [FloatEntry]. Will be converted to [Float].
 * @param [y] Position on y-axis of this [FloatEntry]. Will be converted to [Float].
 * @return [FloatEntry] with [x] and [y] values converted to [Float].
 *
 * @see [entriesOf]
 */
public fun entryOf(x: Number, y: Number): FloatEntry = entryOf(x.toFloat(), y.toFloat())

/**
 * Conveniently creates a [List] of [FloatEntry] out of [Pair] of [Number].
 * [Pair.first] is mapped to x-axis value, and [Pair.second] is mapped to y-axis value.
 *
 * For example:
 *
 * ```
 * entriesOf(0 to 1, 1 to 2, 3 to 5)
 * ```
 *
 * All [Number] instances are converted to [Float].
 * @see [entryOf]
 */
public fun entriesOf(vararg pairs: Pair<Number, Number>): List<FloatEntry> =
    pairs.map { (x, y) -> entryOf(x, y) }

/**
 * Conveniently creates a [List] of [FloatEntry] out of array of y-axis values.
 *
 * For example:
 *
 * ```
 * entriesOf(1, 2, 5)
 * ```
 * is the same as:
 *
 * ```
 * entriesOf(0 to 1, 1 to 2, 2 to 5)
 * ```
 *
 * Each y-axis value from [yValues] will have a x-axis value assigned.
 * X-axis value will be equal to index of y-axis value in the [yValues].
 *
 * All [Number] instances are converted to [Float].
 * @see [entryOf]
 */
public fun entriesOf(vararg yValues: Number): List<FloatEntry> =
    yValues.mapIndexed { index, y -> entryOf(index, y) }
