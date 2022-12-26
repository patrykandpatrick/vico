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

import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.line.LineChart

/**
 * Creates a [ChartEntryModel] out of the given pairs of numbers, treating the first number in each pair as the _x_
 * value, and the second one as the _y_ value.
 */
public fun entryModelOf(vararg entries: Pair<Number, Number>): ChartEntryModel =
    entries
        .map { (x, y) -> entryOf(x.toFloat(), y.toFloat()) }
        .let { entryList -> ChartEntryModelProducer(listOf(entryList)) }
        .getModel()

/**
 * Creates a [ChartEntryModel] out of the provided array of numbers, treating each number’s index as the _x_ value, and
 * the number itself as the _y_ value.
 */
public fun entryModelOf(vararg values: Number): ChartEntryModel =
    values
        .mapIndexed { index, value -> entryOf(index.toFloat(), value.toFloat()) }
        .let { entryList -> ChartEntryModelProducer(listOf(entryList)) }
        .getModel()

/**
 * Creates a [ChartEntryModel] out of the provided list of list of [FloatEntry] instances.
 *
 * This can be used to create [LineChart]s with multiple lines and [ColumnChart]s with multi-column segments.
 */
public fun entryModelOf(vararg values: List<FloatEntry>): ChartEntryModel =
    ChartEntryModelProducer(values.toList()).getModel()
