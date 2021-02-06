package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection

typealias MultiEntriesModelListener<T> = (MultiEntriesModel<T>) -> Unit

interface MultiEntryCollection<Entry : AnyEntry> :
    EntryCollection {

    val stackedMinY: Float
    val stackedMaxY: Float
    val model: MultiEntriesModel<Entry>

    fun setEntryCollection(entryCollections: List<List<Entry>>)

    fun addOnEntriesChangedListener(listener: MultiEntriesModelListener<Entry>)
    fun removeOnEntriesChangedListener(listener: MultiEntriesModelListener<Entry>)
}