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
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.entry.diff.DrawingModelStore
import com.patrykandpatrick.vico.core.entry.diff.MutableDrawingModelStore
import com.patrykandpatrick.vico.core.extension.copy
import com.patrykandpatrick.vico.core.extension.setToAllChildren
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A [ChartModelProducer] implementation that generates [ChartEntryModel] instances.
 *
 * @param entryCollections a two-dimensional list of [ChartEntry] instances used to generate the [ChartEntryModel].
 * @param backgroundExecutor an [Executor] used to generate instances of the [ChartEntryModel] off the main thread.
 *
 * @see ChartModelProducer
 */
public class ChartEntryModelProducer(
    entryCollections: List<List<ChartEntry>>,
    backgroundExecutor: Executor = Executors.newFixedThreadPool(DEF_THREAD_POOL_SIZE),
) : ChartModelProducer<ChartEntryModel> {

    private var cachedInternalModel: InternalModel? = null

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
    ) : this(entryCollections.toList(), backgroundExecutor)

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
        entriesHashCode = entries.hashCode()
        cachedInternalModel = null
        updateReceivers.values.forEach { updateReceiver ->
            executor.execute {
                updateReceiver.cancelAnimation()
                updateReceiver.modelTransformer?.prepareForTransformation(
                    oldModel = updateReceiver.getOldModel(),
                    newModel = getModel(),
                    drawingModelStore = updateReceiver.drawingModelStore,
                    chartValuesManager = updateReceiver.updateChartValues(getModel()),
                )
                updateReceiver.startAnimation(::progressModel)
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

    private fun getInternalModel(drawingModelStore: DrawingModelStore = DrawingModelStore.empty): InternalModel {
        cachedInternalModel.let { if (it != null) return it }
        val xRange = entries.xRange
        val yRange = entries.yRange
        val aggregateYRange = entries.calculateStackedYRange()
        return InternalModel(
            entries = entries.copy(),
            minX = xRange.start,
            maxX = xRange.endInclusive,
            minY = yRange.start,
            maxY = yRange.endInclusive,
            stackedPositiveY = aggregateYRange.endInclusive,
            stackedNegativeY = aggregateYRange.start,
            xGcd = entries.calculateXGcd(),
            id = entriesHashCode ?: entries.hashCode().also { entriesHashCode = it },
            drawingModelStore = drawingModelStore,
        )
    }

    override fun getModel(): ChartEntryModel = getInternalModel()

    override fun progressModel(key: Any, progress: Float) {
        with(updateReceivers[key] ?: return) {
            executor.execute {
                modelTransformer?.transform(drawingModelStore, progress)
                onModelCreated(getInternalModel(drawingModelStore.copy()))
            }
        }
    }

    override fun registerForUpdates(
        key: Any,
        cancelAnimation: () -> Unit,
        startAnimation: (progressModel: (chartKey: Any, progress: Float) -> Unit) -> Unit,
        getOldModel: () -> ChartEntryModel?,
        modelTransformerProvider: Chart.ModelTransformerProvider?,
        drawingModelStore: MutableDrawingModelStore,
        updateChartValues: (ChartEntryModel) -> ChartValuesManager,
        onModelCreated: (ChartEntryModel) -> Unit,
    ) {
        val modelTransformer = modelTransformerProvider?.getModelTransformer<ChartEntryModel>()
        updateReceivers[key] = UpdateReceiver(
            cancelAnimation,
            startAnimation,
            onModelCreated,
            drawingModelStore,
            modelTransformer,
            getOldModel,
            updateChartValues,
        )
        executor.execute {
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

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    override fun isRegistered(key: Any): Boolean = updateReceivers.containsKey(key = key)

    private data class UpdateReceiver(
        val cancelAnimation: () -> Unit,
        val startAnimation: (progressModel: (chartKey: Any, progress: Float) -> Unit) -> Unit,
        val onModelCreated: (ChartEntryModel) -> Unit,
        val drawingModelStore: MutableDrawingModelStore,
        val modelTransformer: Chart.ModelTransformer<ChartEntryModel>?,
        val getOldModel: () -> ChartEntryModel?,
        val updateChartValues: (ChartEntryModel) -> ChartValuesManager,
    )

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
