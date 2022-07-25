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

package com.patrykandpatryk.vico.core.entry

import com.patrykandpatryk.vico.core.DEF_THREAD_POOL_SIZE
import com.patrykandpatryk.vico.core.entry.diff.DefaultDiffProcessor
import com.patrykandpatryk.vico.core.entry.diff.DiffProcessor
import com.patrykandpatryk.vico.core.extension.setAll
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A subclass of [ChartModelProducer] that generates [ChartEntryModel] instances.
 *
 * @param entryCollections a two-dimensional list of [ChartEntry] instances used to generate the [ChartEntryModel].
 * @param backgroundExecutor an [Executor] used to generate instances of the [ChartEntryModel] off the main thread.
 * @param diffProcessor the [DiffProcessor] to use for diff animations.
 *
 * @see ChartModelProducer
 */
public class ChartEntryModelProducer(
    entryCollections: List<List<ChartEntry>>,
    backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
    private val diffProcessor: DiffProcessor<ChartEntry> = DefaultDiffProcessor(),
) : ChartModelProducer<ChartEntryModel> {

    private var cachedModel: ChartEntryModel? = null

    private val updateReceivers: HashMap<Any, UpdateReceiver> = HashMap()

    private val executor: Executor = backgroundExecutor

    /**
     * A mutable two-dimensional list of the [ChartEntry] instances used to generate the [ChartEntryModel].
     */
    private val entries: ArrayList<List<ChartEntry>> = ArrayList()

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
        this.entries.setAll(entries)
        cachedModel = null
        updateReceivers.values.forEach { (updateListener, _, diffProcessor) ->
            val oldModel = updateListener()
            executor.execute {
                diffProcessor.setEntries(old = oldModel?.entries.orEmpty(), new = entries)
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
        val (_, modelReceiver, diffProcessor) = updateReceivers[key] ?: return
        executor.execute {
            progressModelSynchronously(progress, modelReceiver, diffProcessor)
        }
    }

    private fun progressModelSynchronously(
        progress: Float,
        modelReceiver: (ChartEntryModel) -> Unit,
        diffProcessor: DiffProcessor<ChartEntry>,
    ) {
        val model = getModel(
            entries = diffProcessor.progressDiff(progress),
            yRange = diffProcessor.yRangeProgressDiff(progress),
            stackedYRange = diffProcessor.stackedYRangeProgressDiff(progress),
        )
        modelReceiver(model)
    }

    private fun getModel(
        entries: List<List<ChartEntry>>,
        yRange: ClosedFloatingPointRange<Float> = entries.yRange,
        stackedYRange: ClosedFloatingPointRange<Float> = entries.calculateStackedYRange(),
    ): ChartEntryModel =
        Model(
            entries = entries,
            minX = entries.xRange.start,
            maxX = entries.xRange.endInclusive,
            minY = yRange.start,
            maxY = yRange.endInclusive,
            stackedMaxY = stackedYRange.endInclusive,
            stepX = entries.calculateStep(),
        )

    override fun registerForUpdates(
        key: Any,
        updateListener: () -> ChartEntryModel?,
        onModel: (ChartEntryModel) -> Unit,
    ) {
        updateReceivers[key] = UpdateReceiver(
            listener = updateListener,
            onModel = onModel,
            diffProcessor = diffProcessor,
        )
        val oldModel = updateListener()
        executor.execute {
            diffProcessor.setEntries(old = oldModel?.entries.orEmpty(), new = entries)
            progressModelSynchronously(0f, onModel, diffProcessor)
        }
    }

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    private data class UpdateReceiver(
        val listener: () -> ChartEntryModel?,
        val onModel: (ChartEntryModel) -> Unit,
        val diffProcessor: DiffProcessor<ChartEntry>,
    )

    internal data class Model(
        override val entries: List<List<ChartEntry>>,
        override val minX: Float,
        override val maxX: Float,
        override val minY: Float,
        override val maxY: Float,
        override val stackedMaxY: Float,
        override val stepX: Float,
    ) : ChartEntryModel
}
