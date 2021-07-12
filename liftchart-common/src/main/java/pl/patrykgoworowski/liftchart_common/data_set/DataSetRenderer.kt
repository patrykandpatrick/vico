package pl.patrykgoworowski.liftchart_common.data_set

import android.graphics.Canvas
import android.graphics.PointF
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.modifier.PaintModifier
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.marker.Marker

public interface DataSetRenderer<in Model: EntriesModel> : BoundsAware {
    public var columnPaintModifier: PaintModifier?
    public fun draw(canvas: Canvas, model: Model, touchPoint: PointF?, marker: Marker?)
    public fun getMeasuredWidth(model: Model): Int
    public fun getSegmentProperties(model: Model): SegmentProperties
}