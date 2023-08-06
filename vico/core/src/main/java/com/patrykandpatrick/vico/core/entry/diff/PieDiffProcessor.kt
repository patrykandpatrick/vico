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

package com.patrykandpatrick.vico.core.entry.diff

import com.patrykandpatrick.vico.core.entry.pie.PieEntry
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.setAll
import java.util.TreeMap

/**
 * The default implementation of [DiffProcessor] for pie charts.
 */
public class PieDiffProcessor : DiffProcessor<PieEntry> {

    private val progressMap = TreeMap<Int, ProgressModel>()
    private val oldEntries = ArrayList<PieEntry>()
    private val newEntries = ArrayList<PieEntry>()

    override fun setEntries(old: List<PieEntry>, new: List<PieEntry>) {
        oldEntries.setAll(old)
        newEntries.setAll(new)
        updateProgressMap()
    }

    private fun updateProgressMap() {
        progressMap.clear()
        oldEntries.forEachIndexed { index, pieEntry ->
            progressMap[index] = ProgressModel(
                oldY = pieEntry.value,
                pieEntry = pieEntry,
            )
        }
        newEntries.forEachIndexed { index, pieEntry ->
            progressMap[index] = ProgressModel(
                oldY = progressMap[index]?.oldY,
                newY = pieEntry.value,
                pieEntry = pieEntry,
            )
        }
    }

    override fun setEntries(new: List<PieEntry>) {
        setEntries(old = newEntries, new = new)
    }

    override fun progressDiff(progress: Float): List<PieEntry> = synchronized(this) {
        progressMap.mapNotNull { (_, model) ->
            if (model.isTemporary && progress == 1f) {
                null
            } else {
                model.progressDiff(progress)
            }
        }
    }

    private class ProgressModel(
        val oldY: Float? = null,
        val newY: Float? = null,
        val pieEntry: PieEntry,
    ) {

        val isTemporary: Boolean
            get() = newY == null

        fun progressDiff(progress: Float): PieEntry {
            val oldY = oldY.orZero
            val newY = newY.orZero

            return pieEntry.withValue(
                value = oldY + (newY - oldY) * progress,
            )
        }
    }
}
