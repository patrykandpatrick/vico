package pl.patrykgoworowski.liftchart_common.data_set.axis.formatter

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import java.text.DecimalFormat

class DecimalFormatAxisValueFormatter(
    private val decimalFormat: DecimalFormat
) : AxisValueFormatter {

    constructor() : this(DEF_FORMAT)

    constructor(pattern: String) : this(DecimalFormat(pattern))

    override fun formatValue(value: Float, model: EntriesModel): String {
        return decimalFormat.format(value)
    }

    companion object {
        private const val DEF_FORMAT = "#.##"
    }

}