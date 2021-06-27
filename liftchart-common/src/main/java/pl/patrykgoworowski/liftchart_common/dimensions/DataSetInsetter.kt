package pl.patrykgoworowski.liftchart_common.dimensions

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface DataSetInsetter {

    fun getInsets(
        outDimensions: MutableDimensions,
        model: EntriesModel,
    ): Dimensions

}