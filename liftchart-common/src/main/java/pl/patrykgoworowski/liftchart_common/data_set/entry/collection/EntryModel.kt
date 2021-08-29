package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import kotlin.math.abs

open class EntryModel(
    val entryCollections: List<List<DataEntry>>,
    val entries: List<DataEntry>,
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val stackedMinY: Float,
    val stackedMaxY: Float,
    val step: Float,
) {

    fun getEntriesLength(): Int =
        (((abs(maxX) - abs(minX)) / step) + 1).toInt()
}

fun emptyEntryModel(): EntryModel =
    EntryModel(
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