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

    private val diffProcessor: DiffProcessor<ChartEntry> = DefaultDiffProcessor()
    private val listeners: ArrayList<Listener> = ArrayList()

    public val data: ArrayList<List<ChartEntry>> = ArrayList()

    override lateinit var model: ChartEntryModel

    public var minX: Float = 0f
        private set

    public var maxX: Float = 0f
        private set

    public var minY: Float = 0f
        private set

    public var maxY: Float = 0f
        private set

    public var step: Float = 0f
        private set

    public var stackedMinY: Float = 0f
        private set

    public var stackedMaxY: Float = 0f
        private set

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
                refreshModel(
                    entries = diffProcessor.progressDiff(progress),
                    yRange = diffProcessor.yRangeProgressDiff(progress),
                    stackedYRange = diffProcessor.stackedYRangeProgressDiff(progress),
                )
            }
        } ?: kotlin.run {
            refreshModel(entries)
        }
    }

    public fun setEntries(vararg entries: List<ChartEntry>) {
        setEntries(entries.toList())
    }

    private fun refreshModel(
        entries: List<List<ChartEntry>>,
        yRange: ClosedFloatingPointRange<Float> = entries.yRange,
        stackedYRange: ClosedFloatingPointRange<Float> = entries.calculateStackedYRange(),
    ) {
        data.setAll(entries)
        val xRange = entries.xRange
        this.minX = xRange.start
        this.maxX = xRange.endInclusive
        this.minY = yRange.start
        this.maxY = yRange.endInclusive
        this.step = entries.calculateStep()
        this.stackedMinY = stackedYRange.start
        this.stackedMaxY = stackedYRange.endInclusive
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
            stepX = step,
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
        override val stepX: Float,
    ) : ChartEntryModel
}
