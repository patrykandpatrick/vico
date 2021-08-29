package pl.patrykgoworowski.liftchart_common.dimensions

import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel

interface DataSetInsetter {

    fun getVerticalInsets(
        outDimensions: MutableDimensions,
        model: EntryModel,
        dataSetModel: DataSetModel,
    ): Dimensions

    fun getHorizontalInsets(
        outDimensions: MutableDimensions,
        availableHeight: Float,
        model: EntryModel,
        dataSetModel: DataSetModel,
    ): Dimensions

}