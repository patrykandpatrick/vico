package pl.patrykgoworowski.liftchart_common.data_set.entry.collection

import kotlin.math.abs

interface EntriesModel {
    val minX: Float
    val maxX: Float
    val minY: Float
    val maxY: Float
    val step: Float

    fun getEntriesLength(): Int =
        (((abs(maxX) - abs(minX)) / step) + 1).toInt()
}