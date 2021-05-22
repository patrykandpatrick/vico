package pl.patrykgoworowski.liftchart_common.axis

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface HorizontalAxisRenderer : AxisRenderer<HorizontalAxisPosition> {

    fun getHeight(
        model: EntriesModel,
        position: HorizontalAxisPosition,
        width: Float,
    ): Float

}