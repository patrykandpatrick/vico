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

package com.patrykandpatrick.vico.core.entry.diff

import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.calculateStackedYRange
import com.patrykandpatrick.vico.core.entry.yRange
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.setToAllChildren
import java.util.TreeMap

/**
 * The default implementation of [DiffProcessor].
 */
public class DefaultDiffProcessor : DiffProcessor<ChartEntry> {

    private val progressMaps = ArrayList<TreeMap<Float, ChartEntryProgressModel>>()

    private val oldEntries = ArrayList<ArrayList<ChartEntry>>()
    private val newEntries = ArrayList<ArrayList<ChartEntry>>()

    private var oldYRange = 0f..0f
    private var newYRange = 0f..0f
    private var oldStackedYRange = 0f..0f
    private var newStackedYRange = 0f..0f

    override fun setEntries(
        old: List<List<ChartEntry>>,
        new: List<List<ChartEntry>>,
    ): Unit = synchronized(this) {
        oldEntries.setToAllChildren(old)
        newEntries.setToAllChildren(new)
        updateProgressMap()
        updateRanges()
    }

    override fun setEntries(new: List<List<ChartEntry>>) {
        setEntries(old = newEntries, new = new)
    }

    override fun progressDiff(progress: Float): List<List<ChartEntry>> = synchronized(this) {
        progressMaps.mapNotNull { map ->
            map.mapNotNull { (_, model) ->
                if (model.temporary && progress == 1f) null else model.progressDiff(progress)
            }.takeIf { list -> list.isNotEmpty() }
        }
    }

    override fun yRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float> = when {
        oldYRange == ZERO_TO_ZERO -> newYRange
        newYRange == ZERO_TO_ZERO -> if (progress == 1f) newYRange else oldYRange
        else -> RangeProgressModel(
            oldRange = oldYRange,
            newRange = newYRange,
        ).progressDiff(progress)
    }

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
            val map = TreeMap<Float, ChartEntryProgressModel>()
            oldEntries
                .getOrNull(i)
                ?.forEach { chartEntry ->
                    map[chartEntry.x] = ChartEntryProgressModel(
                        oldY = chartEntry.y,
                        chartEntry = chartEntry,
                    )
                }
            newEntries
                .getOrNull(i)
                ?.forEach { chartEntry ->
                    map[chartEntry.x] = ChartEntryProgressModel(
                        oldY = map[chartEntry.x]?.oldY,
                        newY = chartEntry.y,
                        temporary = false,
                        chartEntry = chartEntry,
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

    private data class ChartEntryProgressModel(
        val oldY: Float? = null,
        val newY: Float? = null,
        val temporary: Boolean = true,
        val chartEntry: ChartEntry,
    ) {
        fun progressDiff(progress: Float): ChartEntry = chartEntry.withY(
            y = ProgressModel(
                oldY = oldY,
                newY = newY,
            ).progressDiff(progress = progress),
        )
    }

    private data class ProgressModel(
        val oldY: Float? = null,
        val newY: Float? = null,
    ) {
        fun progressDiff(progress: Float): Float {
            val oldY = oldY.orZero
            val newY = newY.orZero
            return oldY + (newY - oldY) * progress
        }
    }

    private companion object {
        val ZERO_TO_ZERO = 0f..0f
    }
}
