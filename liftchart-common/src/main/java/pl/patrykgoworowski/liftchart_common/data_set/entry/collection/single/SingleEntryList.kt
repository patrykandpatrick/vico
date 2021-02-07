package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.SingleEntryModelCalculator
import pl.patrykgoworowski.liftchart_common.extension.setAll


class SingleEntryList<Entry : AnyEntry>() : SingleEntryCollection<Entry> {

    override val data: ArrayList<Entry> = ArrayList()
    private val calculator = SingleEntryModelCalculator()
    private val listeners: ArrayList<SingleEntriesModelListener<Entry>> = ArrayList()

    override val minX: Float by calculator::minX
    override val maxX: Float by calculator::maxX
    override val minY: Float by calculator::minY
    override val maxY: Float by calculator::maxY
    override val step: Float by calculator::step

    override var model: SingleEntriesModel<Entry> = emptySingleEntriesModel()

    constructor(entries: Collection<Entry>) : this() {
        setEntries(entries)
    }

    override fun setEntries(entries: Collection<Entry>) {
        this.data.setAll(entries)
        refreshModel()
    }

    fun addEntry(entry: Entry) {
        if (data.add(entry)) {
            refreshModel()
        }
    }

    fun removeEntry(entry: Entry) {
        if (data.remove(entry)) {
            refreshModel()
        }
    }

    fun addEntries(entries: Collection<Entry>) {
        if (data.addAll(entries)) {
            refreshModel()
        }
    }

    fun removeEntries(entries: Collection<Entry>) {
        if (data.removeAll(entries)) {
            refreshModel()
        }
    }

    operator fun plusAssign(entry: Entry) = addEntry(entry)

    operator fun plusAssign(entries: Collection<Entry>) = addEntries(entries)

    operator fun minusAssign(entry: Entry) = removeEntry(entry)

    operator fun minusAssign(entries: Collection<Entry>) = removeEntries(entries)

    private fun refreshModel() {
        calculator.calculateData(data)
        notifyChange()
    }

    override fun addOnEntriesChangedListener(listener: SingleEntriesModelListener<Entry>) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: SingleEntriesModelListener<Entry>) {
        listeners -= listener
    }

    private fun notifyChange() {
        model = SingleEntriesModel(data, minX, maxX, minY, maxY, step)
        listeners.forEach { it(model) }

    }
}