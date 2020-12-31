package pl.patrykgoworowski.liftchart_core.data_set.mergeable

import pl.patrykgoworowski.liftchart_core.data_set.DataSet

public interface MergedDataSet<T, R> : DataSet {
    fun add(vararg other: T): R
    fun remove(vararg other: T): R

    operator fun plusAssign(other: T) {
        add(other)
    }

    operator fun minusAssign(other: T) {
        remove(other)
    }
}