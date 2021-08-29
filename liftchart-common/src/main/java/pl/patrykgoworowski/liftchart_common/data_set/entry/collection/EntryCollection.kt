package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

import pl.patrykgoworowski.liftchart_common.entry.DataEntry

interface EntryCollection<Model: EntryModel> {
    val minX: Float
    val maxX: Float
    val minY: Float
    val maxY: Float
    val stackedMinY: Float
    val stackedMaxY: Float
    val step: Float
    val model: Model

    fun setEntries(entries: List<List<DataEntry>>)
    fun setEntries(vararg entries: List<DataEntry>)

    fun addOnEntriesChangedListener(listener: (Model) -> Unit)
    fun removeOnEntriesChangedListener(listener: (Model) -> Unit)
}