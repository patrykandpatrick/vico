package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

interface EntryCollection<Model: EntryModel> {

    val model: Model

    fun addOnEntriesChangedListener(listener: (Model) -> Unit)
    fun removeOnEntriesChangedListener(listener: (Model) -> Unit)
}
