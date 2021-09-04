package pl.patrykgoworowski.liftchart_common.data_set.composed

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

class ComposedEntryModel<Model: EntryModel>(
    val composedEntryCollections: List<Model>,
    entryCollections: List<List<DataEntry>>,
    minX: Float,
    maxX: Float,
    minY: Float,
    maxY: Float,
    composedMaxY: Float,
    step: Float,
): EntryModel(
    entryCollections = entryCollections,
    minX = minX,
    maxX = maxX,
    minY = minY,
    maxY = maxY,
    composedMaxY = composedMaxY,
    step = step,
)
