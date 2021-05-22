package pl.patrykgoworowski.liftchart_common.axis


import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface VerticalAxisRenderer : AxisRenderer<VerticalAxisPosition> {

    fun getWidth(
        model: EntriesModel,
        position: VerticalAxisPosition,
    ): Float

}