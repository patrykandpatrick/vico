package pl.patrykgoworowski.liftchart_common.axis.formatter

import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

// TODO: Determine if this unused class can be deleted.
object DefaultAxisFormatter : AxisValueFormatter {
    override fun formatValue(
        value: Float,
        index: Int,
        model: EntriesModel,
        dataSetModel: DataSetModel
    ): String = value.toString()
}