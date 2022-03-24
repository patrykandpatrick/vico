/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package pl.patrykgoworowski.vico.core.entry

import pl.patrykgoworowski.vico.core.axis.model.ChartModel
import pl.patrykgoworowski.vico.core.chart.Chart
import pl.patrykgoworowski.vico.core.chart.column.ColumnChart
import pl.patrykgoworowski.vico.core.entry.composed.ComposedChartEntryModelProducer
import kotlin.math.abs

/**
 * The real source of data used by [Chart] to render itself.
 * The [ChartEntryModel] has data needed for [Chart] rendering pre-calculated.
 *
 * The [Chart] may override [minX], [maxX], [minY] and [maxY] when respectively [Chart.minX],
 * [Chart.maxX], [Chart.minY] and [Chart.maxY] are non-null.
 * Overridden values will be used in [ChartModel].
 *
 * It’s recommended to delegate creation of [ChartEntryModel] to [ChartEntryModelProducer], or
 * [ComposedChartEntryModelProducer].
 *
 * @see [ChartModel]
 * @see [ChartEntryModelProducer]
 * @see [ComposedChartEntryModelProducer].
 */
public interface ChartEntryModel {

    /**
     * The collection of [List] of [ChartEntry].
     * Multiple lists of entries can be rendered by [ColumnChart].
     */
    public val entries: List<List<ChartEntry>>

    /**
     * The minimum x-axis value among all [entries].
     */
    public val minX: Float

    /**
     * The maximum x-axis value among all [entries].
     */
    public val maxX: Float

    /**
     * The minimum y-axis value among all [entries].
     */
    public val minY: Float

    /**
     * The maximum y-axis value among all [entries].
     */
    public val maxY: Float

    /**
     * The maximum y-axis value among all [entries] with the same [ChartEntry.x] value.
     */
    public val stackedMaxY: Float

    /**
     * The value at which [Chart] increments x-axis value between each [ChartEntry].
     */
    public val stepX: Float

    /**
     * Calculates entry count rendered on [Chart].
     */
    public fun getDrawnEntryCount(): Int =
        (((abs(maxX) - abs(minX)) / stepX) + 1).toInt()
}
