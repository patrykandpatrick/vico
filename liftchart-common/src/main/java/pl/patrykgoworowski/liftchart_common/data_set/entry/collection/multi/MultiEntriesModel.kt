package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

data class MultiEntriesModel(
    val entryCollections: List<List<DataEntry>>,
    override val entries: List<DataEntry>,
    override val minX: Float,
    override val maxX: Float,
    override val minY: Float,
    override val maxY: Float,
    val stackedMinY: Float,
    val stackedMaxY: Float,
    override val step: Float
): EntriesModel

fun emptyMultiEntriesModel(): MultiEntriesModel =
    MultiEntriesModel(
        emptyList(),
        emptyList(),
        1f,
        1f,
        1f,
        1f,
        1f,
        1f,
        1f
    )