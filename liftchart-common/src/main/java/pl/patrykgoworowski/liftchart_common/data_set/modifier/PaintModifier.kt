package pl.patrykgoworowski.liftchart_common.data_set.modifier

import android.graphics.Paint
import android.graphics.RectF

fun interface PaintModifier {
    fun modifyPaint(paint: Paint, bounds: RectF, entryCollectionIndex: Int)
}