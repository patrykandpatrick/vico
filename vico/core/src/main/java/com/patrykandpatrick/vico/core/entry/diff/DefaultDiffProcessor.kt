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

import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.calculateStackedYRange
import com.patrykandpatrick.vico.core.entry.yRange
import com.patrykandpatrick.vico.core.extension.doubled
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
    private var oldAggregateYRange = 0f..0f
    private var newAggregateYRange = 0f..0f

    override fun setEntries(
        old: List<List<ChartEntry>>,
        new: List<List<ChartEntry>>,
        oldYRange: ClosedFloatingPointRange<Float>,
        oldAggregateYRange: ClosedFloatingPointRange<Float>,
    ): Unit = synchronized(this) {
        oldEntries.setToAllChildren(old)
        newEntries.setToAllChildren(new)
        updateProgressMap()
        updateRanges(oldYRange, oldAggregateYRange)
    }

    override fun setEntries(new: List<List<ChartEntry>>) {
        setEntries(old = newEntries, new = new, oldYRange = newYRange, oldAggregateYRange = newAggregateYRange)
    }

    override fun progressDiff(progress: Float): List<List<ChartEntry>> = synchronized(this) {
        progressMaps.mapNotNull { map ->
            map.mapNotNull { (_, model) ->
                when {
                    oldYRange == 0f..0f -> model.transformFromZero(progress.coerceAtMost(.5f).doubled)

                    newYRange == 0f..0f ->
                        if (progress >= .5f) null else model.transformToZero(progress.coerceAtMost(.5f).doubled)

                    progress >= .5f -> if (model.removed) null else model.transformFromZero((progress - .5f).doubled)
                    else -> if (model.added) null else model.transformToZero(progress.doubled)
                }
            }.takeIf { list -> list.isNotEmpty() }
        }
    }

    override fun yRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float> =
        if (progress >= .5f || oldYRange == 0f..0f) newYRange else oldYRange

    override fun stackedYRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float> =
        if (progress >= .5f || oldYRange == 0f..0f) newAggregateYRange else oldAggregateYRange

    private fun updateRanges(
        oldYRange: ClosedFloatingPointRange<Float>,
        oldAggregateYRange: ClosedFloatingPointRange<Float>,
    ) {
        this.oldYRange = oldYRange
        this.oldAggregateYRange = oldAggregateYRange
        newYRange = newEntries.yRange
        newAggregateYRange = newEntries.calculateStackedYRange()
    }

    private fun updateProgressMap() {
        progressMaps.clear()
        val maxListSize = maxOf(oldEntries.size, newEntries.size)
        for (i in 0..<maxListSize) {
            val map = TreeMap<Float, ChartEntryProgressModel>()
            oldEntries
                .getOrNull(i)
                ?.forEach { chartEntry ->
                    map[chartEntry.x] = ChartEntryProgressModel(
                        oldY = chartEntry.y,
                        chartEntry = chartEntry,
                        added = false,
                    )
                }
            newEntries
                .getOrNull(i)
                ?.forEach { chartEntry ->
                    val current = map[chartEntry.x]
                    map[chartEntry.x] = ChartEntryProgressModel(
                        oldY = current?.oldY,
                        newY = chartEntry.y,
                        removed = false,
                        added = current?.added != false,
                        chartEntry = chartEntry,
                    )
                }
            progressMaps.add(map)
        }
    }

    private data class ChartEntryProgressModel(
        val oldY: Float? = null,
        val newY: Float? = null,
        val removed: Boolean = true,
        val added: Boolean = true,
        val chartEntry: ChartEntry,
    ) {
        fun transformToZero(fraction: Float) = chartEntry.withY(oldY.orZero * (1 - fraction))

        fun transformFromZero(fraction: Float) = chartEntry.withY(newY.orZero * fraction)
    }
}
