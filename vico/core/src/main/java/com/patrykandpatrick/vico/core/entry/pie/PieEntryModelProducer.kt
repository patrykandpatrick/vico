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

package com.patrykandpatrick.vico.core.entry.pie

import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.entry.UpdateReceiver
import com.patrykandpatrick.vico.core.entry.diff.DiffProcessor
import com.patrykandpatrick.vico.core.entry.diff.PieDiffProcessor
import com.patrykandpatrick.vico.core.extension.setAll
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A [ChartModelProducer] implementation that generates [PieEntryModel] instances.
 *
 * @param entries a list of [PieEntry] instances used to generate the [PieEntryModel].
 * @param backgroundExecutor an [Executor] used to generate instances of the [PieEntryModel] off the main thread.
 * @param diffProcessor the [DiffProcessor] to use for difference animations.
 */
public open class PieEntryModelProducer(
    entries: List<PieEntry> = emptyList(),
    protected val backgroundExecutor: Executor = Executors.newSingleThreadExecutor(),
    protected val diffProcessor: DiffProcessor<PieEntry> = PieDiffProcessor(),
) : ChartModelProducer<PieEntryModel> {

    private var cachedModel: PieEntryModel? = null

    private val entries: ArrayList<PieEntry> = ArrayList(entries)

    private val updateReceivers: HashMap<Any, UpdateReceiver<PieEntryModel, DiffProcessor<PieEntry>>> = HashMap()

    init {
        setEntries(entries)
    }

    /**
     * Updates the list of [PieEntry] instances and notifies listeners about the update.
     *
     * @see registerForUpdates
     */
    public fun setEntries(entries: List<PieEntry>) {
        this.entries.setAll(entries)
        cachedModel = null
        updateReceivers.values.forEach { updateReceiver ->
            backgroundExecutor.execute {
                updateReceiver.diffProcessor.setEntries(
                    old = updateReceiver.getOldModel()?.entries.orEmpty(),
                    new = entries,
                )
                updateReceiver.listener()
            }
        }
    }

    override fun getModel(): PieEntryModel =
        cachedModel ?: getModel(entries).also { cachedModel = it }

    private fun getModel(
        entries: List<PieEntry>,
        maxValueFraction: Float = 1f,
    ): PieEntryModel = Model(
        entries = entries,
        maxValue = entries.fold(0f) { sum, entry ->
            (sum + entry.value) / maxValueFraction
        },
    )

    override fun progressModel(key: Any, progress: Float) {
        val updateReceiver = updateReceivers[key] ?: return
        val (_, modelReceiver, diffProcessor, getOldModel) = updateReceiver
        backgroundExecutor.execute {
            if (progress == 0f) {
                updateReceiver.isRunningInitialUpdate = updateReceiver.getOldModel() == null
            }
            progressModelSynchronously(
                progress = progress,
                modelReceiver = modelReceiver,
                diffProcessor = diffProcessor,
                isRunningInitialUpdate = updateReceiver.isRunningInitialUpdate,
            )
        }
    }

    private fun progressModelSynchronously(
        progress: Float,
        modelReceiver: (PieEntryModel) -> Unit,
        diffProcessor: DiffProcessor<PieEntry>,
        isRunningInitialUpdate: Boolean = false,
    ) {
        val entries = diffProcessor.progressDiff(progress)
        val maxValueFraction = if (isRunningInitialUpdate) progress else 1f
        modelReceiver(getModel(entries, maxValueFraction))
    }

    override fun registerForUpdates(
        key: Any,
        updateListener: () -> Unit,
        getOldModel: () -> PieEntryModel?,
        onModel: (PieEntryModel) -> Unit,
    ) {
        updateReceivers[key] = UpdateReceiver(
            listener = updateListener,
            onModel = onModel,
            diffProcessor = diffProcessor,
            getOldModel = getOldModel,
        )
        backgroundExecutor.execute {
            diffProcessor.setEntries(old = getOldModel()?.entries.orEmpty(), new = entries)
            updateListener()
        }
    }

    override fun isRegistered(key: Any): Boolean =
        updateReceivers.containsKey(key)

    override fun unregisterFromUpdates(key: Any) {
        updateReceivers.remove(key)
    }

    internal data class Model(
        override val entries: List<PieEntry>,
        override val maxValue: Float,
    ) : PieEntryModel
}

/**
 * Creates a [PieEntryModel] out of given numbers. Entries won’t have labels.
 */
public fun pieEntryModelOf(vararg entries: Number): PieEntryModel =
    PieEntryModelProducer(
        entries = entries.map { FloatPieEntry(it.toFloat()) },
    ).getModel()

/**
 * Creates a [PieEntryModel] out of given [PieEntry]s.
 */
public fun pieEntryModelOf(vararg entries: PieEntry): PieEntryModel =
    PieEntryModelProducer(entries = entries.toList()).getModel()
