package pl.patrykgoworowski.liftchart_common.data_set.mergeable

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer

abstract class AbstractMergedDataSet<T: DataSetRenderer, R: MergedDataSet<T>> public constructor() :
    MergedDataSet<T>() {

    override val bounds = RectF()
    protected val dataSets = ArrayList<T>()

    public constructor(vararg dataSets: T) : this() {
        this.dataSets.addAll(dataSets)
    }

    override fun getMeasuredWidth(): Int =
        dataSets.maxOfOrNull { it.getMeasuredWidth() } ?: 0

    override fun add(vararg other: T) {
        other.forEach { dataSet -> dataSet.setBounds(bounds) }
        dataSets.addAll(other)
    }

    override fun remove(vararg other: T) {
        dataSets.removeAll(other)
    }
}