package pl.patrykgoworowski.liftchart_view.data_set.common

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSet
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener

class ViewDataSet <Model: EntryModel>(
    private val dataSet: DataSet<Model>,
    model: Model,
) : DataSetWithModel<Model>, DataSet<Model> by dataSet {

    private val listeners = ArrayList<UpdateRequestListener>()

    var model: Model = model
        set(value) {
            field = value
            listeners.forEach { it() }
        }

    override fun getEntriesModel(): Model = model

    override fun getSegmentProperties(): SegmentProperties = dataSet.getSegmentProperties(model)

    override fun setToAxisModel(axisModel: MutableDataSetModel) {
        dataSet.setToAxisModel(axisModel, model)
    }

    override fun draw(canvas: Canvas, rendererViewState: RendererViewState, marker: Marker?) {
        dataSet.draw(canvas, model, rendererViewState, marker)
    }

    override fun addListener(listener: UpdateRequestListener) {
        listeners += listener
    }

    override fun removeListener(listener: UpdateRequestListener) {
        listeners -= listener
    }

}