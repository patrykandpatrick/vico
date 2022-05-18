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

package com.patrykandpatryk.vico.core.chart.values

import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel

/**
 * A subclass of [ChartValues] whose every property is mutable.
 */
public class MutableChartValues : ChartValues {

    override var minX: Float = 0f
        private set

    override var maxX: Float = 0f
        private set

    override var minY: Float = 0f
        private set

    override var maxY: Float = 0f
        private set

    override var chartEntryModel: ChartEntryModel = emptyChartEntryModel()

    override val stepX: Float
        get() = chartEntryModel.stepX

    /**
     * Returns `true` if all values have been set and at least one call to [tryUpdate] or [set] has been made.
     */
    public var hasValuesSet: Boolean = false
        private set

    /**
     * Attempts to update the stored values to the provided params.
     * [minX] and [minY] can be updated to a smaller value.
     * [maxX] and [maxY] can be updated to a higher value.
     * The [chartEntryModel] is always be updated.
     */
    public fun tryUpdate(
        minX: Float = this.minX,
        maxX: Float = this.maxX,
        minY: Float = this.minY,
        maxY: Float = this.maxY,
        chartEntryModel: ChartEntryModel = this.chartEntryModel,
    ): MutableChartValues = apply {
        this.minX = if (hasValuesSet) minOf(this.minX, minX) else minX
        this.maxX = if (hasValuesSet) maxOf(this.maxX, maxX) else maxX
        this.minY = if (hasValuesSet) minOf(this.minY, minY) else minY
        this.maxY = if (hasValuesSet) maxOf(this.maxY, maxY) else maxY
        this.chartEntryModel = chartEntryModel
        hasValuesSet = true
    }

    /**
     * Sets [minX], [maxX], [minY], and [maxY] to 0.
     */
    public fun reset() {
        minX = 0f
        maxX = 0f
        minY = 0f
        maxY = 0f
        chartEntryModel = emptyChartEntryModel()
        hasValuesSet = false
    }

    private companion object {

        fun emptyChartEntryModel(): ChartEntryModel = object : ChartEntryModel {
            override val entries: List<List<ChartEntry>> = emptyList()
            override val minX: Float = 0f
            override val maxX: Float = 0f
            override val minY: Float = 0f
            override val maxY: Float = 0f
            override val stackedMaxY: Float = 0f
            override val stepX: Float = 1f
        }
    }
}
