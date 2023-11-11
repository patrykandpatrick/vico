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

import androidx.annotation.WorkerThread
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.entry.diff.DrawingModelStore
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore
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
     * behavior, use [setEntriesSuspending].
     */
    public fun setEntries(entries: List<List<ChartEntry>>): Boolean {
        if (!mutex.tryLock()) return false
        series = entries.copy()
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
     * once the update has been processed.
     */
    public suspend fun setEntriesSuspending(entries: List<List<ChartEntry>>): Deferred<Unit> {
        mutex.lock()
        series = entries.copy()
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
     * behavior, use [setEntriesSuspending].
     */
    public fun setEntries(vararg entries: List<ChartEntry>): Boolean = setEntries(entries.toList())

    /**
     * Updates the data set. Unlike [setEntries], this function suspends the current coroutine and waits until an update
     * can be run, meaning the update cannot be rejected. The returned [Deferred] implementation is marked as completed
     * once the update has been processed.
     */
    public suspend fun setEntriesSuspending(vararg entries: List<ChartEntry>): Deferred<Unit> =
        setEntriesSuspending(entries.toList())

    private fun getInternalModel(drawingModelStore: DrawingModelStore = DrawingModelStore.empty) =
        if (series.isEmpty()) {
            null
        } else {
            cachedInternalModel?.copy(drawingModelStore = drawingModelStore)
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
                        xGcd = series.calculateXGcd(),
                        id = series.hashCode(),
                        drawingModelStore = drawingModelStore,
                    ).also { cachedInternalModel = it }
                }
        }

    override fun getModel(): ChartEntryModel? = getInternalModel()

    override suspend fun transformModel(key: Any, fraction: Float) {
        with(updateReceivers[key] ?: return) {
            modelTransformer?.transform(drawingModelStore, fraction)
            val internalModel = getInternalModel(drawingModelStore.copy())
            currentCoroutineContext().ensureActive()
            onModelCreated(internalModel)
        }
    }

    @WorkerThread
    override fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        getOldModel: () -> ChartEntryModel?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        drawingModelStore: MutableDrawingModelStore,
        updateChartValues: (ChartEntryModel?) -> ChartValuesProvider,
        onModelCreated: (ChartEntryModel?) -> Unit,
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

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    override fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key = key)

    private inner class UpdateReceiver(
        val cancelAnimation: () -> Unit,
        val startAnimation: (transformModel: suspend (chartKey: Any, fraction: Float) -> Unit) -> Unit,
        val onModelCreated: (ChartEntryModel?) -> Unit,
        val drawingModelStore: MutableDrawingModelStore,
        val modelTransformer: Chart.ModelTransformer<ChartEntryModel>?,
        val getOldModel: () -> ChartEntryModel?,
        val updateChartValues: (ChartEntryModel?) -> ChartValuesProvider,
    ) {
        fun handleUpdate() {
            cancelAnimation()
            modelTransformer?.prepareForTransformation(
                oldModel = getOldModel(),
                newModel = getModel(),
                drawingModelStore = drawingModelStore,
                chartValuesProvider = updateChartValues(getModel()),
            )
            startAnimation(::transformModel)
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
        override val id: Int,
        override val drawingModelStore: DrawingModelStore,
    ) : ChartEntryModel
}
