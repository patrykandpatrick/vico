package pl.patrykgoworowski.liftchart_common.data_set.mergeable

import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer

public abstract class MergedDataSet<T> : DataSetRenderer() {

    abstract fun add(vararg other: T)
    abstract fun remove(vararg other: T)

    operator fun plusAssign(other: T) {
        add(other)
    }

    operator fun minusAssign(other: T) {
        remove(other)
    }
}