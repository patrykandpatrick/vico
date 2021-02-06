package pl.patrykgoworowski.liftchart_common.data_set.bar

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel

enum class MergeMode {
    Stack, Grouped;

    fun <Entry : AnyEntry> getMaxY(model: MultiEntriesModel<Entry>): Float = when (this) {
        Grouped -> model.maxY
        Stack -> model.stackedMaxY
    }

    fun <Entry : AnyEntry> getWidthMultiplier(model: MultiEntriesModel<Entry>): Int = when (this) {
        Stack -> 1
        Grouped -> model.entryCollections.size
    }
}