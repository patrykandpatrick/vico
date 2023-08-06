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

package com.patrykandpatrick.vico.core.util

import com.patrykandpatrick.vico.core.entry.pie.FloatPieEntry
import com.patrykandpatrick.vico.core.entry.pie.PieEntry

private const val SLICE_COUNT_RANGE_START = 3
private const val SLICE_COUNT_RANGE_END = 8
private const val SLICE_VALUE_RANGE_START = 1
private const val SLICE_VALUE_RANGE_END = 4

/**
 * A utility class for generating random [PieEntry] lists.
 *
 * @param sliceCount the range of the number of slices in the generated list.
 * @param sliceValue the range of the value of each slice in the generated list.
 */
public class RandomPieEntriesGenerator(
    private val sliceCount: IntRange = SLICE_COUNT_RANGE_START..SLICE_COUNT_RANGE_END,
    private val sliceValue: IntRange = SLICE_VALUE_RANGE_START..SLICE_VALUE_RANGE_END,
) {

    /**
     * Generates random [PieEntry] list.
     */
    public fun get(): List<PieEntry> = buildList {
        repeat(sliceCount.random()) {
            add(FloatPieEntry(sliceValue.random().toFloat()))
        }
    }
}
