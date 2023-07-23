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

package com.patrykandpatrick.vico.core.candlestickentry.diff

import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntry
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickEntryType
import com.patrykandpatrick.vico.core.candlestickentry.CandlestickTypedEntry
import com.patrykandpatrick.vico.core.candlestickentry.yRange
import com.patrykandpatrick.vico.core.chart.candlestick.CandlestickChartType
import com.patrykandpatrick.vico.core.entry.diff.DiffProcessor
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.progressValues
import com.patrykandpatrick.vico.core.extension.setAll
import java.util.TreeMap

/**
 * The default implementation of [DiffProcessor].
 */
public class DefaultCandlestickDiffProcessor : CandlestickDiffProcessor<CandlestickEntry, CandlestickTypedEntry> {

    private val progressMap = TreeMap<Float, EntryProgressModel>()

    private val oldEntries = ArrayList<CandlestickEntry>()
    private val newEntries = ArrayList<CandlestickEntry>()

    private var oldYRange = 0f..0f
    private var newYRange = 0f..0f

    override fun <E1 : CandlestickEntry, E2 : CandlestickEntry> setEntries(old: List<E1>, new: List<E2>) {
        synchronized(this) {
            oldEntries.setAll(old)
            newEntries.setAll(new)
            updateProgressMap()
            updateRanges()
        }
    }

    override fun setEntries(new: List<CandlestickEntry>) {
        setEntries(old = newEntries, new = new)
    }

    override fun getTypedEntries(
        entries: List<CandlestickEntry>,
        candlestickChartType: CandlestickChartType,
    ): List<CandlestickTypedEntry> {
        var previousClose: Float? = null

        return entries.map { entry ->
            entry.withValuesAndType(
                low = entry.low,
                high = entry.high,
                open = entry.open,
                close = entry.close,
                type = getType(
                    candlestickChartType = candlestickChartType,
                    previousClose = previousClose,
                    currentClose = entry.close,
                    currentOpen = entry.open,
                ),
            ).also { typedEntry ->
                previousClose = typedEntry.close
            }
        }
    }

    override fun progressDiff(
        progress: Float,
        candlestickChartType: CandlestickChartType,
    ): List<CandlestickTypedEntry> = synchronized(this) {
        var previousClose: Float? = null

        progressMap.mapNotNull { (_, model) ->
            if (model.temporary && progress == 1f) {
                null
            } else {
                model.progressDiff(
                    progress = progress,
                    candlestickChartType = candlestickChartType,
                    previousClose = previousClose,
                ).also { entry ->
                    previousClose = entry.close
                }
            }
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

    private fun updateRanges() {
        oldYRange = oldEntries.yRange
        newYRange = newEntries.yRange
    }

    private fun updateProgressMap() {
        progressMap.clear()
        val map = TreeMap<Float, EntryProgressModel>()
        oldEntries
            .forEach { entry ->
                map[entry.x] = EntryProgressModel(
                    oldLow = entry.low,
                    oldHigh = entry.high,
                    oldOpen = entry.open,
                    oldClose = entry.close,
                    entry = entry,
                )
            }
        newEntries
            .forEach { entry ->
                val oldModel = map[entry.x]
                map[entry.x] = EntryProgressModel(
                    oldLow = oldModel?.oldLow,
                    oldHigh = oldModel?.oldHigh,
                    oldOpen = oldModel?.oldOpen,
                    oldClose = oldModel?.oldClose,
                    newLow = entry.low,
                    newHigh = entry.high,
                    newOpen = entry.open,
                    newClose = entry.close,
                    entry = entry,
                    temporary = false,
                )
            }
        progressMap.putAll(map)
    }

    private data class RangeProgressModel(
        val oldRange: ClosedFloatingPointRange<Float>,
        val newRange: ClosedFloatingPointRange<Float>,
    ) {
        fun progressDiff(progress: Float): ClosedFloatingPointRange<Float> {
            val minValue = progressValues(
                start = oldRange.start,
                end = newRange.start,
                progress = progress,
            )

            val maxValue = progressValues(
                start = oldRange.endInclusive,
                end = newRange.endInclusive,
                progress = progress,
            )

            return minValue..maxValue
        }
    }

    private data class EntryProgressModel(
        val newLow: Float? = null,
        val newHigh: Float? = null,
        val newOpen: Float? = null,
        val newClose: Float? = null,
        val oldLow: Float? = null,
        val oldHigh: Float? = null,
        val oldOpen: Float? = null,
        val oldClose: Float? = null,
        val temporary: Boolean = true,
        val entry: CandlestickEntry,
    ) {
        fun progressDiff(
            progress: Float,
            candlestickChartType: CandlestickChartType,
            previousClose: Float?,
        ): CandlestickTypedEntry = entry.withValuesAndType(
            low = progressValues(
                start = oldLow.orZero,
                end = newLow.orZero,
                progress = progress,
            ),
            high = progressValues(
                start = oldHigh.orZero,
                end = newHigh.orZero,
                progress = progress,
            ),
            open = progressValues(
                start = oldOpen.orZero,
                end = newOpen.orZero,
                progress = progress,
            ),
            close = progressValues(
                start = oldClose.orZero,
                end = newClose.orZero,
                progress = progress,
            ),
            type = getType(
                candlestickChartType = candlestickChartType,
                previousClose = previousClose,
                currentClose = newClose.orZero,
                currentOpen = newOpen.orZero,
            ),
        )
    }

    private companion object {

        val ZERO_TO_ZERO = 0f..0f

        private fun getType(
            candlestickChartType: CandlestickChartType,
            previousClose: Float?,
            currentClose: Float,
            currentOpen: Float,
        ): CandlestickEntryType = when (candlestickChartType) {
            CandlestickChartType.Standard -> CandlestickEntryType.standard(currentOpen, currentClose)
            CandlestickChartType.Hollow -> CandlestickEntryType.hollow(previousClose, currentClose, currentOpen)
        }
    }
}
