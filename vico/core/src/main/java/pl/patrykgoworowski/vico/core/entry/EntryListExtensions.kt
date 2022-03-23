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

package pl.patrykgoworowski.vico.core.entry

/**
 * Creates a [ChartEntryModel] out of the given pairs of numbers, treating the first number in each pair as the x value,
 * and the second one as the y value.
 */
public fun entryModelOf(vararg entries: Pair<Number, Number>): ChartEntryModel =
    entries
        .map { (x, y) -> entryOf(x.toFloat(), y.toFloat()) }
        .let { entryList -> ChartEntryModelProducer(listOf(entryList)) }
        .getModel()

/**
 * Creates a [ChartEntryModel] out of the provided array of numbers, treating each number’s index as the x value, and
 * the number itself as the y value.
 */
public fun entryModelOf(vararg values: Number): ChartEntryModel =
    values
        .mapIndexed { index, value -> entryOf(index.toFloat(), value.toFloat()) }
        .let { entryList -> ChartEntryModelProducer(listOf(entryList)) }
        .getModel()
