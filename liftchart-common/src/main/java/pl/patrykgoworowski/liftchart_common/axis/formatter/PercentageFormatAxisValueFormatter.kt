package pl.patrykgoworowski.liftchart_common.axis.formatter

import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import java.text.DecimalFormat

// TODO: Determine if this unused class can be deleted.
class PercentageFormatAxisValueFormatter(
    pattern: String
) : AxisValueFormatter {

    private val decimalFormat = DecimalFormat(pattern)

    constructor() : this(DEF_PATTERN)

    override fun formatValue(
        value: Float,
        index: Int,
        model: EntryModel,
        dataSetModel: DataSetModel
    ): String {
        val percentage = value / dataSetModel.maxY
        return decimalFormat.format(percentage)
    }

    companion object {
        private const val DEF_PATTERN = "#.##%"
    }
}