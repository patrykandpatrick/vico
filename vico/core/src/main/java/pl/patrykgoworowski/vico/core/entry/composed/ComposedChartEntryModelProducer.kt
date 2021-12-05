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

package pl.patrykgoworowski.vico.core.entry.composed

import pl.patrykgoworowski.vico.core.chart.composed.ComposedChartEntryModel
import pl.patrykgoworowski.vico.core.chart.composed.composedChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartModelProducer
import pl.patrykgoworowski.vico.core.extension.runEach

public class ComposedChartEntryModelProducer<Model : ChartEntryModel>(
    public val chartModelProducers: List<ChartModelProducer<Model>>
) : ChartModelProducer<ComposedChartEntryModel<Model>> {

    override lateinit var model: ComposedChartEntryModel<Model>

    private val listeners = ArrayList<(ComposedChartEntryModel<Model>) -> Unit>()

    private val internalListener = { _: Model ->
        recalculateModel()
        listeners.runEach(model)
    }

    public constructor(
        vararg producers: ChartModelProducer<Model>,
    ) : this(producers.toList())

    init {
        chartModelProducers.forEach { entryCollection ->
            entryCollection.addOnEntriesChangedListener(internalListener)
        }
        recalculateModel()
    }

    private fun recalculateModel() {
        val models = chartModelProducers.map { it.model }

        model = composedChartEntryModel(
            composedEntryCollections = models,
            entryCollections = models.map { it.entries }.flatten(),
            minX = models.minOf { it.minX },
            maxX = models.maxOf { it.maxX },
            minY = models.minOf { it.minY },
            maxY = models.maxOf { it.maxY },
            composedMaxY = models.maxOf { it.composedMaxY },
            step = models.minOf { it.step }
        )
    }

    override fun addOnEntriesChangedListener(listener: (ComposedChartEntryModel<Model>) -> Unit) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: (ComposedChartEntryModel<Model>) -> Unit) {
        listeners -= listener
    }
}
