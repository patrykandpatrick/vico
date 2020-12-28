package pl.patrykgoworowski.liftchart_core.data_set.bar

import android.graphics.*
import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_core.data_set.ArrayListEntryManager
import pl.patrykgoworowski.liftchart_core.data_set.DataSet
import pl.patrykgoworowski.liftchart_core.data_set.EntryManager
import pl.patrykgoworowski.liftchart_core.data_set.segment.DrawSegmentSpec
import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec
import pl.patrykgoworowski.liftchart_core.delegates.observable
import kotlin.math.roundToInt

class BarDataSet<T: AnyEntry>(
    entryManager: EntryManager<T> = ArrayListEntryManager()
) : EntryManager<T> by entryManager, DataSet {

    private val barPath = Path()
    private val barRect = RectF()

    private var barDrawSegmentSpec = BarDrawSegmentSpec()

    var color: Int by observable(Color.MAGENTA) { newColor ->
        paint.color = newColor
    }

    var segmentSpec: SegmentSpec = SegmentSpec()
    var barPathCreator: BarPathCreator = DefaultBarPath()

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)


    init {
        paint.color = color
    }

    override fun draw(
        canvas: Canvas,
        bounds: RectF,
        drawSegmentSpec: DrawSegmentSpec,
        animationOffset: Float
    ) {
        barDrawSegmentSpec.copyValues(drawSegmentSpec)
        calculateDrawSegmentSpec(bounds)
        draw(canvas, bounds, barDrawSegmentSpec, animationOffset)
    }

    public fun draw(
        canvas: Canvas,
        bounds: RectF,
        drawSegmentSpec: BarDrawSegmentSpec,
        animationOffset: Float,
    ) {
        val heightMultiplier = bounds.height() / maxY
        val bottom = bounds.bottom
        val drawingStart = bounds.left + drawSegmentSpec.startMargin

        entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val startX = drawingStart + (drawSegmentSpec.width + drawSegmentSpec.spacing) * entry.x
            barPath.reset()
            barRect.set(startX, bottom - height, startX + drawSegmentSpec.width, bottom)
            barPathCreator.drawBarPath(canvas, paint, barPath, bounds, barRect, animationOffset, entry)
        }
    }

    private fun calculateDrawSegmentSpec(bounds: RectF) {
        val measuredWidth = getMeasuredWidth()
        if (bounds.width() >= measuredWidth) {
            barDrawSegmentSpec.width = segmentSpec.width
            barDrawSegmentSpec.spacing = segmentSpec.spacing
        } else {
            val widthScale: Float = bounds.width() / measuredWidth
            barDrawSegmentSpec.width = segmentSpec.width * widthScale
            barDrawSegmentSpec.spacing = segmentSpec.spacing * widthScale
        }
    }

    override fun getMeasuredWidth(): Int {
        return ((maxX + 1) * segmentSpec.width + (maxX * segmentSpec.spacing)
            .coerceAtLeast(0f))
            .roundToInt()
    }

    public data class BarDrawSegmentSpec(
        var startMargin: Float = 0f,
        var endMargin: Float = 0f,
        override var width: Float = 0f,
        override var spacing: Float = 0f
    ) : SegmentSpec {

        public fun copyValues(spec: DrawSegmentSpec) {
            startMargin = spec.startMargin
            endMargin = spec.endMargin
            width = spec.width
            spacing = spec.spacing
        }

    }

}