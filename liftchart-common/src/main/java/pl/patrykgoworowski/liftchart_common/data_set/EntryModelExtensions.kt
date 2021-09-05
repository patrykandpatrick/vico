package pl.patrykgoworowski.liftchart_common.data_set

import pl.patrykgoworowski.liftchart_common.data_set.composed.ComposedEntryModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel

fun emptyEntryModel(): EntryModel =
    EntryModel(
        emptyList(),
        1f,
        1f,
        1f,
        1f,
        1f,
        1f
    )

fun <Model : EntryModel> emptyComposedEntryModel(): ComposedEntryModel<Model> =
    ComposedEntryModel(
        emptyList(),
        emptyList(),
        1f,
        1f,
        1f,
        1f,
        1f,
        1f,
    )
