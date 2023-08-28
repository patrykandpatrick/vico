/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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
 * For each [ChartEntry] in the list such that [ChartEntry.x] belongs to the provided range, calls the [action] function
 * block with the [ChartEntry] as the block’s argument.
 */
public inline fun List<ChartEntry>.forEachIn(range: ClosedFloatingPointRange<Float>, action: (ChartEntry) -> Unit) {
    forEach { if (it.x in range) action(it) }
}

/**
 * For each [ChartEntry] in the list such that [ChartEntry.x] belongs to the provided range, calls the [action] function
 * block with the [ChartEntry] and its index in the list as the block’s arguments.
 */
public inline fun List<ChartEntry>.forEachInAbsolutelyIndexed(
    range: ClosedFloatingPointRange<Float>,
    action: (Int, ChartEntry) -> Unit,
) {
    var index = 0
    forEach { entry ->
        if (entry.x in range) action(index, entry)
        index++
    }
}

/**
 * For each [ChartEntry] in the list such that [ChartEntry.x] belongs to the provided range, calls the [action] function
 * block with the [ChartEntry], its index in the list, and the next [ChartEntry] in the filtered list as the block’s
 * arguments.
 */
public inline fun List<ChartEntry>.forEachInAbsolutelyIndexed(
    range: ClosedFloatingPointRange<Float>,
    action: (Int, ChartEntry, ChartEntry?) -> Unit,
) {
    forEachInAbsolutelyIndexed(range) { index, entry ->
        action(index, entry, getOrNull(index + 1)?.takeIf { it.x in range })
    }
}

/**
 * For each [ChartEntry] in the list such that [ChartEntry.x] belongs to the provided range, calls the [action] function
 * block with the [ChartEntry] and its index in the filtered list as the block’s arguments.
 */
public inline fun List<ChartEntry>.forEachInRelativelyIndexed(
    range: ClosedFloatingPointRange<Float>,
    action: (Int, ChartEntry) -> Unit,
) {
    var index = 0
    forEachIn(range) { action(index++, it) }
}

/**
 * For each [ChartEntry] in the list such that [ChartEntry.x] belongs to the provided range, calls the [action] function
 * block with the [ChartEntry], its index in the filtered list, and the next [ChartEntry] in the filtered list as the
 * block’s arguments.
 */
public inline fun List<ChartEntry>.forEachInRelativelyIndexed(
    range: ClosedFloatingPointRange<Float>,
    action: (Int, ChartEntry, ChartEntry?) -> Unit,
) {
    var relativeIndex = 0
    forEachInAbsolutelyIndexed(range) { absoluteIndex, entry ->
        action(relativeIndex++, entry, getOrNull(absoluteIndex + 1)?.takeIf { it.x in range })
    }
}
