package pl.patrykgoworowski.liftchart_common.data_set.renderer

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.marker.Marker

public interface DataSet<in Model> : BoundsAware {

    public var minY: Float?
    public var maxY: Float?
    public var minX: Float?
    public var maxX: Float?

    public var isHorizontalScrollEnabled: Boolean
    public var zoom: Float?
    public val maxScrollAmount: Float

    public fun draw(
        canvas: Canvas,
        model: Model,
        rendererViewState: RendererViewState,
        marker: Marker?,
    )

    public fun getMeasuredWidth(model: Model): Int
    public fun getSegmentProperties(model: Model): SegmentProperties
    public fun setToAxisModel(axisModel: MutableDataSetModel, model: Model)
}