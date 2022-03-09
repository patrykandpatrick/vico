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

package pl.patrykgoworowski.vico.core.chart.composed

import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartEntry

public interface ComposedChartEntryModel<Model : ChartEntryModel> : ChartEntryModel {
    public val composedEntryCollections: List<Model>
}

public fun <Model : ChartEntryModel> composedChartEntryModel(
    composedEntryCollections: List<Model> = emptyList(),
    entryCollections: List<List<ChartEntry>> = emptyList(),
    minX: Float = 1f,
    maxX: Float = 1f,
    minY: Float = 1f,
    maxY: Float = 1f,
    composedMaxY: Float = 1f,
    step: Float = 1f,
): ComposedChartEntryModel<Model> = object : ComposedChartEntryModel<Model> {
    override val composedEntryCollections: List<Model> = composedEntryCollections
    override val entries: List<List<ChartEntry>> = entryCollections
    override val minX: Float = minX
    override val maxX: Float = maxX
    override val minY: Float = minY
    override val maxY: Float = maxY
    override val stackedMaxY: Float = composedMaxY
    override val stepX: Float = step
}
