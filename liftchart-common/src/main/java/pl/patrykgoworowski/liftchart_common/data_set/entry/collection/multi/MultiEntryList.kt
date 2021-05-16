package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.entry.entryOf
import pl.patrykgoworowski.liftchart_common.extension.setAll

class MultiEntryList() :
    MultiEntryCollection {

    private val calculator = MultiEntriesModelCalculator()
    private val listeners: ArrayList<MultiEntriesModelListener> = ArrayList()

    override var model: MultiEntriesModel = emptyMultiEntriesModel()

    override val minX: Float by calculator::minX
    override val maxX: Float by calculator::maxX
    override val minY: Float by calculator::minY
    override val maxY: Float by calculator::maxY
    override val step: Float by calculator::step
    override val stackedMaxY: Float by calculator::stackedMaxY
    override val stackedMinY: Float by calculator::stackedMinY

    val data: ArrayList<List<DataEntry>> = ArrayList()

    constructor(entryCollections: List<List<DataEntry>>) : this() {
        setEntryCollection(entryCollections)
    }

    constructor(vararg entryCollections: List<DataEntry>) : this() {
        setEntryCollection(entryCollections.toList())
    }

    override fun setEntryCollection(entryCollections: List<List<DataEntry>>) {
        data.setAll(entryCollections)
        refreshModel()
    }

    private fun refreshModel() {
        calculator.resetValues()
        calculator.calculateData(data)

        notifyChange()
    }

    override fun addOnEntriesChangedListener(listener: MultiEntriesModelListener) {
        listeners += listener
        listener(model)
    }

    override fun removeOnEntriesChangedListener(listener: MultiEntriesModelListener) {
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