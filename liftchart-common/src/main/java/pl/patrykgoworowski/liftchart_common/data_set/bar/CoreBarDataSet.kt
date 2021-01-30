package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.Color.MAGENTA
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.BarPathCreator
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.DefaultBarPath
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import kotlin.math.abs
import kotlin.math.roundToInt

public open class CoreBarDataSet<T: AnyEntry> (
    entryCollection: EntryCollection<T>
) :  DataSetRenderer() {

    override val bounds: RectF = RectF()
    private val barPath = Path()
    private val barRect = RectF()

    private val entries by entryCollection::entries
    private val minX by entryCollection::minX
    private val maxX by entryCollection::maxX
    private val maxY by entryCollection::maxY
    private val step by entryCollection::step

    private var drawBarWidth = 0f
    private var drawBarSpacing = 0f

    var barWidth: Float = 0f
    var barSpacing: Float = 0f
    var barPathCreator: BarPathCreator = DefaultBarPath()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var color: Int by paint::color

    init {
        paint.color = MAGENTA
    }

    override fun setBounds(bounds: RectF) {
        if (this.bounds == bounds) return
        this.bounds.set(bounds)
        calculateDrawSegmentSpec(bounds)
    }

    override fun draw(
        canvas: Canvas,
        animationOffset: Float
    ) {
        val heightMultiplier = bounds.height() / maxY
        val bottom = bounds.bottom
        val drawingStart = bounds.left

        entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val startX = drawingStart + (drawBarWidth + drawBarSpacing) * entry.x / step
            barRect.set(startX, bottom - height, startX + drawBarWidth, bottom)
            drawBar(canvas, entry, bounds, barRect, animationOffset)
        }
    }

    private fun drawBar(canvas: Canvas,
                entry: AnyEntry,
                bounds: RectF,
                barBounds: RectF,
                animationOffset: Float) {
        barPath.reset()
        barPathCreator.drawBarPath(canvas, paint, barPath, bounds, barBounds, animationOffset, entry)
    }

    private fun calculateDrawSegmentSpec(bounds: RectF) {
        val measuredWidth = getMeasuredWidth()
        if (bounds.width() >= measuredWidth) {
            drawBarWidth = barWidth
            drawBarSpacing = barSpacing
        } else {
            val scale: Float = bounds.width() / measuredWidth
            drawBarWidth = barWidth * scale
            drawBarSpacing = barSpacing * scale
        }
    }

    override fun getMeasuredWidth(): Int {
        val length = (abs(maxX) - abs(minX)) / step
        return (((barWidth * (length + 1)) + (barSpacing * length)) / 1).roundToInt()
    }

}