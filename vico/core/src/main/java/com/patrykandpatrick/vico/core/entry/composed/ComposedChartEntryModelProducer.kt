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

package com.patrykandpatrick.vico.core.entry.composed

import com.patrykandpatrick.vico.core.DEF_THREAD_POOL_SIZE
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.calculateStackedYRange
import com.patrykandpatrick.vico.core.entry.calculateXGcd
import com.patrykandpatrick.vico.core.entry.diff.DrawingModelStore
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore
import com.patrykandpatrick.vico.core.entry.xRange
import com.patrykandpatrick.vico.core.entry.yRange
import com.patrykandpatrick.vico.core.extension.gcdWith
import com.patrykandpatrick.vico.core.extension.setAll
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A [ChartModelProducer] implementation that generates [ComposedChartEntryModel] instances.
 *
 * @see ComposedChartEntryModel
 * @see ChartModelProducer
 */
public class ComposedChartEntryModelProducer private constructor(
    private val backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
) : ChartModelProducer<ComposedChartEntryModel<ChartEntryModel>> {

    private var dataSets = mutableListOf<List<List<ChartEntry>>>()
    private var cachedInternalModel: InternalModel? = null
    private val updateReceivers = mutableMapOf<Any, UpdateReceiver>()

    private fun setDataSets(entries: List<List<List<ChartEntry>>>) {
        this.dataSets.setAll(entries)
        cachedInternalModel = null
        updateReceivers.values.forEach { updateReceiver ->
            backgroundExecutor.execute {
                updateReceiver.cancelAnimation()
                updateReceiver.modelTransformer?.prepareForTransformation(
                    oldModel = updateReceiver.getOldModel(),
                    newModel = getModel(),
                    drawingModelStore = updateReceiver.drawingModelStore,
                    chartValuesManager = updateReceiver.getChartValuesManager(getModel()),
                )
                updateReceiver.startAnimation(::progressModel)
            }
        }
    }

    private fun getInternalModel(drawingModelStore: DrawingModelStore = DrawingModelStore.empty): InternalModel {
        cachedInternalModel.let { if (it != null) return it }
        val models = dataSets.map { dataSet ->
            val xRange = dataSet.xRange
            val yRange = dataSet.yRange
            val aggregateYRange = dataSet.calculateStackedYRange()
            object : ChartEntryModel {
                override val entries: List<List<ChartEntry>> = dataSet
                override val minX: Float = xRange.start
                override val maxX: Float = xRange.endInclusive
                override val minY: Float = yRange.start
                override val maxY: Float = yRange.endInclusive
                override val stackedPositiveY: Float = aggregateYRange.endInclusive
                override val stackedNegativeY: Float = aggregateYRange.start
                override val xGcd: Float = dataSet.calculateXGcd()
                override val drawingModelStore: DrawingModelStore = drawingModelStore
            }
        }
        return InternalModel(
            composedEntryCollections = models,
            entries = models.map { it.entries }.flatten(),
            minX = models.minOf { it.minX },
            maxX = models.maxOf { it.maxX },
            minY = models.minOf { it.minY },
            maxY = models.maxOf { it.maxY },
            stackedPositiveY = models.maxOf { it.stackedPositiveY },
            stackedNegativeY = models.minOf { it.stackedNegativeY },
            xGcd = models.fold<ChartEntryModel, Float?>(null) { gcd, model ->
                gcd?.gcdWith(model.xGcd) ?: model.xGcd
            } ?: 1f,
            id = models.map { it.id }.hashCode(),
            drawingModelStore = drawingModelStore,
        )
    }

    override fun getModel(): ComposedChartEntryModel<ChartEntryModel> = getInternalModel()

    override fun progressModel(key: Any, progress: Float) {
        with(updateReceivers[key] ?: return) {
            backgroundExecutor.execute {
                modelTransformer?.transform(drawingModelStore, progress)
                onModelCreated(getInternalModel(drawingModelStore.copy()))
            }
        }
    }

    override fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (progressModel: (chartKey: Any, progress: Float) -> Unit) -> Unit,
        getOldModel: () -> ComposedChartEntryModel<ChartEntryModel>?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        drawingModelStore: MutableDrawingModelStore,
        updateChartValues: (ComposedChartEntryModel<ChartEntryModel>) -> ChartValuesManager,
        onModelCreated: (ComposedChartEntryModel<ChartEntryModel>) -> Unit,
    ) {
        val modelTransformer = modelTransformerProvider?.getModelTransformer<ComposedChartEntryModel<ChartEntryModel>>()
        updateReceivers[key] = UpdateReceiver(
            cancelAnimation,
            startAnimation,
            onModelCreated,
            drawingModelStore,
            modelTransformer,
            getOldModel,
            updateChartValues,
        )
        backgroundExecutor.execute {
            cancelAnimation()
            modelTransformer?.prepareForTransformation(
                oldModel = getOldModel(),
                newModel = getModel(),
                drawingModelStore = drawingModelStore,
                chartValuesManager = updateChartValues(getModel()),
            )
            startAnimation(::progressModel)
        }
    }

    override fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key)

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    /**
     * Creates a [Transaction] instance.
     */
    public fun createTransaction(): Transaction = Transaction()

    /**
     * Creates a [Transaction], runs [block], and calls [Transaction.commit].
     */
    public fun runTransaction(block: Transaction.() -> Unit) {
        createTransaction().also(block).commit()
    }

    /**
     * Handles data updates. An initially empty list of data sets is created and can be updated via the class’s
     * functions. Each data set corresponds to a single nested [Chart].
     */
    public inner class Transaction internal constructor() {
        private val newEntries = mutableListOf<List<List<ChartEntry>>>()

        /**
         * Populates the new list of data sets with the current data sets.
         */
        public fun populate() {
            newEntries.setAll(dataSets)
        }

        /**
         * Replaces the data set at the specified index ([Pair.first]) with the provided data set ([Pair.second]).
         */
        public fun set(pair: Pair<Int, List<List<ChartEntry>>>) {
            set(pair.first, pair.second)
        }

        /**
         * Removes the data set at the specified index.
         */
        public fun removeAt(index: Int) {
            newEntries.removeAt(index)
        }

        /**
         * Replaces the data set at the specified index with the provided data set.
         */
        public fun set(index: Int, dataSet: List<List<ChartEntry>>) {
            newEntries[index] = dataSet
        }

        /**
         * Adds a data set.
         */
        public fun add(dataSet: List<List<ChartEntry>>) {
            newEntries.add(dataSet)
        }

        /**
         * Adds a data set.
         */
        public fun add(index: Int, dataSet: List<List<ChartEntry>>) {
            newEntries.add(index, dataSet)
        }

        /**
         * Adds a data set comprising the provided series.
         */
        public fun add(vararg series: List<ChartEntry>) {
            add(series.toList())
        }

        /**
         * Clears the new list of data sets.
         */
        public fun clear() {
            newEntries.clear()
        }

        /**
         * Finalizes the data update.
         */
        public fun commit() {
            setDataSets(newEntries)
        }
    }

    private data class UpdateReceiver(
        val cancelAnimation: () -> Unit,
        val startAnimation: (progressModel: (chartKey: Any, progress: Float) -> Unit) -> Unit,
        val onModelCreated: (ComposedChartEntryModel<ChartEntryModel>) -> Unit,
        val drawingModelStore: MutableDrawingModelStore,
        val modelTransformer: Chart.ModelTransformer<ComposedChartEntryModel<ChartEntryModel>>?,
        val getOldModel: () -> ComposedChartEntryModel<ChartEntryModel>?,
        val getChartValuesManager: (ComposedChartEntryModel<ChartEntryModel>) -> ChartValuesManager,
    )

    private data class InternalModel(
        override val composedEntryCollections: List<ChartEntryModel>,
        override val entries: List<List<ChartEntry>>,
        override val minX: Float,
        override val maxX: Float,
        override val minY: Float,
        override val maxY: Float,
        override val stackedPositiveY: Float,
        override val stackedNegativeY: Float,
        override val xGcd: Float,
        override val id: Int,
        override val drawingModelStore: DrawingModelStore = DrawingModelStore.empty,
    ) : ComposedChartEntryModel<ChartEntryModel>

    public companion object {
        /**
         * Creates a [ComposedChartEntryModelProducer], running an initial [Transaction]. [backgroundExecutor] is used
         * to run calculations off the main thread.
         */
        public fun build(
            backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
            transaction: Transaction.() -> Unit = {},
        ): ComposedChartEntryModelProducer =
            ComposedChartEntryModelProducer(backgroundExecutor).also { it.runTransaction(transaction) }
    }
}
