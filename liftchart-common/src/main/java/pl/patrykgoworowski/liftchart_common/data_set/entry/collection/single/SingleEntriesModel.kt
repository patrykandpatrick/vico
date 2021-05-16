package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

data class SingleEntriesModel(
    override val entries: List<DataEntry>,
    override val minX: Float,
    override val maxX: Float,
    override val minY: Float,
    override val maxY: Float,
    override val step: Float
) : EntriesModel

fun emptySingleEntriesModel(): SingleEntriesModel =
    SingleEntriesModel(emptyList(), 1f, 1f, 1f, 1f, 1f)