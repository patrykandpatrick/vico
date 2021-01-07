package pl.patrykgoworowski.liftchart_common.data_set.mergeable

import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer

public operator fun DataSetRenderer.plus(other: DataSetRenderer): DefaultMergedDataSet {
    return DefaultMergedDataSet(this, other)
}