package pl.patrykgoworowski.liftchart_core.data_set

import android.graphics.*
import pl.patrykgoworowski.liftchart_core.data_set.segment.BarSegmentSpec
import pl.patrykgoworowski.liftchart_core.data_set.segment.DefaultBarSegmentSpec

class BarDataSet(
    segmentSpec: BarSegmentSpec = DefaultBarSegmentSpec(),
    entries: ArrayList<AnyEntry> = ArrayList(),
    color: Int = Color.MAGENTA,
) : BaseDataSet<BarSegmentSpec, ArrayList<AnyEntry>>(
    color,
    segmentSpec,
    ArrayListEntryManager()
) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    private val barPath = Path()

    init {
        setEntries(entries)
    }

    override fun draw(canvas: Canvas, bounds: RectF, segmentWidth: Float, animationOffset: Float) {
        val heightMultiplier = bounds.height() / maxY
        val bottom = bounds.bottom

        entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val startX = bounds.left + (segmentWidth + segmentSpec.spacing) * entry.x

            barPath.reset()
            barPath.moveTo(startX, bottom)
            barPath.lineTo(startX, bottom - height)
            barPath.lineTo(startX + segmentWidth, bottom - height)
            barPath.lineTo(startX + segmentWidth, bottom)
            barPath.close()
            canvas.drawPath(barPath, paint)
        }
    }
}