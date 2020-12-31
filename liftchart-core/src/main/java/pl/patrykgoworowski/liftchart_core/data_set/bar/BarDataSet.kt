package pl.patrykgoworowski.liftchart_core.data_set.bar

import android.graphics.*
import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_core.data_set.ArrayListEntryManager
import pl.patrykgoworowski.liftchart_core.data_set.DataSet
import pl.patrykgoworowski.liftchart_core.data_set.entry.EntryManager
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_core.delegates.observable
import kotlin.math.abs
import kotlin.math.roundToInt

public open class BarDataSet<T: AnyEntry>(
    entryManager: EntryManager<T> = ArrayListEntryManager()
) : EntryManager<T> by entryManager, DataSet {

    private val bounds: RectF = RectF()
    private val barPath = Path()
    private val barRect = RectF()

    private var drawBarWidth = DEF_BAR_WIDTH
    private var drawBarSpacing = DEF_BAR_SPACING

    var color: Int by observable(Color.MAGENTA) { newColor ->
        paint.color = newColor
    }

    var barWidth: Float = DEF_BAR_WIDTH
    var barSpacing: Float = DEF_BAR_SPACING
    var barPathCreator: BarPathCreator = DefaultBarPath()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = color
    }

    override fun setBounds(bounds: RectF) {
        this.bounds.set(bounds)
        calculateDrawSegmentSpec(bounds)
    }

    override fun draw(
        canvas: Canvas,
        animationOffset: Float
    ) {
        //barDrawSegmentSpec.copyValues(drawSegmentSpec)
        val heightMultiplier = bounds.height() / maxY
        val bottom = bounds.bottom
        val drawingStart = bounds.left

        entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val startX = drawingStart + (drawBarWidth + drawBarSpacing) * entry.x
            barRect.set(startX, bottom - height, startX + drawBarWidth, bottom)
            drawBar(canvas, entry, bounds, barRect, animationOffset)
        }
    }

    public fun drawBar(canvas: Canvas,
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
        val length = abs(maxX) - abs(minY)
        return ((length + 1) * barWidth + (length * barSpacing)).roundToInt()
    }

}