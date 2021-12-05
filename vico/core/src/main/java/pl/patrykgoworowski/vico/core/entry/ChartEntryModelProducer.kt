/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.entry

import pl.patrykgoworowski.vico.core.entry.diff.DefaultDiffProcessor
import pl.patrykgoworowski.vico.core.entry.diff.DiffAnimator
import pl.patrykgoworowski.vico.core.entry.diff.DiffProcessor
import pl.patrykgoworowski.vico.core.extension.setAll

private typealias Listener = (ChartEntryModel) -> Unit

public class ChartEntryModelProducer(
    entryCollections: List<List<ChartEntry>>,
    private val diffAnimator: DiffAnimator? = null,
) : ChartModelProducer<ChartEntryModel> {

    private val calculator = EntryModelCalculator()
    private val diffProcessor: DiffProcessor<ChartEntry> = DefaultDiffProcessor()
    private val listeners: ArrayList<Listener> = ArrayList()

    public val data: ArrayList<List<ChartEntry>> = ArrayList()

    override lateinit var model: ChartEntryModel

    public val minX: Float
        get() = calculator.minX

    public val maxX: Float
        get() = calculator.maxX

    public val minY: Float
        get() = calculator.minY

    public val maxY: Float
        get() = calculator.maxY

    public val step: Float
        get() = calculator.step

    public val stackedMaxY: Float
        get() = calculator.stackedMaxY

    public val stackedMinY: Float
        get() = calculator.stackedMinY

    public constructor(
        vararg entryCollections: List<ChartEntry>,
        diffAnimator: DiffAnimator? = null,
    ) : this(entryCollections.toList(), diffAnimator = diffAnimator)

    init {
        setEntries(entryCollections)
    }

    public fun setEntries(entries: List<List<ChartEntry>>) {
        diffAnimator?.also { animator ->
            diffProcessor.setEntries(
                old = diffProcessor.progressDiff(animator.currentProgress),
                new = entries,
            )
            animator.start { progress ->
                refreshModel(diffProcessor.progressDiff(progress))
            }
        } ?: kotlin.run {
            refreshModel(entries)
        }
    }

    public fun setEntries(vararg entries: List<ChartEntry>) {
        setEntries(entries.toList())
    }

    private fun refreshModel(entries: List<List<ChartEntry>>) {
        data.setAll(entries)
        calculator.resetValues()
        calculator.calculateData(data)
        notifyChange()
    }

    override fun addOnEntriesChangedListener(listener: Listener) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: Listener) {
        listeners -= listener
    }

    private fun notifyChange() {
        model = Model(
            entries = data,
            minX = minX,
            maxX = maxX,
            minY = minY,
            maxY = maxY,
            composedMaxY = stackedMaxY,
            step = step
        )
        listeners.forEach { it(model) }
    }

    internal data class Model(
        override val entries: List<List<ChartEntry>>,
        override val minX: Float,
        override val maxX: Float,
        override val minY: Float,
        override val maxY: Float,
        override val composedMaxY: Float,
        override val step: Float
    ) : ChartEntryModel
}
