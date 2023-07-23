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

package com.patrykandpatrick.vico.core.entry

import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer

/**
 * Contains the data for a [Chart]. Pre-calculates values needed for the rendering of the [Chart].
 *
 * The [Chart] may override [minX], [maxX], [minY], or [maxY] via [AxisValuesOverrider]. These overrides will be used
 * in the [Chart]’s [ChartValues] instance.
 *
 * It’s recommended to delegate the creation of [EntryModel] to [ChartEntryModelProducer] or
 * [ComposedChartEntryModelProducer].
 *
 * @see [ChartValues]
 * @see [ChartEntryModelProducer]
 * @see [ComposedChartEntryModelProducer].
 */
public interface EntryModel<Entries> {

    /**
     * The [EntryModel]’s identifier. Different [EntryModel] instances don’t necessarily have different
     * identifiers. [ChartEntryModelProducer] and [ComposedChartEntryModelProducer] use the same [id] for all
     * [EntryModel] instances created for the purpose of running a single difference animation. This enables
     * charts to differentiate between data set changes and difference animations.
     */
    public val id: Int
        get() = entries.hashCode()

    /**
     * The chart entries ([ChartEntry] instances). Multiple lists of [ChartEntry] instances can be provided. In such a
     * case, entries will be associated by index, and the [Chart] will stack or group them if it’s a [ColumnChart],
     * and display multiple lines if it’s a [LineChart].
     */
    public val entries: List<Entries>

    /**
     * The minimum x-axis value from among all [entries].
     */
    public val minX: Float

    /**
     * The maximum x-axis value from among all [entries].
     */
    public val maxX: Float

    /**
     * The minimum y-axis value from among all [entries].
     */
    public val minY: Float

    /**
     * The maximum y-axis value from among all [entries].
     */
    public val maxY: Float

    /**
     * The greatest common divisor of the _x_ values.
     */
    public val xGcd: Float
}
