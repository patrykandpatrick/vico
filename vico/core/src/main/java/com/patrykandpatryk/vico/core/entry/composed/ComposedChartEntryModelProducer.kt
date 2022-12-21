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

package com.patrykandpatryk.vico.core.entry.composed

import com.patrykandpatryk.vico.core.DEF_THREAD_POOL_SIZE
import com.patrykandpatryk.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartModelProducer
import java.util.SortedMap
import java.util.TreeMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A [ChartModelProducer] implementation that generates [ComposedChartEntryModel] instances.
 *
 * @property chartModelProducers the list of [ChartModelProducer]s to be composed by this
 * [ComposedChartEntryModelProducer].
 * @param backgroundExecutor an [Executor] used to generate instances of the [ComposedChartEntryModel] off the main
 * thread.
 *
 * @see ComposedChartEntryModel
 * @see ChartModelProducer
 */
public class ComposedChartEntryModelProducer<Model : ChartEntryModel>(
    public val chartModelProducers: List<ChartModelProducer<Model>>,
    backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
) : ChartModelProducer<ComposedChartEntryModel<Model>> {

    private val compositeModelReceivers: HashMap<Any, CompositeModelReceiver<Model>> = HashMap()

    private val executor: Executor = backgroundExecutor

    private var cachedModel: ComposedChartEntryModel<Model>? = null

    public constructor(
        vararg chartModelProducers: ChartModelProducer<Model>,
        backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
    ) : this(chartModelProducers.toList(), backgroundExecutor)

    override fun getModel(): ComposedChartEntryModel<Model> =
        cachedModel ?: composedChartEntryModelOf(chartModelProducers.map { it.getModel() })
            .also { cachedModel = it }

    override fun progressModel(key: Any, progress: Float) {
        chartModelProducers.forEach { producer ->
            producer.progressModel(key, progress)
        }
    }

    override fun registerForUpdates(
        key: Any,
        updateListener: () -> Unit,
        getOldModel: () -> ComposedChartEntryModel<Model>?,
        onModel: (ComposedChartEntryModel<Model>) -> Unit,
    ) {
        val receiver = CompositeModelReceiver(onModel, executor)
        compositeModelReceivers[key] = receiver
        chartModelProducers.forEachIndexed { index, producer ->
            producer.registerForUpdates(
                key = key,
                updateListener = updateListener,
                getOldModel = { getOldModel()?.composedEntryCollections?.getOrNull(index) },
                onModel = receiver.getModelReceiver(index),
            )
        }
    }

    private class CompositeModelReceiver<Model : ChartEntryModel>(
        private val onModel: (ComposedChartEntryModel<Model>) -> Unit,
        private val executor: Executor,
    ) {

        private val modelReceivers: SortedMap<Int, Model?> = TreeMap()

        internal fun getModelReceiver(index: Int): (Model) -> Unit {
            val modelReceiver: (Model) -> Unit = { model ->
                onModelUpdate(index, model)
            }
            modelReceivers[index] = null
            return modelReceiver
        }

        private fun onModelUpdate(index: Int, model: Model) {
            modelReceivers[index] = model
            val models = modelReceivers.values.mapNotNull { it }
            if (modelReceivers.values.size == models.size) {
                executor.execute {
                    onModel(composedChartEntryModelOf(models))
                }
            }
        }
    }

    override fun unregisterFromUpdates(key: Any) {
        compositeModelReceivers.remove(key)
        chartModelProducers.forEach { producer ->
            producer.unregisterFromUpdates(key)
        }
    }

    override fun isRegistered(key: Any): Boolean = compositeModelReceivers.containsKey(key = key)

    public companion object {

        /**
         * Creates a [ComposedChartEntryModel] instance comprising the provided [Model]s.
         */
        public fun <Model : ChartEntryModel> composedChartEntryModelOf(
            models: List<Model>,
        ): ComposedChartEntryModel<Model> = object : ComposedChartEntryModel<Model> {
            override val composedEntryCollections: List<Model> = models
            override val entries: List<List<ChartEntry>> = models.map { it.entries }.flatten()
            override val minX: Float = models.minOf { it.minX }
            override val maxX: Float = models.maxOf { it.maxX }
            override val minY: Float = models.minOf { it.minY }
            override val maxY: Float = models.maxOf { it.maxY }
            override val stackedPositiveY: Float = models.maxOf { it.stackedPositiveY }
            override val stackedNegativeY: Float = models.minOf { it.stackedNegativeY }
            override val stepX: Float = models.minOf { it.stepX }
            override val id: Int = models.map { it.id }.hashCode()
        }
    }
}
