package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

interface AxisRenderer : BoundsAware {

    val position: AxisPosition
    var isLTR: Boolean
    var isVisible: Boolean

    fun draw(canvas: Canvas, model: AxisModel) {
        if (isVisible) {
            onDraw(canvas, model)
        }
    }

    fun onDraw(canvas: Canvas, model: AxisModel)

    fun getAxisPosition(): Position = position.position

    fun getSize(model: EntriesModel): Float

}