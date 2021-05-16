package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.axis.model.MutableAxisModel
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_common.extension.setAll
import kotlin.math.roundToInt

public open class ColumnDataSetRenderer(
    val column: RectComponent,
    var spacing: Float,
) : DataSetRenderer<SingleEntriesModel> {

    private val axisModel = MutableAxisModel()

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

    override fun getAxisModel(model: SingleEntriesModel): AxisModel =
        axisModel.apply {
            calculateDrawSegmentSpecIfNeeded(model)
            minX = model.minX
            maxX = model.maxX
            minY = model.minY
            maxY = model.maxY
            step = model.step
            xSegmentWidth = columnThickness
            xSegmentSpacing = spacing * drawScale
            entries.setAll(model.entries)
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