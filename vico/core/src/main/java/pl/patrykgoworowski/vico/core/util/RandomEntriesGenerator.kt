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

package pl.patrykgoworowski.vico.core.util

import pl.patrykgoworowski.vico.core.chart.composed.ComposedChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartEntryModelProducer
import pl.patrykgoworowski.vico.core.entry.FloatEntry
import pl.patrykgoworowski.vico.core.entry.composed.ComposedChartEntryModelProducer
import pl.patrykgoworowski.vico.core.entry.entryOf

/**
 * Generates randomized chart entries.
 * @param xRange the range of x values.
 * @param yRange the range from which y values are randomly selected.
 */
public class RandomEntriesGenerator(
    private val xRange: IntRange = 0..X_RANGE_TOP,
    private val yRange: IntRange = 0..Y_RANGE_TOP
) {
    /**
     * Generates a [List] of [FloatEntry] instances with randomized y values.
     * The size of the [List] is equal to the number of values in [xRange].
     */
    public fun generateRandomEntries(): List<FloatEntry> {
        val result = ArrayList<FloatEntry>()
        val yLength = yRange.last - yRange.first
        for (x in xRange) {
            result += entryOf(x.toFloat(), yRange.first + (Math.random() * yLength).toFloat())
        }
        return result
    }

    /**
     * Creates a [ChartEntryModel] containing a collection of [FloatEntry] instances with randomized y values.
     * The size of the collection is equal to the number of values in [xRange].
     */
    public fun randomEntryModel(): ChartEntryModel =
        ChartEntryModelProducer(generateRandomEntries()).getModel()

    /**
     * Creates a [ComposedChartEntryModel] with three [ChartEntryModelProducer]s, each containing a collection of
     * [FloatEntry] instances with randomized y values. The size of each collection is equal to the number of values in
     * [xRange].
     */
    public fun randomComposedEntryModel(): ComposedChartEntryModel<ChartEntryModel> =
        ComposedChartEntryModelProducer(
            ChartEntryModelProducer(generateRandomEntries()),
            ChartEntryModelProducer(generateRandomEntries()),
            ChartEntryModelProducer(generateRandomEntries()),
        ).getModel()

    private companion object {
        const val X_RANGE_TOP = 10
        const val Y_RANGE_TOP = 20
    }
}
