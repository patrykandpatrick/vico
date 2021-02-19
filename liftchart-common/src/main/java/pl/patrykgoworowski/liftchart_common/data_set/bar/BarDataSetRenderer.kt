package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.MutableAxisModel
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.RectShape
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.Shape
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_COLOR
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_common.extension.setAll
import kotlin.math.abs
import kotlin.math.roundToInt

public open class BarDataSetRenderer<Entry : AnyEntry>(
    color: Int = DEF_COLOR,
    var barWidth: Float = DEF_BAR_WIDTH,
    var barSpacing: Float = DEF_BAR_SPACING,
    var shape: Shape = RectShape()
) : DataSetRenderer<SingleEntriesModel<Entry>> {

    private val barPath = Path()
    private val barRect = RectF()
    private val axisModel = MutableAxisModel()

    private var isScaleCalculated = false
    private var drawBarWidth = 0f
    private var drawBarSpacing = 0f

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var color: Int by paint::color

    override val bounds: RectF = RectF()

    init {
        this.color = color
    }

    override fun setBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    ) {
        this.bounds.set(left, top, right, bottom)
        isScaleCalculated = false
    }

    override fun draw(canvas: Canvas, model: SingleEntriesModel<Entry>) {

        calculateDrawSegmentSpecIfNeeded(model)

        val heightMultiplier = bounds.height() / model.maxY
        val bottom = bounds.bottom
        val drawingStart = bounds.left

        model.entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val startX = drawingStart + (drawBarWidth + drawBarSpacing) * entry.x / model.step
            barRect.set(startX, bottom - height, startX + drawBarWidth, bottom)
            drawBar(canvas, entry, bounds, barRect)
        }
    }

    override fun getAxisModel(model: SingleEntriesModel<Entry>): AxisModel =
        axisModel.apply {
            calculateDrawSegmentSpecIfNeeded(model)
            minX = model.minX
            maxX = model.maxX
            minY = model.minY
            maxY = model.maxY
            xSegmentWidth = drawBarWidth / model.step
            xSegmentSpacing = drawBarSpacing / model.step
            entries.setAll(model.entries)
        }

    private fun drawBar(
        canvas: Canvas,
        entry: AnyEntry,
        bounds: RectF,
        barBounds: RectF,
    ) {
        barPath.reset()
        shape.drawEntryShape(canvas, paint, barPath, bounds, barBounds, entry)
    }

    private fun calculateDrawSegmentSpecIfNeeded(model: SingleEntriesModel<Entry>) {
        if (isScaleCalculated) return
        val measuredWidth = getMeasuredWidth(model)
        if (bounds.width() >= measuredWidth) {
            drawBarWidth = barWidth
            drawBarSpacing = barSpacing
        } else {
            val scale: Float = bounds.width() / measuredWidth
            drawBarWidth = barWidth * scale
            drawBarSpacing = barSpacing * scale
        }
        isScaleCalculated = true
    }

    override fun getMeasuredWidth(model: SingleEntriesModel<Entry>): Int {
        val length = (abs(model.maxX) - abs(model.minX)) / model.step
        return (((barWidth * (length + 1)) + (barSpacing * length)) / 1).roundToInt()
    }

}