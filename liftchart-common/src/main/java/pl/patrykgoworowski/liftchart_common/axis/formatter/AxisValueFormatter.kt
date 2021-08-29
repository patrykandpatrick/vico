package pl.patrykgoworowski.liftchart_common.axis.formatter

import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel

fun interface AxisValueFormatter {
    fun formatValue(
        value: Float,
        index: Int,
        model: EntryModel,
        dataSetModel: DataSetModel,
    ): String
}