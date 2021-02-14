package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single

import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

data class SingleEntriesModel<T: AnyEntry>(
    val entries: List<T>,
    override val minX: Float,
    override val maxX: Float,
    override val minY: Float,
    override val maxY: Float,
    override val step: Float
) : EntriesModel

fun <T: AnyEntry> emptySingleEntriesModel(): SingleEntriesModel<T> =
    SingleEntriesModel(emptyList(), 1f, 1f, 1f, 1f, 1f)