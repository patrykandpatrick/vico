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
import com.patrykandpatrick.vico.core.chart.candlestick.CandlestickChartType

/**
 * Processes the difference between two collections of [CandlestickEntry] instances and generates intermediate
 * collections for use in difference animations.
 */
public interface CandlestickDiffProcessor<InputEntry : CandlestickEntry, OutputEntry : CandlestickEntry> {

    /**
     * Sets the initial and target collections of [CandlestickEntry] instances.
     * @param old the initial collection.
     * @param new the target collection.
     */
    public fun <E1 : InputEntry, E2 : InputEntry> setEntries(old: List<E1>, new: List<E2>)

    /**
     * Reuses the current target collection of [CandlestickEntry] instances as the initial collection and sets
     * a new target collection.
     * @param new the target collection.
     */
    public fun setEntries(new: List<InputEntry>)

    /**
     * TODO
     */
    public fun getTypedEntries(entries: List<InputEntry>, candlestickChartType: CandlestickChartType): List<OutputEntry>

    /**
     * Creates an intermediate collection of [CandlestickEntry] instances for use in difference animations.
     * @param progress the balance between the initial and target collections. A value of `0f` yields the initial
     * @param candlestickChartType TODO
     * collection, and a value of `1f` yields the target collection.
     */
    public fun progressDiff(progress: Float, candlestickChartType: CandlestickChartType): List<OutputEntry>

    /**
     * Creates an intermediate y-value range for use in difference animations.
     * @param progress the balance between the initial and target range. A value of `Of` yields the initial range, and
     * a value of `1f` yields the target range.
     */
    public fun yRangeProgressDiff(progress: Float): ClosedFloatingPointRange<Float>
}
