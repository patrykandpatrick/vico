package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.extension.PaintModifier
import pl.patrykgoworowski.liftchart_common.data_set.segment.MutableSegmentProperties
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.extension.findClosestPositiveValue
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_common.marker.Marker
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

    private val markerLocationMap = HashMap<Float, Marker.EntryModel>()

    override var columnPaintModifier: PaintModifier? = null
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

    override fun draw(
        canvas: Canvas,
        model: SingleEntriesModel,
        touchPoint: PointF?,
        marker: Marker?
    ) {
        markerLocationMap.clear()
        calculateDrawSegmentSpecIfNeeded(model)

        val heightMultiplier = bounds.height() / model.maxY
        val bottom = bounds.bottom
        val drawingStart = bounds.left + scaledSpacing.half
        val entries = model.entries
        columnPaintModifier?.modifyPaint(column.paint, bounds, 0)

        entries.forEach { entry ->
            val height = entry.y * heightMultiplier
            val top = bottom - height
            val columnCenterX = drawingStart + scaledColumnThickness.half +
                    (scaledColumnThickness + scaledSpacing) * (entry.x - model.minX) / model.step
            if (touchPoint != null && marker != null) {
                markerLocationMap[columnCenterX] = Marker.EntryModel(
                    PointF(columnCenterX, top),
                    entry,
                    column.color,
                )
            }
            column.drawVertical(canvas, top, bottom, columnCenterX)
        }

        if (touchPoint == null || marker == null) return
        getClosestMarkerEntryPositionModel(touchPoint)?.let { markerModel ->
            marker.draw(
                canvas,
                bounds,
                listOf(markerModel),
                entries,
            )
        }
    }

    private fun getClosestMarkerEntryPositionModel(touchPoint: PointF): Marker.EntryModel? {
        return markerLocationMap.keys.findClosestPositiveValue(touchPoint.x)?.let(markerLocationMap::get)
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