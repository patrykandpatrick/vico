package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.AnyEntry

data class SingleEntriesModel<T: AnyEntry>(
    val entries: List<T>,
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val step: Float
)

fun <T: AnyEntry> emptySingleEntriesModel(): SingleEntriesModel<T> =
    SingleEntriesModel(emptyList(), 1f, 1f, 1f, 1f, 1f)