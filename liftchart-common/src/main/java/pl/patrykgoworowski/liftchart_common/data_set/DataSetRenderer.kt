package pl.patrykgoworowski.liftchart_common.data_set

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

typealias AnyEntry = DataEntry<*, *>

public abstract class DataSetRenderer {
    protected abstract val bounds: RectF
    public abstract fun getMeasuredWidth(): Int
    public abstract fun setBounds(bounds: RectF)
    public abstract fun draw(canvas: Canvas, animationOffset: Float)
}