package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.MutableSegmentProperties
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.set
import kotlin.math.roundToInt

public open class ColumnDataSetRenderer(
    val column: RectComponent,
    var spacing: Float,
) : DataSetRenderer<SingleEntriesModel> {

    private var drawScale: Float = 1f
    private var isScaleCalculated = false
    private var scaledSpacing: Float = spacing

    private val scaledColumnThickness by column::scaledThickness
    private val segmentProperties = MutableSegmentProperties()

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

    override fun draw(canvas: Canvas, model: SingleEntriesModel, touchPoint: PointF?) {
        calculateDrawSegmentSpecIfNeeded(model)

        val heightMultiplier = bounds.height() / model.maxY
        val bottom = bounds.bottom
        val drawingStart = bounds.left + scaledSpacing.half

        model.entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val startX = drawingStart + scaledColumnThickness.half +
                    (scaledColumnThickness + scaledSpacing) * (entry.x - model.minX) / model.step
            column.drawVertical(canvas, bottom - height, bottom, startX)
        }
    }

    private fun calculateDrawSegmentSpecIfNeeded(model: SingleEntriesModel) {
        if (isScaleCalculated) return
        val measuredWidth = getMeasuredWidth(model)
        drawScale = minOf(bounds.width() / measuredWidth, 1f)
        column.thicknessScale = drawScale
        scaledSpacing = spacing * drawScale
        isScaleCalculated = true
    }

    override fun getSegmentProperties(model: SingleEntriesModel): SegmentProperties {
        calculateDrawSegmentSpecIfNeeded(model)
        return segmentProperties.apply {
            contentWidth = scaledColumnThickness
            marginWidth = scaledSpacing
        }
    }

    override fun getMeasuredWidth(model: SingleEntriesModel): Int {
        val length = model.getEntriesLength()
        return ((column.thickness * length) + (spacing * length)).roundToInt()
    }

}