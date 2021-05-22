package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.set
import kotlin.math.roundToInt

public open class ColumnDataSetRenderer(
    val column: RectComponent,
    var spacing: Float,
) : DataSetRenderer<SingleEntriesModel> {

    private var drawScale: Float = 1f
    private var isScaleCalculated = false

    private val columnThickness by column::scaledThickness

    override val bounds: RectF = RectF()

    override fun setBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    ) {
        this.bounds.set(left, top, right, bottom)
        isScaleCalculated = false
    }

    override fun draw(canvas: Canvas, model: SingleEntriesModel) {
        calculateDrawSegmentSpecIfNeeded(model)

        val spacing = spacing * drawScale

        val heightMultiplier = bounds.height() / model.maxY
        val bottom = bounds.bottom
        val drawingStart = bounds.left + spacing.half

        model.entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val startX =
                drawingStart + columnThickness.half + (columnThickness + spacing) * (entry.x - model.minX) / model.step
            column.drawVertical(canvas, bottom - height, bottom, startX)
        }
    }

    private fun calculateDrawSegmentSpecIfNeeded(model: SingleEntriesModel) {
        if (isScaleCalculated) return
        val measuredWidth = getMeasuredWidth(model)
        drawScale = if (bounds.width() >= measuredWidth) {
            1f
        } else {
            bounds.width() / measuredWidth
        }
        column.thicknessScale = drawScale
        isScaleCalculated = true
    }

    override fun getMeasuredWidth(model: SingleEntriesModel): Int {
        val length = model.getEntriesLength()
        return ((column.thickness * length) + (spacing * length)).roundToInt()
    }

}