package pl.patrykgoworowski.liftchart_common.axis


import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface VerticalAxisRenderer : AxisRenderer<VerticalAxisPosition> {

    var maxLabelCount: Int
    var labelSpacing: Float

    fun getWidth(
        model: EntriesModel,
        position: VerticalAxisPosition,
        availableHeight: Int,
    ): Int

}