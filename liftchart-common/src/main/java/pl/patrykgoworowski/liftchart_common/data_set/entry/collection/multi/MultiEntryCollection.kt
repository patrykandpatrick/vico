package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

typealias MultiEntriesModelListener = (MultiEntriesModel) -> Unit

interface MultiEntryCollection : EntryCollection {

    val stackedMinY: Float
    val stackedMaxY: Float
    val model: MultiEntriesModel

    fun setEntryCollection(entryCollections: List<List<DataEntry>>)

    fun addOnEntriesChangedListener(listener: MultiEntriesModelListener)
    fun removeOnEntriesChangedListener(listener: MultiEntriesModelListener)
}