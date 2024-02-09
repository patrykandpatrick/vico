/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

import androidx.annotation.WorkerThread
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.entry.diff.ExtraStore
import com.patrykandpatrick.vico.core.entry.diff.MutableExtraStore
import com.patrykandpatrick.vico.core.extension.copy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * A [ChartModelProducer] implementation that generates [ChartEntryModel] instances.
 *
 * @param entryCollections the initial data set (list of series).
 * @param dispatcher the [CoroutineDispatcher] to be used for update handling.
 *
 * @see ChartModelProducer
 */
public class ChartEntryModelProducer(
    entryCollections: List<List<ChartEntry>>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ChartModelProducer<ChartEntryModel> {

    private var series = emptyList<List<ChartEntry>>()
    private var cachedInternalModel: InternalModel? = null
    private val mutex = Mutex()
    private val coroutineScope = CoroutineScope(dispatcher)
    private val updateReceivers: HashMap<Any, UpdateReceiver> = HashMap()
    private val extraStore = MutableExtraStore()

    public constructor(
        vararg entryCollections: List<ChartEntry>,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) : this(entryCollections.toList(), dispatcher)

    init {
        setEntries(entryCollections)
    }

    /**
     * Requests that the data set be updated to the provided one. If the update is accepted, `true` is returned. If the
     * update is rejected, which occurs when there’s already an update in progress, `false` is returned. For suspending
     * behavior, use [setEntriesSuspending]. [updateExtras] allows for adding auxiliary data, which can later be
     * retrieved via [ChartEntryModel.extraStore].
     */
    public fun setEntries(entries: List<List<ChartEntry>>, updateExtras: (MutableExtraStore) -> Unit = {}): Boolean {
        if (!mutex.tryLock()) return false
        series = entries.copy()
        updateExtras(extraStore)
        cachedInternalModel = null
        val deferredUpdates = updateReceivers.values.map { updateReceiver ->
            coroutineScope.async { updateReceiver.handleUpdate() }
        }
        coroutineScope.launch {
            deferredUpdates.awaitAll()
            mutex.unlock()
        }
        return true
    }

    /**
     * Updates the data set. Unlike [setEntries], this function suspends the current coroutine and waits until an update
     * can be run, meaning the update cannot be rejected. The returned [Deferred] implementation is marked as completed
     * once the update has been processed. [updateExtras] allows for adding auxiliary data, which can later be retrieved
     * via [ChartEntryModel.extraStore].
     */
    public suspend fun setEntriesSuspending(
        entries: List<List<ChartEntry>>,
        updateExtras: (MutableExtraStore) -> Unit = {},
    ): Deferred<Unit> {
        mutex.lock()
        series = entries.copy()
        updateExtras(extraStore)
        cachedInternalModel = null
        val completableDeferred = CompletableDeferred<Unit>()
        val deferredUpdates = updateReceivers.values.map { updateReceiver ->
            coroutineScope.async { updateReceiver.handleUpdate() }
        }
        coroutineScope.launch {
            deferredUpdates.awaitAll()
            mutex.unlock()
            completableDeferred.complete(Unit)
        }
        return completableDeferred
    }

    /**
     * Requests that the data set be updated to the provided one. If the update is accepted, `true` is returned. If the
     * update is rejected, which occurs when there’s already an update in progress, `false` is returned. For suspending
     * behavior, use [setEntriesSuspending]. [updateExtras] allows for adding auxiliary data, which can later be
     * retrieved via [ChartEntryModel.extraStore].
     */
    public fun setEntries(vararg entries: List<ChartEntry>, updateExtras: (MutableExtraStore) -> Unit = {}): Boolean =
        setEntries(entries.toList(), updateExtras)

    /**
     * Updates the data set. Unlike [setEntries], this function suspends the current coroutine and waits until an update
     * can be run, meaning the update cannot be rejected. The returned [Deferred] implementation is marked as completed
     * once the update has been processed. [updateExtras] allows for adding auxiliary data, which can later be retrieved
     * via [ChartEntryModel.extraStore].
     */
    public suspend fun setEntriesSuspending(
        vararg entries: List<ChartEntry>,
        updateExtras: (MutableExtraStore) -> Unit = {},
    ): Deferred<Unit> = setEntriesSuspending(entries.toList(), updateExtras)

    private fun getInternalModel(extraStore: ExtraStore? = null) =
        if (series.isEmpty()) {
            null
        } else {
            val mergedExtraStore = this.extraStore.let { if (extraStore != null) it + extraStore else it }
            cachedInternalModel?.copy(extraStore = mergedExtraStore)
                ?: run {
                    val xRange = series.xRange
                    val yRange = series.yRange
                    val aggregateYRange = series.calculateStackedYRange()
                    InternalModel(
                        entries = series,
                        minX = xRange.start,
                        maxX = xRange.endInclusive,
                        minY = yRange.start,
                        maxY = yRange.endInclusive,
                        stackedPositiveY = aggregateYRange.endInclusive,
                        stackedNegativeY = aggregateYRange.start,
                        id = series.hashCode(),
                        extraStore = mergedExtraStore,
                    ).also { cachedInternalModel = it }
                }
        }

    override fun getModel(): ChartEntryModel? = getInternalModel()

    private suspend fun transformModel(
        key: Any,
        fraction: Float,
        model: InternalModel?,
        chartValuesProvider: ChartValuesProvider,
    ) {
        with(updateReceivers[key] ?: return) {
            modelTransformer?.transform(extraStore, fraction)
            val internalModel = model?.copy(extraStore = this@ChartEntryModelProducer.extraStore + extraStore.copy())
            currentCoroutineContext().ensureActive()
            onModelCreated(internalModel, chartValuesProvider)
        }
    }

    @Deprecated("Use the function passed to the `startAnimation` lambda of `registerForUpdates`.")
    override suspend fun transformModel(key: Any, fraction: Float) {
        with(updateReceivers[key] ?: return) {
            modelTransformer?.transform(extraStore, fraction)
            val internalModel = getInternalModel(extraStore.copy())
            currentCoroutineContext().ensureActive()
            onModelCreated(internalModel, updateChartValues(internalModel))
        }
    }

    @WorkerThread
    override fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (transformModel: suspend (key: Any, fraction: Float) -> Unit) -> Unit,
        getOldModel: () -> ChartEntryModel?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        extraStore: MutableExtraStore,
        updateChartValues: (ChartEntryModel?) -> ChartValuesProvider,
        onModelCreated: (ChartEntryModel?, ChartValuesProvider) -> Unit,
    ) {
        UpdateReceiver(
            cancelAnimation,
            startAnimation,
            onModelCreated,
            extraStore,
            modelTransformerProvider?.getModelTransformer(),
            getOldModel,
            updateChartValues,
        ).run {
            updateReceivers[key] = this
            handleUpdate()
        }
    }

    @WorkerThread
    @Deprecated("Use the overload in which `onModelCreated` has two parameters.")
    override fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        getOldModel: () -> ChartEntryModel?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        extraStore: MutableExtraStore,
        updateChartValues: (ChartEntryModel?) -> ChartValuesProvider,
        onModelCreated: (ChartEntryModel?) -> Unit,
    ) {
        registerForUpdates(
            key,
            cancelAnimation,
            startAnimation,
            getOldModel,
            modelTransformerProvider,
            extraStore,
            updateChartValues,
        ) { model, _ ->
            onModelCreated(model)
        }
    }

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    override fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key = key)

    private inner class UpdateReceiver(
        val cancelAnimation: () -> Unit,
        val startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        val onModelCreated: (ChartEntryModel?, ChartValuesProvider) -> Unit,
        val extraStore: MutableExtraStore,
        val modelTransformer: Chart.ModelTransformer<ChartEntryModel>?,
        val getOldModel: () -> ChartEntryModel?,
        val updateChartValues: (ChartEntryModel?) -> ChartValuesProvider,
    ) {
        fun handleUpdate() {
            cancelAnimation()
            val model = getInternalModel()
            val chartValuesProvider = updateChartValues(model)
            modelTransformer?.prepareForTransformation(
                oldModel = getOldModel(),
                newModel = model,
                extraStore = extraStore,
                chartValuesProvider = chartValuesProvider,
            )
            startAnimation { key, fraction -> transformModel(key, fraction, model, chartValuesProvider) }
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
        override val id: Int,
        override val extraStore: ExtraStore,
    ) : ChartEntryModel {
        override val xGcd: Float get() = entries.calculateXGcd()
    }
}
