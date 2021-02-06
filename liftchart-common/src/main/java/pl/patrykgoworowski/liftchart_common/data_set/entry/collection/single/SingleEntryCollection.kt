package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection

typealias SingleEntriesModelListener<Entry> = (SingleEntriesModel<Entry>) -> Unit

interface SingleEntryCollection<Entry: AnyEntry> : EntryCollection {

    val data: Collection<Entry>
    val model: SingleEntriesModel<Entry>

    fun setEntries(entries: Collection<Entry>)

    fun addOnEntriesChangedListener(listener: SingleEntriesModelListener<Entry>)
    fun removeOnEntriesChangedListener(listener: SingleEntriesModelListener<Entry>)

    companion object {
        const val NO_VALUE = -1f
    }

}