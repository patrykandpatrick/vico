package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

typealias SingleEntriesModelListener = (SingleEntriesModel) -> Unit

interface SingleEntryCollection : EntryCollection {

    val data: Collection<DataEntry>
    val model: SingleEntriesModel

    fun setEntries(entries: List<DataEntry>)

    fun addOnEntriesChangedListener(listener: SingleEntriesModelListener)
    fun removeOnEntriesChangedListener(listener: SingleEntriesModelListener)

    companion object {
        const val NO_VALUE = -1f
    }

}