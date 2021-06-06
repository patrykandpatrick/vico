package pl.patrykgoworowski.liftchart_common.axis.formatter

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import java.text.DecimalFormat

class PercentageFormatAxisValueFormatter(
    pattern: String
) : AxisValueFormatter {

    private val decimalFormat = DecimalFormat(pattern)

    constructor() : this(DEF_PATTERN)

    override fun formatValue(value: Float, index: Int, model: EntriesModel): String {
        val percentage = value / model.maxY
        return decimalFormat.format(percentage)
    }

    companion object {
        private const val DEF_PATTERN = "#.##%"
    }

}