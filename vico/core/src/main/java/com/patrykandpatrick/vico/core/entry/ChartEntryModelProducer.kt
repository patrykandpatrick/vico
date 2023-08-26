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

import com.patrykandpatrick.vico.core.DEF_THREAD_POOL_SIZE
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.entry.diff.DefaultDiffProcessor
import com.patrykandpatrick.vico.core.entry.diff.DiffProcessor
import com.patrykandpatrick.vico.core.entry.diff.DrawingModelStore
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore
import com.patrykandpatrick.vico.core.extension.orEmpty
import com.patrykandpatrick.vico.core.extension.setToAllChildren
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A [ChartModelProducer] implementation that generates [ChartEntryModel] instances.
 *
 * @param entryCollections a two-dimensional list of [ChartEntry] instances used to generate the [ChartEntryModel].
 * @param backgroundExecutor an [Executor] used to generate instances of the [ChartEntryModel] off the main thread.
 * @param diffProcessor the [DiffProcessor] to use for difference animations.
 *
 * @see ChartModelProducer
 */
public class ChartEntryModelProducer(
    entryCollections: List<List<ChartEntry>>,
    backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
    private val diffProcessor: DiffProcessor<ChartEntry> = DefaultDiffProcessor(),
) : ChartModelProducer<ChartEntryModel> {

    private var cachedModel: ChartEntryModel? = null

    private var entriesHashCode: Int? = null

    private val updateReceivers: HashMap<Any, UpdateReceiver> = HashMap()

    private val executor: Executor = backgroundExecutor

    /**
     * A mutable two-dimensional list of the [ChartEntry] instances used to generate the [ChartEntryModel].
     */
    private val entries: ArrayList<ArrayList<ChartEntry>> = ArrayList()

    public constructor(
        vararg entryCollections: List<ChartEntry>,
        backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
        diffProcessor: DiffProcessor<ChartEntry> = DefaultDiffProcessor(),
    ) : this(entryCollections.toList(), backgroundExecutor, diffProcessor)

    init {
        setEntries(entryCollections)
    }

    /**
     * Updates the two-dimensional list of [ChartEntry] instances and notifies listeners about the update.
     *
     * @see entries
     * @see registerForUpdates
     */
    public fun setEntries(entries: List<List<ChartEntry>>) {
        this.entries.setToAllChildren(entries)
        val entriesHashCode = entries.hashCode()
        cachedModel = null
        updateReceivers.values.forEach { updateReceiver ->
            executor.execute {
                this.entriesHashCode = entriesHashCode
                updateReceiver.cancelProgressAnimation(hashCode())
                updateReceiver.transformer?.prepareForTransformation(
                    updateReceiver.getOldModel(),
                    getModel(),
                    updateReceiver.drawingModelStore,
                )
                updateReceiver.diffProcessor.setEntries(
                    old = updateReceiver.getOldModel()?.entries.orEmpty(),
                    new = entries,
                    oldYRange = updateReceiver.getOldModel()?.yRange.orEmpty,
                    oldAggregateYRange = updateReceiver.getOldModel()?.aggregateYRange.orEmpty,
                )
                updateReceiver.startProgressAnimation(hashCode(), ::progressModel)
            }
        }
    }

    /**
     * Updates the two-dimensional list of [ChartEntry] instances and notifies listeners about the update.
     *
     * @see entries
     * @see registerForUpdates
     */
    public fun setEntries(vararg entries: List<ChartEntry>) {
        setEntries(entries.toList())
    }

    override fun getModel(): ChartEntryModel =
        cachedModel ?: getModel(entries).also { cachedModel = it }

    override fun progressModel(key: Any, progress: Float) {
        val (_, _, modelReceiver, diffProcessor, store, transformer) = updateReceivers[key] ?: return
        executor.execute {
            progressModelSynchronously(progress, modelReceiver, diffProcessor, store, transformer)
        }
    }

    private fun progressModelSynchronously(
        progress: Float,
        modelReceiver: (ChartEntryModel) -> Unit,
        diffProcessor: DiffProcessor<ChartEntry>,
        drawingModelStore: MutableDrawingModelStore = MutableDrawingModelStore(),
        modelTransformer: Chart.ModelTransformer<ChartEntryModel>?,
    ) {
        modelTransformer?.transform(drawingModelStore, progress)
        val model = getModel(
            entries = diffProcessor.progressDiff(progress),
            yRange = diffProcessor.yRangeProgressDiff(progress),
            stackedPositiveYRange = diffProcessor.stackedYRangeProgressDiff(progress),
            drawingModelStore = drawingModelStore,
        )
        modelReceiver(model)
    }

    private fun getModel(
        entries: List<List<ChartEntry>>,
        yRange: ClosedFloatingPointRange<Float> = entries.yRange,
        stackedPositiveYRange: ClosedFloatingPointRange<Float> = entries.calculateStackedYRange(),
        drawingModelStore: DrawingModelStore = DrawingModelStore.Empty,
    ): ChartEntryModel =
        Model(
            entries = entries,
            minX = entries.xRange.start,
            maxX = entries.xRange.endInclusive,
            minY = yRange.start,
            maxY = yRange.endInclusive,
            stackedPositiveY = stackedPositiveYRange.endInclusive,
            stackedNegativeY = stackedPositiveYRange.start,
            xGcd = entries.calculateXGcd(),
            id = entriesHashCode ?: entries.hashCode().also { entriesHashCode = it },
            drawingModelStore = drawingModelStore,
        )

    override fun registerForUpdates(
        key: Any,
        cancelProgressAnimation: (producerKey: Any) -> Unit,
        startProgressAnimation: (producerKey: Any, progressModel: (chartKey: Any, progress: Float) -> Unit) -> Unit,
        getOldModel: () -> ChartEntryModel?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        drawingModelStore: MutableDrawingModelStore,
        onModel: (ChartEntryModel) -> Unit,
    ) {
        val modelTransformer = modelTransformerProvider
            ?.getModelTransformer<ChartEntryModel>()

        updateReceivers[key] = UpdateReceiver(
            cancelProgressAnimation = cancelProgressAnimation,
            startProgressAnimation = startProgressAnimation,
            onModel = onModel,
            diffProcessor = diffProcessor,
            drawingModelStore = drawingModelStore,
            transformer = modelTransformer,
            getOldModel = getOldModel,
        )
        executor.execute {
            cancelProgressAnimation(hashCode())
            modelTransformer
                ?.prepareForTransformation(getOldModel(), getModel(), drawingModelStore)

            diffProcessor.setEntries(
                old = getOldModel()?.entries.orEmpty(),
                new = entries,
                oldYRange = getOldModel()?.yRange.orEmpty,
                oldAggregateYRange = getOldModel()?.aggregateYRange.orEmpty,
            )
            startProgressAnimation(hashCode(), ::progressModel)
        }
    }

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    override fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key = key)

    private data class UpdateReceiver(
        val cancelProgressAnimation: (producerKey: Any) -> Unit,
        val startProgressAnimation: (producerKey: Any, progressModel: (chartKey: Any, progress: Float) -> Unit) -> Unit,
        val onModel: (ChartEntryModel) -> Unit,
        val diffProcessor: DiffProcessor<ChartEntry>,
        val drawingModelStore: MutableDrawingModelStore,
        val transformer: Chart.ModelTransformer<ChartEntryModel>?,
        val getOldModel: () -> ChartEntryModel?,
    )

    internal data class Model(
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
