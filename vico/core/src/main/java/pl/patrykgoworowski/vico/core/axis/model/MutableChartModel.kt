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

package pl.patrykgoworowski.vico.core.axis.model

import pl.patrykgoworowski.vico.core.entry.ChartEntry
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel

/**
 * A subclass of [ChartModel] whose every property is mutable.
 */
public class MutableChartModel(
    override var minX: Float = 0f,
    override var maxX: Float = 0f,
    override var minY: Float = 0f,
    override var maxY: Float = 0f,
    override var chartEntryModel: ChartEntryModel = emptyChartEntryModel(),
) : ChartModel {

    override val stepX: Float
        get() = chartEntryModel.stepX

    /**
     * Sets [minX], [maxX], [minY], and [maxY] to 0.
     */
    public fun clear() {
        minX = 0f
        maxX = 0f
        minY = 0f
        maxY = 0f
        chartEntryModel = emptyChartEntryModel()
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
