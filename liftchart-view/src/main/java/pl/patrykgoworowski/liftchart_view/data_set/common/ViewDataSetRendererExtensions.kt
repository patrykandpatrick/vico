package pl.patrykgoworowski.liftchart_view.data_set.common

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet

operator fun <Model: EntryModel> DataSet<Model>.plus(
    model: Model,
): ViewDataSet<Model> = ViewDataSet(this, model)