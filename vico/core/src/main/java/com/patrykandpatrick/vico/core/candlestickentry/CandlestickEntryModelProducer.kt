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

package com.patrykandpatrick.vico.core.candlestickentry

import com.patrykandpatrick.vico.core.DEF_THREAD_POOL_SIZE
import com.patrykandpatrick.vico.core.candlestickentry.diff.CandlestickDiffProcessor
import com.patrykandpatrick.vico.core.candlestickentry.diff.DefaultCandlestickDiffProcessor
import com.patrykandpatrick.vico.core.chart.candlestick.CandlestickChartType
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.calculateXGcd
import com.patrykandpatrick.vico.core.entry.diff.DiffProcessor
import com.patrykandpatrick.vico.core.entry.xRange
import com.patrykandpatrick.vico.core.extension.setAll
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A [ChartModelProducer] implementation that generates [CandlestickEntryModel] instances.
 *
 * @param entryCollections a list of [CandlestickEntry] instances used to generate the [CandlestickEntryModel].
 * @param candlestickChartType TODO
 * @param backgroundExecutor an [Executor] used to generate instances of the [CandlestickEntryModel] off the main
 * thread.
 * @param diffProcessor the [DiffProcessor] to use for difference animations.
 *
 * @see ChartModelProducer
 */
public class CandlestickEntryModelProducer(
    entryCollections: List<CandlestickEntry> = emptyList(),
    public val candlestickChartType: CandlestickChartType = CandlestickChartType.Standard,
    backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
    private val diffProcessor: CandlestickDiffProcessor<CandlestickEntry, CandlestickTypedEntry> =
        DefaultCandlestickDiffProcessor(),
) : ChartModelProducer<CandlestickEntryModel> {

    private var cachedModel: CandlestickEntryModel? = null

    private var entriesHashCode: Int = 0

    private val updateReceivers: HashMap<Any, UpdateReceiver> = HashMap()

    private val executor: Executor = backgroundExecutor

    /**
     * A mutable list of the [CandlestickEntry] instances used to generate the [CandlestickEntryModel].
     */
    private val entries: ArrayList<CandlestickEntry> = ArrayList()

    init {
        setEntries(entryCollections)
    }

    /**
     * Updates the two-dimensional list of [CandlestickEntry] instances and notifies listeners about the update.
     *
     * @see entries
     * @see registerForUpdates
     */
    public fun setEntries(entries: List<CandlestickEntry>) {
        this.entries.setAll(entries)
        val entriesHashCode = entries.hashCode()
        cachedModel = null
        updateReceivers.values.forEach { updateReceiver ->
            executor.execute {
                this.entriesHashCode = entriesHashCode
                updateReceiver.diffProcessor.setEntries(
                    old = updateReceiver.getOldModel()?.entries.orEmpty(),
                    new = entries,
                )
                updateReceiver.listener()
            }
        }
    }

    override fun getModel(): CandlestickEntryModel =
        cachedModel ?: getModel(
            entries = diffProcessor.getTypedEntries(
                entries = entries,
                candlestickChartType = candlestickChartType,
            ),
        ).also { cachedModel = it }

    override fun progressModel(key: Any, progress: Float) {
        val (_, modelReceiver, diffProcessor) = updateReceivers[key] ?: return
        executor.execute {
            progressModelSynchronously(progress, modelReceiver, diffProcessor)
        }
    }

    private fun progressModelSynchronously(
        progress: Float,
        modelReceiver: (CandlestickEntryModel) -> Unit,
        diffProcessor: CandlestickDiffProcessor<CandlestickEntry, CandlestickTypedEntry>,
    ) {
        val model = getModel(
            entries = diffProcessor.progressDiff(progress, candlestickChartType),
            yRange = diffProcessor.yRangeProgressDiff(progress),
        )
        modelReceiver(model)
    }

    private fun getModel(
        entries: List<CandlestickTypedEntry>,
        yRange: ClosedFloatingPointRange<Float> = entries.yRange,
    ): CandlestickEntryModel =
        Model(
            entries = entries,
            minX = entries.xRange.start,
            maxX = entries.xRange.endInclusive,
            minY = yRange.start,
            maxY = yRange.endInclusive,
            xGcd = entries.calculateXGcd(),
            id = entriesHashCode,
        )

    override fun registerForUpdates(
        key: Any,
        updateListener: () -> Unit,
        getOldModel: () -> CandlestickEntryModel?,
        onModel: (CandlestickEntryModel) -> Unit,
    ) {
        updateReceivers[key] = UpdateReceiver(
            listener = updateListener,
            onModel = onModel,
            diffProcessor = diffProcessor,
            getOldModel = getOldModel,
        )
        executor.execute {
            diffProcessor.setEntries(old = getOldModel()?.entries.orEmpty(), new = entries)
            updateListener()
        }
    }

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    override fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key = key)

    private data class UpdateReceiver(
        val listener: () -> Unit,
        val onModel: (CandlestickEntryModel) -> Unit,
        val diffProcessor: CandlestickDiffProcessor<CandlestickEntry, CandlestickTypedEntry>,
        val getOldModel: () -> CandlestickEntryModel?,
    )

    internal data class Model(
        override val entries: List<CandlestickTypedEntry>,
        override val minX: Float,
        override val maxX: Float,
        override val minY: Float,
        override val maxY: Float,
        override val xGcd: Float,
        override val id: Int,
    ) : CandlestickEntryModel
}
