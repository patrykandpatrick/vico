package pl.patrykgoworowski.liftchart_core.data_set

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_core.entry.DataEntry

typealias AnyEntry = DataEntry<*, *>

public interface DataSet {
    fun getMeasuredWidth(): Int
    fun setBounds(bounds: RectF)
    fun draw(canvas: Canvas, animationOffset: Float)
}