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

package com.patrykandpatrick.vico.core.chart

import com.patrykandpatrick.vico.core.entry.ChartEntry

/**
 * For each [ChartEntry] the list, calls the [action] function block with the [ChartEntry] as the block’s argument if
 * [ChartEntry.x] belongs to the provided range.
 */
public inline fun List<ChartEntry>.forEachIn(
    range: ClosedFloatingPointRange<Float>,
    action: (ChartEntry) -> Unit,
) {
    for (entry in this) {
        if (entry.x in range) action(entry)
    }
}

/**
 * For each [ChartEntry] the list, calls the [action] function block with the [ChartEntry] and its index as the block’s
 * arguments if [ChartEntry.x] belongs to the provided range.
 */
public inline fun List<ChartEntry>.forEachInIndexed(
    range: ClosedFloatingPointRange<Float>,
    action: (Int, ChartEntry) -> Unit,
) {
    var index = 0
    forEachIn(range = range) {
        action(index, it)
        index++
    }
}
