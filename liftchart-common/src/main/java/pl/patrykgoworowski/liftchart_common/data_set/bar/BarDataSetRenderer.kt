package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.BarPathCreator
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.DefaultBarPath
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_COLOR
import kotlin.math.abs
import kotlin.math.roundToInt

public open class BarDataSetRenderer<Entry : AnyEntry>(
    color: Int = DEF_COLOR,
    var barWidth: Float = DEF_BAR_WIDTH,
    var barSpacing: Float = DEF_BAR_SPACING,
    var barPathCreator: BarPathCreator = DefaultBarPath()
) {

    private val barPath = Path()
    private val barRect = RectF()
    private val bounds: RectF = RectF()

    private var drawBarWidth = 0f
    private var drawBarSpacing = 0f

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var color: Int by paint::color

    init {
        this.color = color
    }

    fun setBounds(bounds: RectF, model: SingleEntriesModel<Entry>) {
        this.bounds.set(bounds)
        calculateDrawSegmentSpec(model)
    }

    fun draw(canvas: Canvas, model: SingleEntriesModel<Entry>) {
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

    private fun drawBar(
        canvas: Canvas,
        entry: AnyEntry,
        bounds: RectF,
        barBounds: RectF,
    ) {
        barPath.reset()
        barPathCreator.drawBarPath(canvas, paint, barPath, bounds, barBounds, entry)
    }

    private fun calculateDrawSegmentSpec(model: SingleEntriesModel<Entry>) {
        val measuredWidth = getMeasuredWidth(model)
        if (bounds.width() >= measuredWidth) {
            drawBarWidth = barWidth
            drawBarSpacing = barSpacing
        } else {
            val scale: Float = bounds.width() / measuredWidth
            drawBarWidth = barWidth * scale
            drawBarSpacing = barSpacing * scale
        }
    }

    fun getMeasuredWidth(model: SingleEntriesModel<Entry>): Int {
        val length = (abs(model.maxX) - abs(model.minX)) / model.step
        return (((barWidth * (length + 1)) + (barSpacing * length)) / 1).roundToInt()
    }

}