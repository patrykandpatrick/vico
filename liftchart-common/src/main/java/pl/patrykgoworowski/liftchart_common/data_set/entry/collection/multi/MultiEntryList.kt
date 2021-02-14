package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.entry.entryOf
import pl.patrykgoworowski.liftchart_common.extension.setAll

class MultiEntryList<Entry : AnyEntry>() :
    MultiEntryCollection<Entry> {

    private val calculator = MultiEntriesModelCalculator()
    private val listeners: ArrayList<MultiEntriesModelListener<Entry>> = ArrayList()

    override var model: MultiEntriesModel<Entry> = emptyMultiEntriesModel()

    override val minX: Float by calculator::minX
    override val maxX: Float by calculator::maxX
    override val minY: Float by calculator::minY
    override val maxY: Float by calculator::maxY
    override val step: Float by calculator::step
    override val stackedMaxY: Float by calculator::stackedMaxY
    override val stackedMinY: Float by calculator::stackedMinY

    val data: ArrayList<List<Entry>> = ArrayList()

    constructor(entryCollections: List<List<Entry>>) : this() {
        setEntryCollection(entryCollections)
    }

    constructor(vararg entryCollections: List<Entry>) : this() {
        setEntryCollection(entryCollections.toList())
    }

    override fun setEntryCollection(entryCollections: List<List<Entry>>) {
        data.setAll(entryCollections)
        refreshModel()
    }

    private fun refreshModel() {
        calculator.resetValues()
        calculator.calculateData(data)

        notifyChange()
    }

    override fun addOnEntriesChangedListener(listener: MultiEntriesModelListener<Entry>) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: MultiEntriesModelListener<Entry>) {
        listeners -= listener
    }

    private fun notifyChange() {
        val mergedEntries = calculator.stackedMap.map { (x, y) ->
            entryOf(x, y)
        }

        model = MultiEntriesModel(data, mergedEntries, minX, maxX, minY, maxY, stackedMinY, stackedMaxY, step)
        listeners.forEach { it(model) }
    }
}