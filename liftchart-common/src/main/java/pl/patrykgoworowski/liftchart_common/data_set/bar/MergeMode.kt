package pl.patrykgoworowski.liftchart_common.data_set.bar

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel

enum class MergeMode {
    Stack, Grouped;

    fun getMaxY(model: EntryModel): Float = when (this) {
        Grouped -> model.maxY
        Stack -> model.stackedMaxY
    }

}