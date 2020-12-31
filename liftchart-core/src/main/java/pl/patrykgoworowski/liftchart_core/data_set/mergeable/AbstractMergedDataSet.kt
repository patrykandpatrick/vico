package pl.patrykgoworowski.liftchart_core.data_set.mergeable

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_core.data_set.DataSet

abstract class AbstractMergedDataSet<T: DataSet, R: MergedDataSet<T, R>> public constructor() : MergedDataSet<T, R> {

    protected val bounds = RectF()
    protected val dataSets = ArrayList<T>()

    public constructor(vararg dataSets: T) : this() {
        this.dataSets.addAll(dataSets)
    }

    override fun getMeasuredWidth(): Int =
        dataSets.maxOfOrNull { it.getMeasuredWidth() } ?: 0

    override fun add(vararg other: T): R {
        other.forEach { dataSet -> dataSet.setBounds(bounds) }
        dataSets.addAll(other)
        return this as R
    }

    override fun remove(vararg other: T): R {
        dataSets.removeAll(other)
        return this as R
    }
}