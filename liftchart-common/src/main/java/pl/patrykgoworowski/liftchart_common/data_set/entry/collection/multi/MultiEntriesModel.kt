package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.entry.FloatEntry

data class MultiEntriesModel<T: AnyEntry>(
    val entryCollections: List<List<T>>,
    val mergedEntries: List<FloatEntry>,
    override val minX: Float,
    override val maxX: Float,
    override val minY: Float,
    override val maxY: Float,
    val stackedMinY: Float,
    val stackedMaxY: Float,
    override val step: Float
): EntriesModel

fun <T: AnyEntry> emptyMultiEntriesModel(): MultiEntriesModel<T> =
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