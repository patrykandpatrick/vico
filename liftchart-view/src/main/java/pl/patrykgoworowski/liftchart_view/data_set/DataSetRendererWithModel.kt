package pl.patrykgoworowski.liftchart_view.data_set

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener

interface DataSetRendererWithModel<Model: EntriesModel> : DataSetRenderer<Model> {
    fun getMeasuredWidth(): Int
    fun draw(canvas: Canvas)
    fun addListener(listener: UpdateRequestListener)
    fun removeListener(listener: UpdateRequestListener)
    fun getEntriesModel(): Model
    fun getSegmentProperties(): SegmentProperties
}