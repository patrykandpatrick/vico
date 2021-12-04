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

package pl.patrykgoworowski.vico.core.chart.entry.collection

import pl.patrykgoworowski.vico.core.entry.ChartEntry
import kotlin.math.abs

public interface EntryModel {
    public val entryCollections: List<List<ChartEntry>>
    public val minX: Float
    public val maxX: Float
    public val minY: Float
    public val maxY: Float
    public val composedMaxY: Float
    public val step: Float

    public fun getEntriesLength(): Int =
        (((abs(maxX) - abs(minX)) / step) + 1).toInt()
}

public fun entryModel(
    entryCollections: List<List<ChartEntry>> = emptyList(),
    minX: Float = 1f,
    maxX: Float = 1f,
    minY: Float = 1f,
    maxY: Float = 1f,
    composedMaxY: Float = 1f,
    step: Float = 1f,
): EntryModel = object : EntryModel {
    override val entryCollections: List<List<ChartEntry>> = entryCollections
    override val minX: Float = minX
    override val maxX: Float = maxX
    override val minY: Float = minY
    override val maxY: Float = maxY
    override val composedMaxY: Float = composedMaxY
    override val step: Float = step
}
