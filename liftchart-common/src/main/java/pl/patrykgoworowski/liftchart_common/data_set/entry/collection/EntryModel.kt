package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import kotlin.math.abs

open class EntryModel(
    val entryCollections: List<List<DataEntry>>,
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val composedMaxY: Float,
    val step: Float,
) {

    fun getEntriesLength(): Int =
        (((abs(maxX) - abs(minX)) / step) + 1).toInt()
}
