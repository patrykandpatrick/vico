package pl.patrykgoworowski.liftchart_view.data_set.common

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener

interface DataSetWithModel<Model : EntriesModel> : DataSet<Model> {
    fun draw(
        canvas: Canvas,
        rendererViewState: RendererViewState,
        marker: Marker?,
    )

    fun addListener(listener: UpdateRequestListener)
    fun removeListener(listener: UpdateRequestListener)
    fun getEntriesModel(): Model
    fun getSegmentProperties(): SegmentProperties
    fun setToAxisModel(axisModel: MutableDataSetModel)
}