package pl.patrykgoworowski.liftchart_core.data_set.default

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_core.data_set.DataSet
import pl.patrykgoworowski.liftchart_core.data_set.mergeable.AbstractMergedDataSet

class DefaultMergedDataSet(): AbstractMergedDataSet<DataSet, DefaultMergedDataSet>() {

    public constructor(vararg dataSets: DataSet) : this() {
        this.dataSets.addAll(dataSets)
    }

    override fun setBounds(bounds: RectF) {
        this.bounds.set(bounds)
        dataSets.forEach { dataSet -> dataSet.setBounds(bounds) }
    }

    override fun draw(
        canvas: Canvas,
        animationOffset: Float
    ) {
        dataSets.forEach { dataSet ->
            dataSet.draw(canvas, animationOffset)
        }
    }

}