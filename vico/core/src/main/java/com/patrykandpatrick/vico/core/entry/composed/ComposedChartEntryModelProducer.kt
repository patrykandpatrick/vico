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

import androidx.annotation.WorkerThread
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.calculateStackedYRange
import com.patrykandpatrick.vico.core.entry.calculateXGcd
import com.patrykandpatrick.vico.core.entry.diff.DrawingModelStore
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore
import com.patrykandpatrick.vico.core.entry.xRange
import com.patrykandpatrick.vico.core.entry.yRange
import com.patrykandpatrick.vico.core.extension.copy
import com.patrykandpatrick.vico.core.extension.gcdWith
import com.patrykandpatrick.vico.core.extension.setAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * A [ChartModelProducer] implementation that generates [ComposedChartEntryModel] instances.
 *
 * @see ComposedChartEntryModel
 * @see ChartModelProducer
 */
public class ComposedChartEntryModelProducer private constructor(dispatcher: CoroutineDispatcher) :
    ChartModelProducer<ComposedChartEntryModel<ChartEntryModel>> {

    private var dataSets = emptyList<List<List<ChartEntry>>>()
    private var cachedInternalComposedModel: InternalComposedModel? = null
    private val mutex = Mutex()
    private val coroutineScope = CoroutineScope(dispatcher)
    private val updateReceivers = mutableMapOf<Any, UpdateReceiver>()

    private fun setDataSets(dataSets: List<List<List<ChartEntry>>>): Boolean {
        if (!mutex.tryLock()) return false
        this.dataSets = dataSets.copy()
        cachedInternalComposedModel = null
        var runningUpdateCount = 0
        updateReceivers
            .values
            .takeIf { it.isNotEmpty() }
            ?.forEach { updateReceiver ->
                runningUpdateCount++
                coroutineScope.launch {
                    updateReceiver.handleUpdate()
                    if (--runningUpdateCount == 0) mutex.unlock()
                }
            }
            ?: mutex.unlock()
        return true
    }

    private suspend fun setDataSetsSuspending(dataSets: List<List<List<ChartEntry>>>): Deferred<Unit> {
        mutex.lock()
        this.dataSets = dataSets.copy()
        cachedInternalComposedModel = null
        val completableDeferred = CompletableDeferred<Unit>()
        var runningUpdateCount = 0
        updateReceivers
            .values
            .takeIf { it.isNotEmpty() }
            ?.map { updateReceiver ->
                runningUpdateCount++
                coroutineScope.launch {
                    updateReceiver.handleUpdate()
                    if (--runningUpdateCount == 0) {
                        mutex.unlock()
                        completableDeferred.complete(Unit)
                    }
                }
            }
            ?: run {
                mutex.unlock()
                completableDeferred.complete(Unit)
            }
        return completableDeferred
    }

    private fun getInternalModel(drawingModelStore: DrawingModelStore = DrawingModelStore.empty) =
        cachedInternalComposedModel
            ?.let { composedModel ->
                composedModel.copy(
                    composedEntryCollections = composedModel.composedEntryCollections
                        .map { model -> model.copy(drawingModelStore = drawingModelStore) },
                    drawingModelStore = drawingModelStore,
                )
            }
            ?: run {
                val models = dataSets.map { dataSet ->
                    val xRange = dataSet.xRange
                    val yRange = dataSet.yRange
                    val aggregateYRange = dataSet.calculateStackedYRange()
                    InternalModel(
                        entries = dataSet,
                        minX = xRange.start,
                        maxX = xRange.endInclusive,
                        minY = yRange.start,
                        maxY = yRange.endInclusive,
                        stackedPositiveY = aggregateYRange.endInclusive,
                        stackedNegativeY = aggregateYRange.start,
                        xGcd = dataSet.calculateXGcd(),
                        drawingModelStore = drawingModelStore,
                    )
                }
                InternalComposedModel(
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
                ).also { cachedInternalComposedModel = it }
            }

    override fun getModel(): ComposedChartEntryModel<ChartEntryModel> = getInternalModel()

    override suspend fun progressModel(key: Any, progress: Float) {
        with(updateReceivers[key] ?: return) {
            modelTransformer?.transform(drawingModelStore, progress)
            val internalModel = getInternalModel(drawingModelStore.copy())
            currentCoroutineContext().ensureActive()
            onModelCreated(internalModel)
        }
    }

    @WorkerThread
    override fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (progressModel: suspend (chartKey: Any, progress: Float) -> Unit) -> Unit,
        getOldModel: () -> ComposedChartEntryModel<ChartEntryModel>?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        drawingModelStore: MutableDrawingModelStore,
        updateChartValues: (ComposedChartEntryModel<ChartEntryModel>) -> ChartValuesProvider,
        onModelCreated: (ComposedChartEntryModel<ChartEntryModel>) -> Unit,
    ) {
        UpdateReceiver(
            cancelAnimation,
            startAnimation,
            onModelCreated,
            drawingModelStore,
            modelTransformerProvider?.getModelTransformer(),
            getOldModel,
            updateChartValues,
        ).run {
            updateReceivers[key] = this
            handleUpdate()
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
     * Creates a [Transaction], runs [block], and calls [Transaction.commit], returning its output. For suspending
     * behavior, use [runTransactionSuspending].
     */
    public fun runTransaction(block: Transaction.() -> Unit): Boolean = createTransaction().also(block).commit()

    /**
     * Creates a [Transaction], runs [block], and calls [Transaction.commitSuspending], returning its output.
     */
    public suspend fun runTransactionSuspending(block: Transaction.() -> Unit): Deferred<Unit> =
        createTransaction().also(block).commitSuspending()

    /**
     * Handles data updates. An initially empty list of data sets is created and can be updated via the class’s
     * functions. Each data set corresponds to a single nested [Chart].
     */
    public inner class Transaction internal constructor() {
        private val newDataSets = mutableListOf<List<List<ChartEntry>>>()

        /**
         * Populates the new list of data sets with the current data sets.
         */
        public fun populate() {
            newDataSets.setAll(dataSets)
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
            newDataSets.removeAt(index)
        }

        /**
         * Replaces the data set at the specified index with the provided data set.
         */
        public fun set(index: Int, dataSet: List<List<ChartEntry>>) {
            newDataSets[index] = dataSet
        }

        /**
         * Adds a data set.
         */
        public fun add(dataSet: List<List<ChartEntry>>) {
            newDataSets.add(dataSet)
        }

        /**
         * Adds a data set.
         */
        public fun add(index: Int, dataSet: List<List<ChartEntry>>) {
            newDataSets.add(index, dataSet)
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
            newDataSets.clear()
        }

        /**
         * Requests a data update. If the update is accepted, `true` is returned. If the update is rejected, which
         * occurs when there’s already an update in progress, `false` is returned. For suspending behavior, use
         * [commitSuspending].
         */
        public fun commit(): Boolean = setDataSets(newDataSets)

        /**
         * Runs a data update. Unlike [commit], this function suspends the current coroutine and waits until an update
         * can be run, meaning the update cannot be rejected. The returned [Deferred] implementation is marked as
         * completed once the update has been processed.
         */
        public suspend fun commitSuspending(): Deferred<Unit> = setDataSetsSuspending(newDataSets)
    }

    private inner class UpdateReceiver(
        val cancelAnimation: () -> Unit,
        val startAnimation: (progressModel: suspend (chartKey: Any, progress: Float) -> Unit) -> Unit,
        val onModelCreated: (ComposedChartEntryModel<ChartEntryModel>) -> Unit,
        val drawingModelStore: MutableDrawingModelStore,
        val modelTransformer: Chart.ModelTransformer<ComposedChartEntryModel<ChartEntryModel>>?,
        val getOldModel: () -> ComposedChartEntryModel<ChartEntryModel>?,
        val updateChartValues: (ComposedChartEntryModel<ChartEntryModel>) -> ChartValuesProvider,
    ) {
        fun handleUpdate() {
            cancelAnimation()
            modelTransformer?.prepareForTransformation(
                oldModel = getOldModel(),
                newModel = getModel(),
                drawingModelStore = drawingModelStore,
                chartValuesProvider = updateChartValues(getModel()),
            )
            startAnimation(::progressModel)
        }
    }

    private data class InternalModel(
        override val entries: List<List<ChartEntry>>,
        override val minX: Float,
        override val maxX: Float,
        override val minY: Float,
        override val maxY: Float,
        override val stackedPositiveY: Float,
        override val stackedNegativeY: Float,
        override val xGcd: Float,
        override val drawingModelStore: DrawingModelStore,
    ) : ChartEntryModel

    private data class InternalComposedModel(
        override val composedEntryCollections: List<InternalModel>,
        override val entries: List<List<ChartEntry>>,
        override val minX: Float,
        override val maxX: Float,
        override val minY: Float,
        override val maxY: Float,
        override val stackedPositiveY: Float,
        override val stackedNegativeY: Float,
        override val xGcd: Float,
        override val id: Int,
        override val drawingModelStore: DrawingModelStore,
    ) : ComposedChartEntryModel<ChartEntryModel>

    public companion object {
        /**
         * Creates a [ComposedChartEntryModelProducer], running an initial [Transaction]. [dispatcher] is the
         * [CoroutineDispatcher] to be used for update handling.
         */
        public fun build(
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            transaction: Transaction.() -> Unit = {},
        ): ComposedChartEntryModelProducer =
            ComposedChartEntryModelProducer(dispatcher).also { it.runTransaction(transaction) }
    }
}
