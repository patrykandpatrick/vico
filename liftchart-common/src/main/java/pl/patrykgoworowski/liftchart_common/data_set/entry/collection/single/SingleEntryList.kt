package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.data_set.entry.SingleEntryModelCalculator
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.extension.setAll


class SingleEntryList() : SingleEntryCollection {

    override val data: ArrayList<DataEntry> = ArrayList()
    private val calculator = SingleEntryModelCalculator()
    private val listeners: ArrayList<SingleEntriesModelListener> = ArrayList()

    override val minX: Float by calculator::minX
    override val maxX: Float by calculator::maxX
    override val minY: Float by calculator::minY
    override val maxY: Float by calculator::maxY
    override val step: Float by calculator::step

    override var model: SingleEntriesModel = emptySingleEntriesModel()

    constructor(entries: Collection<DataEntry>) : this() {
        setEntries(entries)
    }

    override fun setEntries(entries: Collection<DataEntry>) {
        this.data.setAll(entries)
        refreshModel()
    }

    fun addEntry(entry: DataEntry) {
        if (data.add(entry)) {
            refreshModel()
        }
    }

    fun removeEntry(entry: DataEntry) {
        if (data.remove(entry)) {
            refreshModel()
        }
    }

    fun addEntries(entries: Collection<DataEntry>) {
        if (data.addAll(entries)) {
            refreshModel()
        }
    }

    fun removeEntries(entries: Collection<DataEntry>) {
        if (data.removeAll(entries)) {
            refreshModel()
        }
    }

    operator fun plusAssign(entry: DataEntry) = addEntry(entry)

    operator fun plusAssign(entries: Collection<DataEntry>) = addEntries(entries)

    operator fun minusAssign(entry: DataEntry) = removeEntry(entry)

    operator fun minusAssign(entries: Collection<DataEntry>) = removeEntries(entries)

    private fun refreshModel() {
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