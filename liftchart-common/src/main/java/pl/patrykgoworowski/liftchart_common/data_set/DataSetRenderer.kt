package pl.patrykgoworowski.liftchart_common.data_set

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties

interface DataSetRenderer<in Model: EntriesModel> : BoundsAware {
    fun draw(canvas: Canvas, model: Model)
    fun getMeasuredWidth(model: Model): Int
    fun getSegmentProperties(model: Model): SegmentProperties
}