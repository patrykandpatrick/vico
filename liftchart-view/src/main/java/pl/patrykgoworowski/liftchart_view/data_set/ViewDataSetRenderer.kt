package pl.patrykgoworowski.liftchart_view.data_set

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener

interface ViewDataSetRenderer {
    fun getMeasuredWidth(): Int
    fun setBounds(bounds: RectF)
    fun draw(canvas: Canvas)
    fun addListener(listener: UpdateRequestListener)
    fun removeListener(listener: UpdateRequestListener)
}