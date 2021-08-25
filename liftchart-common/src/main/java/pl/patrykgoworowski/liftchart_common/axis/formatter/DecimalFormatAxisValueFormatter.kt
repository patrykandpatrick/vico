package pl.patrykgoworowski.liftchart_common.axis.formatter

import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import java.math.RoundingMode
import java.text.DecimalFormat

class DecimalFormatAxisValueFormatter(
    private val decimalFormat: DecimalFormat
) : AxisValueFormatter {

    constructor() : this(DEF_FORMAT)

    constructor(
        pattern: String,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
    ) : this(getDecimalFormat(pattern, roundingMode))

    override fun formatValue(
        value: Float,
        index: Int,
        model: EntriesModel,
        dataSetModel: DataSetModel
    ): String = decimalFormat.format(value)

    companion object {
        private const val DEF_FORMAT = "#.##"

        private fun getDecimalFormat(
            pattern: String,
            roundingMode: RoundingMode,
        ): DecimalFormat =
            DecimalFormat(pattern).apply {
                this.roundingMode = roundingMode
            }
    }
}