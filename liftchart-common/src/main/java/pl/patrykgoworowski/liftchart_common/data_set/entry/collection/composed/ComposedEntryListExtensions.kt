package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.composed

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryCollection
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel

public operator fun <Model: EntryModel> EntryCollection<Model>.plus(
    other: EntryCollection<Model>
): ComposedEntryCollection<Model> =
    ComposedEntryCollection(listOf(this, other))

public operator fun <Model: EntryModel> ComposedEntryCollection<Model>.plus(
    other: EntryCollection<Model>
): ComposedEntryCollection<Model> =
    ComposedEntryCollection(entryCollections + other)

public operator fun <Model: EntryModel> ComposedEntryCollection<Model>.plus(
    other: ComposedEntryCollection<Model>
): ComposedEntryCollection<Model> =
    ComposedEntryCollection(entryCollections + other.entryCollections)
