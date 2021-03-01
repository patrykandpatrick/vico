package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface AxisRenderer : BoundsAware {

    val position: AxisPosition
    val dataSetBounds: RectF
    val axisThickness: Float

    var label: TextComponent
    var axis: RectComponent
    var tick: TickComponent
    var guideline: GuidelineComponent
    var isLTR: Boolean
    var isVisible: Boolean
    var valueFormatter: AxisValueFormatter

    fun draw(canvas: Canvas, model: AxisModel) {
        if (isVisible) {
            onDraw(canvas, model)
        }
    }

    fun onDraw(canvas: Canvas, model: AxisModel)

    fun getAxisPosition(): Position = position.position

    fun getSize(model: EntriesModel): Float

    fun setDataSetBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    )

    fun setDataSetBounds(bounds: RectF) =
        setDataSetBounds(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )

}