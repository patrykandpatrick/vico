package pl.patrykgoworowski.liftchart_common.data_set.bar

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel

enum class MergeMode {
    Stack, Grouped;

    fun getMaxY(model: MultiEntriesModel): Float = when (this) {
        Grouped -> model.maxY
        Stack -> model.stackedMaxY
    }

}