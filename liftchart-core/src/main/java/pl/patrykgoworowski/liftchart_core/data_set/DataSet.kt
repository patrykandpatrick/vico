package pl.patrykgoworowski.liftchart_core.data_set

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_core.data_set.segment.DrawSegmentSpec


public interface DataSet {
    fun getMeasuredWidth(): Int
    fun draw(canvas: Canvas, bounds: RectF, drawSegmentSpec: DrawSegmentSpec, animationOffset: Float)
}