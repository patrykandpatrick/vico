package pl.patrykgoworowski.liftchart_common.data_set

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisModel

interface DataSetRenderer<Model> : BoundsAware {
    fun draw(canvas: Canvas, model: Model): AxisModel?
    fun getMeasuredWidth(model: Model): Int
}