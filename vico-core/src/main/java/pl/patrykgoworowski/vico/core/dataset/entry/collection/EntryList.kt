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

package pl.patrykgoworowski.vico.core.dataset.entry.collection

import pl.patrykgoworowski.vico.core.dataset.entry.collection.diff.DefaultDiffAnimator
import pl.patrykgoworowski.vico.core.dataset.entry.collection.diff.DefaultDiffProcessor
import pl.patrykgoworowski.vico.core.dataset.entry.collection.diff.DiffAnimator
import pl.patrykgoworowski.vico.core.dataset.entry.collection.diff.DiffProcessor
import pl.patrykgoworowski.vico.core.entry.DataEntry
import pl.patrykgoworowski.vico.core.extension.setAll

typealias EntryListModelListener = (EntryModel) -> Unit

class EntryList(
    public var diffAnimator: DiffAnimator = DefaultDiffAnimator(),
    public var animateChanges: Boolean = true
) : EntryCollection<EntryModel> {

    private val calculator = EntryModelCalculator()
    private val diffProcessor: DiffProcessor<DataEntry> = DefaultDiffProcessor()
    private val listeners: ArrayList<EntryListModelListener> = ArrayList()

    public val data: ArrayList<List<DataEntry>> = ArrayList()

    override var model: EntryModel = entryModel()
        private set

    val minX: Float
        get() = calculator.minX

    val maxX: Float
        get() = calculator.maxX

    val minY: Float
        get() = calculator.minY

    val maxY: Float
        get() = calculator.maxY

    val step: Float
        get() = calculator.step

    val stackedMaxY: Float
        get() = calculator.stackedMaxY

    val stackedMinY: Float
        get() = calculator.stackedMinY

    constructor(
        entryCollections: List<List<DataEntry>>,
        animateChanges: Boolean = true,
    ) : this(animateChanges = animateChanges) {
        setEntries(entryCollections)
    }

    constructor(
        vararg entryCollections: List<DataEntry>,
        animateChanges: Boolean = true,
    ) : this(animateChanges = animateChanges) {
        setEntries(entryCollections.toList())
    }

    public fun setEntries(entries: List<List<DataEntry>>) {
        if (animateChanges) {
            diffProcessor.setEntries(
                old = diffProcessor.progressDiff(diffAnimator.currentProgress),
                new = entries,
            )
            diffAnimator.start { progress ->
                refreshModel(diffProcessor.progressDiff(progress))
            }
        } else {
            refreshModel(entries)
        }
    }

    public fun setEntries(vararg entries: List<DataEntry>) {
        setEntries(entries.toList())
    }

    private fun refreshModel(entries: List<List<DataEntry>>) {
        data.setAll(entries)
        calculator.resetValues()
        calculator.calculateData(data)
        notifyChange()
    }

    override fun addOnEntriesChangedListener(listener: EntryListModelListener) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: EntryListModelListener) {
        listeners -= listener
    }

    private fun notifyChange() {
        model = entryModel(
            entryCollections = data,
            minX = minX,
            maxX = maxX,
            minY = minY,
            maxY = maxY,
            composedMaxY = stackedMaxY,
            step = step
        )
        listeners.forEach { it(model) }
    }
}
