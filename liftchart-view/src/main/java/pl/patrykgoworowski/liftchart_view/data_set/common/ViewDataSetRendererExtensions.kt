package pl.patrykgoworowski.liftchart_view.data_set.common

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet

operator fun <Model: EntriesModel> DataSet<Model>.plus(
    model: Model,
): ViewDataSet<Model> = ViewDataSet(this, model)