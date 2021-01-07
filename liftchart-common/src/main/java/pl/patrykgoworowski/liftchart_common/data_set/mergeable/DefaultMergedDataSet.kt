package pl.patrykgoworowski.liftchart_common.data_set.mergeable

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer

class DefaultMergedDataSet(): AbstractMergedDataSet<DataSetRenderer, DefaultMergedDataSet>() {

    public constructor(vararg dataSets: DataSetRenderer) : this() {
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