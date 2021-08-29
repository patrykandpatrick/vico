package pl.patrykgoworowski.liftchart_common.data_set.entry

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface EntryCollection<Model: EntriesModel> {
    val minX: Float
    val maxX: Float
    val minY: Float
    val maxY: Float
    val step: Float
    val model: Model

    fun addOnEntriesChangedListener(listener: (Model) -> Unit)
    fun removeOnEntriesChangedListener(listener: (Model) -> Unit)
}