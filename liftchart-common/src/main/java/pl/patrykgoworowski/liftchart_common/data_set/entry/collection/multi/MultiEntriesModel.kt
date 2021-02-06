package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi

import pl.patrykgoworowski.liftchart_common.AnyEntry

data class MultiEntriesModel<T: AnyEntry>(
    val entryCollections: List<List<T>>,
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val stackedMinY: Float,
    val stackedMaxY: Float,
    val step: Float
)

fun <T: AnyEntry> emptyMultiEntriesModel(): MultiEntriesModel<T> =
    MultiEntriesModel(emptyList(), 1f, 1f, 1f, 1f, 1f, 1f, 1f)