package pl.patrykgoworowski.liftchart_common.data_set.composed

import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet

public operator fun <Model : EntryModel> DataSet<Model>.plus(
    other: DataSet<Model>
): ComposedDataSet<Model> =
    ComposedDataSet(listOf(this, other))

public operator fun <Model : EntryModel> ComposedDataSet<Model>.plus(
    other: DataSet<Model>
): ComposedDataSet<Model> =
    ComposedDataSet(dataSets + other)

public operator fun <Model : EntryModel> ComposedDataSet<Model>.plus(
    other: ComposedDataSet<Model>
): ComposedDataSet<Model> =
    ComposedDataSet(dataSets + other.dataSets)
