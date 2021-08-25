package pl.patrykgoworowski.liftchart_common.data_set.renderer

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.marker.Marker

interface DataSetRenderer<in Model : EntriesModel> : BoundsAware {

    var minY: Float?
    var maxY: Float?
    var minX: Float?
    var maxX: Float?

    var isHorizontalScrollEnabled: Boolean
    var zoom: Float?
    val maxScrollAmount: Float

    fun draw(
        canvas: Canvas,
        model: Model,
        rendererViewState: RendererViewState,
        marker: Marker?,
    )

    fun getMeasuredWidth(model: Model): Int
    fun getSegmentProperties(model: Model): SegmentProperties
    fun setToAxisModel(axisModel: MutableDataSetModel, model: Model)
}