package pl.patrykgoworowski.liftchart_view.data_set

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener

interface ViewDataSetRenderer : BoundsAware {
    fun getMeasuredWidth(): Int
    fun draw(canvas: Canvas): AxisModel?
    fun addListener(listener: UpdateRequestListener)
    fun removeListener(listener: UpdateRequestListener)
    fun getEntriesModel(): EntriesModel
}