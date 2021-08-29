package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

typealias SingleEntriesModelListener = (SingleEntriesModel) -> Unit

interface SingleEntryCollection : EntryCollection<SingleEntriesModel> {

    val data: Collection<DataEntry>
    override val model: SingleEntriesModel

    fun setEntries(entries: List<DataEntry>)

    override fun addOnEntriesChangedListener(listener: SingleEntriesModelListener)
    override fun removeOnEntriesChangedListener(listener: SingleEntriesModelListener)

    companion object {
        const val NO_VALUE = -1f
    }

}