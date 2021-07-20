package pl.patrykgoworowski.liftchart_common.dimensions

import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface DataSetInsetter {

    fun getVerticalInsets(
        outDimensions: MutableDimensions,
        model: EntriesModel,
        dataSetModel: DataSetModel,
    ): Dimensions

    fun getHorizontalInsets(
        outDimensions: MutableDimensions,
        availableHeight: Float,
        model: EntriesModel,
        dataSetModel: DataSetModel,
    ): Dimensions

}