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

package pl.patrykgoworowski.vico.core.dataset.entry.collection

import pl.patrykgoworowski.vico.core.entry.DataEntry
import kotlin.math.abs

interface EntryModel {
    val entryCollections: List<List<DataEntry>>
    val minX: Float
    val maxX: Float
    val minY: Float
    val maxY: Float
    val composedMaxY: Float
    val step: Float

    fun getEntriesLength(): Int =
        (((abs(maxX) - abs(minX)) / step) + 1).toInt()
}

fun entryModel(
    entryCollections: List<List<DataEntry>> = emptyList(),
    minX: Float = 1f,
    maxX: Float = 1f,
    minY: Float = 1f,
    maxY: Float = 1f,
    composedMaxY: Float = 1f,
    step: Float = 1f,
) = object : EntryModel {
    override val entryCollections: List<List<DataEntry>> = entryCollections
    override val minX: Float = minX
    override val maxX: Float = maxX
    override val minY: Float = minY
    override val maxY: Float = maxY
    override val composedMaxY: Float = composedMaxY
    override val step: Float = step
}
