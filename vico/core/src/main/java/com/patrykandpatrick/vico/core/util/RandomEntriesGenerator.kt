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

package com.patrykandpatrick.vico.core.util

import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlin.random.Random

/**
 * Generates randomized chart entries.
 * @param xRange the range of _x_ values.
 * @param yRange the range from which _y_ values are randomly selected.
 */
public class RandomEntriesGenerator(
    private val xRange: IntProgression = 0..X_RANGE_TOP,
    private val yRange: IntProgression = 0..Y_RANGE_TOP,
) {
    /**
     * Generates a [List] of [FloatEntry] instances with randomized _y_ values.
     * The size of the [List] is equal to the number of values in [xRange].
     */
    public fun generateRandomEntries(): List<FloatEntry> {
        val result = ArrayList<FloatEntry>()
        val yLength = yRange.last - yRange.first
        for (x in xRange) {
            result += entryOf(x.toFloat(), yRange.first + Random.nextFloat() * yLength)
        }
        return result
    }

    /**
     * Creates a [ChartEntryModel] containing a collection of [FloatEntry] instances with randomized _y_ values.
     * The size of the collection is equal to the number of values in [xRange].
     */
    public fun randomEntryModel(): ChartEntryModel =
        getChartEntryModelProducer().getModel()

    /**
     * Creates a [ComposedChartEntryModel] with three [ChartEntryModelProducer]s, each containing a collection of
     * [FloatEntry] instances with randomized y values. The size of each collection is equal to the number of values in
     * [xRange].
     */
    public fun randomComposedEntryModel(): ComposedChartEntryModel<ChartEntryModel> =
        ComposedChartEntryModelProducer(
            getChartEntryModelProducer(),
            getChartEntryModelProducer(),
            getChartEntryModelProducer(),
        ).getModel()

    private companion object {
        const val X_RANGE_TOP = 10
        const val Y_RANGE_TOP = 20

        fun RandomEntriesGenerator.getChartEntryModelProducer(): ChartModelProducer<ChartEntryModel> =
            ChartEntryModelProducer(listOf(generateRandomEntries(), generateRandomEntries(), generateRandomEntries()))
    }
}
