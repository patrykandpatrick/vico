package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.data_set.entry.SingleEntryModelCalculator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DefaultDiffAnimator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DefaultDiffProcessor
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DiffAnimator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.DiffProcessor
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.extension.setAll


class SingleEntryList(
    var diffAnimator: DiffAnimator = DefaultDiffAnimator(),
    var animateChanges: Boolean = true,
) : SingleEntryCollection {

    private val calculator = SingleEntryModelCalculator()
    private val diffProcessor: DiffProcessor<DataEntry> = DefaultDiffProcessor()
    private val listeners: ArrayList<SingleEntriesModelListener> = ArrayList()

    override val data: ArrayList<DataEntry> = ArrayList()

    override val minX: Float by calculator::minX
    override val maxX: Float by calculator::maxX
    override val minY: Float by calculator::minY
    override val maxY: Float by calculator::maxY
    override val step: Float by calculator::step

    override var model: SingleEntriesModel = emptySingleEntriesModel()

    constructor(entries: List<DataEntry>) : this() {
        setEntries(entries)
    }

    override fun setEntries(entries: List<DataEntry>) {
        if (animateChanges) {
            diffProcessor.setEntries(
                old = diffProcessor.progressDiff(diffAnimator.currentProgress),
                new = listOf(entries),
            )
            diffAnimator.start { progress ->
                refreshModel(diffProcessor.progressDiff(progress).first())
            }
        } else {
            refreshModel(entries)
        }
    }

    fun addEntry(entry: DataEntry) {
        setEntries(data + entry)
    }

    fun removeEntry(entry: DataEntry) {
        setEntries(data - entry)
    }

    fun addEntries(entries: Collection<DataEntry>) {
        setEntries(data + entries)
    }

    fun removeEntries(entries: Collection<DataEntry>) {
        setEntries(data - entries)
    }

    operator fun plusAssign(entry: DataEntry) = addEntry(entry)

    operator fun plusAssign(entries: Collection<DataEntry>) = addEntries(entries)

    operator fun minusAssign(entry: DataEntry) = removeEntry(entry)

    operator fun minusAssign(entries: Collection<DataEntry>) = removeEntries(entries)

    private fun refreshModel(entries: Collection<DataEntry>) {
        data.setAll(entries)
        calculator.calculateData(data)
        notifyChange()
    }

    override fun addOnEntriesChangedListener(listener: SingleEntriesModelListener) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: SingleEntriesModelListener) {
        listeners -= listener
    }

    private fun notifyChange() {
        model = SingleEntriesModel(data, minX, maxX, minY, maxY, step)
        listeners.forEach { it(model) }
    }
}