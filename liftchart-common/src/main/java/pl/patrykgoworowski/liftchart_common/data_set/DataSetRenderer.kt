package pl.patrykgoworowski.liftchart_common.data_set

import android.graphics.Canvas
import android.graphics.RectF

interface DataSetRenderer<Model> {
    fun setBounds(bounds: RectF, model: Model)
    fun draw(canvas: Canvas, model: Model)
    fun getMeasuredWidth(model: Model): Int
}