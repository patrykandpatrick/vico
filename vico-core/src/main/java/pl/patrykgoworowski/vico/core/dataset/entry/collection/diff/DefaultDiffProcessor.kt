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

package pl.patrykgoworowski.vico.core.dataset.entry.collection.diff

import pl.patrykgoworowski.vico.core.entry.DataEntry
import pl.patrykgoworowski.vico.core.entry.component1
import pl.patrykgoworowski.vico.core.entry.component2
import pl.patrykgoworowski.vico.core.entry.entryOf
import pl.patrykgoworowski.vico.core.extension.setAll
import java.util.TreeMap

public class DefaultDiffProcessor : DiffProcessor<DataEntry> {

    private val progressMaps = ArrayList<TreeMap<Float, ProgressModel>>()

    private val oldEntries = ArrayList<List<DataEntry>>()
    private val newEntries = ArrayList<List<DataEntry>>()

    override fun setEntries(old: List<List<DataEntry>>, new: List<List<DataEntry>>) {
        oldEntries.setAll(old)
        newEntries.setAll(new)
        updateProgressMap()
    }

    override fun setEntries(new: List<List<DataEntry>>) {
        oldEntries.setAll(newEntries)
        newEntries.setAll(new)
        updateProgressMap()
    }

    override fun progressDiff(progress: Float): List<List<DataEntry>> =
        progressMaps
            .map { map ->
                map.map { (x, model) -> entryOf(x, model.progressDiff(progress)) }
            }

    private fun updateProgressMap() {
        progressMaps.clear()
        val maxListSize = maxOf(oldEntries.size, newEntries.size)
        for (i in 0 until maxListSize) {
            val map = TreeMap<Float, ProgressModel>()
            oldEntries
                .getOrNull(i)
                ?.forEach { (x, y) ->
                    map[x] = ProgressModel(oldY = y)
                }
            newEntries
                .getOrNull(i)
                ?.forEach { (x, y) ->
                    map[x] = ProgressModel(
                        oldY = map[x]?.oldY,
                        newY = y,
                    )
                }
            progressMaps.add(map)
        }
    }

    private data class ProgressModel(
        val oldY: Float? = null,
        val newY: Float? = null,
    ) {

        fun progressDiff(progress: Float): Float {
            val oldY = oldY ?: 0f
            val newY = newY ?: 0f
            return oldY + (newY - oldY) * progress
        }
    }
}
