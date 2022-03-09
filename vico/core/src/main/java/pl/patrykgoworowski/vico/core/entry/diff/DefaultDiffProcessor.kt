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

package pl.patrykgoworowski.vico.core.entry.diff

import java.util.TreeMap
import java.util.concurrent.locks.ReentrantLock
import pl.patrykgoworowski.vico.core.entry.ChartEntry
import pl.patrykgoworowski.vico.core.entry.calculateStackedYRange
import pl.patrykgoworowski.vico.core.entry.entryOf
import pl.patrykgoworowski.vico.core.entry.yRange
import pl.patrykgoworowski.vico.core.extension.setAll

public class DefaultDiffProcessor : DiffProcessor<ChartEntry> {

    private val setEntriesLock: ReentrantLock = ReentrantLock()
    private val progressMaps = ArrayList<TreeMap<Float, ProgressModel>>()

    private val oldEntries = ArrayList<List<ChartEntry>>()
    private val newEntries = ArrayList<List<ChartEntry>>()

    private var oldYRange = 0f..0f
    private var newYRange = 0f..0f
    private var oldStackedYRange = 0f..0f
    private var newStackedYRange = 0f..0f

    override fun setEntries(
        old: List<List<ChartEntry>>,
        new: List<List<ChartEntry>>,
    ): Unit = synchronized(this) {
        setEntriesLock.lock()
        oldEntries.setAll(old)
        newEntries.setAll(new)
        updateProgressMap()
        updateRanges()
        setEntriesLock.unlock()
    }

    override fun setEntries(new: List<List<ChartEntry>>) {
        setEntries(old = newEntries, new = new)
    }

    override fun progressDiff(progress: Float): List<List<ChartEntry>> = synchronized(this) {
        if (setEntriesLock.isLocked) {
            setEntriesLock.newCondition().await()
        }
        progressMaps.map { map ->
            map.map { (x, model) ->
                entryOf(x, model.progressDiff(progress))
            }
        }
    }

    override fun yRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float> =
        RangeProgressModel(
            oldRange = oldYRange,
            newRange = newYRange,
        ).progressDiff(progress)

    override fun stackedYRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float> =
        RangeProgressModel(
            oldRange = oldStackedYRange,
            newRange = newStackedYRange,
        ).progressDiff(progress)

    private fun updateRanges() {
        oldYRange = oldEntries.yRange
        newYRange = newEntries.yRange
        oldStackedYRange = oldEntries.calculateStackedYRange()
        newStackedYRange = newEntries.calculateStackedYRange()
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

    private data class RangeProgressModel(
        val oldRange: ClosedFloatingPointRange<Float>,
        val newRange: ClosedFloatingPointRange<Float>,
    ) {

        fun progressDiff(progress: Float): ClosedFloatingPointRange<Float> {
            val minValue = ProgressModel(
                oldY = oldRange.start,
                newY = newRange.start,
            ).progressDiff(progress)

            val maxValue = ProgressModel(
                oldY = oldRange.endInclusive,
                newY = newRange.endInclusive,
            ).progressDiff(progress)

            return minValue..maxValue
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
