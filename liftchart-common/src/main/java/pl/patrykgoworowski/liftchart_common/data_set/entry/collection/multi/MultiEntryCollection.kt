package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

typealias MultiEntriesModelListener = (MultiEntriesModel) -> Unit

interface MultiEntryCollection : EntryCollection<MultiEntriesModel> {

    val stackedMinY: Float
    val stackedMaxY: Float
    override val model: MultiEntriesModel

    fun setEntries(entries: List<List<DataEntry>>)
    fun setEntries(vararg entries: List<DataEntry>)

    override fun addOnEntriesChangedListener(listener: MultiEntriesModelListener)
    override fun removeOnEntriesChangedListener(listener: MultiEntriesModelListener)
}