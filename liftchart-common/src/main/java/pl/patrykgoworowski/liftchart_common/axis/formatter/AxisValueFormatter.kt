package pl.patrykgoworowski.liftchart_common.axis.formatter

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

fun interface AxisValueFormatter {
    fun formatValue(value: Float, model: EntriesModel): String
}