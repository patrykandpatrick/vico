package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.model.MutableDataSetModel
import pl.patrykgoworowski.liftchart_common.component.LineComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.constants.ERR_COLUMN_LIST_EMPTY
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.modifier.PaintModifier
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.MutableSegmentProperties
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.extension.*
import pl.patrykgoworowski.liftchart_common.marker.Marker
import kotlin.math.roundToInt

open class ColumnDataSetRenderer public constructor(
    public val columns: List<LineComponent>,
    public var spacing: Float = DEF_MERGED_BAR_SPACING,
    public var innerSpacing: Float = DEF_MERGED_BAR_INNER_SPACING,
    public var mergeMode: MergeMode = MergeMode.Grouped
) : DataSetRenderer<MultiEntriesModel> {

    private val heightMap = HashMap<Float, Float>()
    override val bounds: RectF = RectF()

    override var minY: Float? = null
    override var maxY: Float? = null
    override var minX: Float? = null
    override var maxX: Float? = null

    override var columnPaintModifier: PaintModifier? = null

    override var isHorizontalScrollEnabled: Boolean = false
    override var maxScrollAmount: Float = 0f

    private var drawScale: Float = 1f
    private var isScaleCalculated = false
    private var scaledSpacing = spacing
    private var scaledInnerSpacing = innerSpacing

    private val segmentProperties = MutableSegmentProperties()

    private val markerLocationMap = HashMap<Float, ArrayList<Marker.EntryModel>>()

    init {
        if (columns.isEmpty()) throw IllegalStateException(ERR_COLUMN_LIST_EMPTY)
    }

    override fun getMeasuredWidth(model: MultiEntriesModel): Int {
        val length = model.getEntriesLength()
        val segmentWidth = getSegmentSize(model.entryCollections.size, false)
        return ((segmentWidth * length) + (spacing * length)).roundToInt()
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

    override fun draw(
        canvas: Canvas,
        model: MultiEntriesModel,
        rendererViewState: RendererViewState,
        marker: Marker?
    ) {
        markerLocationMap.clear()
        if (model.entryCollections.isEmpty()) return
        val (touchPoint, scrollX) = rendererViewState

        val clipRestoreCount = canvas.save()
        canvas.clipRect(bounds)

        calculateDrawSegmentSpecIfNeeded(model)

        val minYorZero = this.minY ?: 0f
        val minX = minX ?: model.minX
        val maxX = maxX ?: model.maxX

        val heightMultiplier = bounds.height() / ((maxY ?: mergeMode.getMaxY(model)) - minYorZero)
        val bottom = bounds.bottom
        val step = model.step

        var drawingStart: Float

        var height: Float
        var columnCenterX: Float
        var entryOffset: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val bottomCompensation = if (minYorZero < 0f) (minYorZero * heightMultiplier) else 0f

        val segmentSize = getSegmentSize(model.entryCollections.size)

        model.entryCollections.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            columnPaintModifier?.modifyPaint(column.paint, bounds, index)
            drawingStart = getDrawingStart(index) - scrollX

            entryCollection.forEach { entry ->
                if (entry.x !in minX..maxX) return@forEach
                height = entry.y * heightMultiplier
                entryOffset = (segmentSize + scaledSpacing) * (entry.x - model.minX) / step
                columnCenterX = drawingStart + entryOffset

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        columnBottom = (bottom + bottomCompensation - cumulatedHeight)
                            .between(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += segmentSize.half
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    MergeMode.Grouped -> {
                        columnBottom = (bottom + bottomCompensation)
                            .between(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += column.scaledThickness.half
                    }
                }

                if (!column.intersectsVertical(columnTop, columnBottom, columnCenterX, bounds)) {
                    return@forEach
                }

                if (touchPoint != null && marker != null) {
                    markerLocationMap.updateList(columnCenterX, entryCollection.size) {
                        add(
                            Marker.EntryModel(
                                PointF(
                                    columnCenterX,
                                    columnTop.between(bounds.top, bounds.bottom)
                                ),
                                entry,
                                column.color,
                            )
                        )
                    }
                }

                column.drawVertical(
                    canvas = canvas,
                    top = columnTop,
                    bottom = columnBottom,
                    centerX = columnCenterX
                )
            }
        }

        heightMap.clear()

        canvas.restoreToCount(clipRestoreCount)

        if (touchPoint == null || marker == null) return
        getClosestMarkerEntryPositionModel(touchPoint)?.let { markerModel ->
            marker.draw(
                canvas,
                bounds,
                markerModel,
                model.entries,
            )
        }
    }

    override fun setToAxisModel(axisModel: MutableDataSetModel, model: MultiEntriesModel) {
        axisModel.minY = minY ?: axisModel.minY
        axisModel.maxY = maxY ?: mergeMode.getMaxY(model)
        axisModel.minX = minX ?: axisModel.minX
        axisModel.maxX = maxX ?: axisModel.maxX
    }

    private fun getClosestMarkerEntryPositionModel(touchPoint: PointF): List<Marker.EntryModel>? {
        return markerLocationMap.keys.findClosestPositiveValue(touchPoint.x)
            ?.let(markerLocationMap::get)
    }

    override fun getSegmentProperties(model: MultiEntriesModel): SegmentProperties {
        calculateDrawSegmentSpecIfNeeded(model)
        return segmentProperties.apply {
            contentWidth = getSegmentSize(entryCollectionSize = model.entryCollections.size)
            marginWidth = scaledSpacing
        }
    }

    private fun getSegmentSize(entryCollectionSize: Int, scaled: Boolean = true): Float =
        when (mergeMode) {
            MergeMode.Stack -> columns.maxOf { if (scaled) it.scaledThickness else it.thickness }
            MergeMode.Grouped -> {
                val innerSpacing = if (scaled) scaledInnerSpacing else innerSpacing
                getCumulatedThickness(entryCollectionSize, scaled) +
                        (innerSpacing * (entryCollectionSize - 1))
            }
        }

    private fun getDrawingStart(entryCollectionIndex: Int): Float {
        val baseLeft = bounds.left + (spacing.half * drawScale)
        return when (mergeMode) {
            MergeMode.Stack -> baseLeft
            MergeMode.Grouped -> baseLeft + (getCumulatedThickness(entryCollectionIndex, true)
                    + (scaledInnerSpacing * entryCollectionIndex))
        }
    }

    private fun getCumulatedThickness(
        count: Int,
        scaled: Boolean,
    ): Float {
        var thickness = 0f
        for (i in 0 until count) {
            thickness += if (scaled) columns.getRepeating(i).scaledThickness
            else columns.getRepeating(i).thickness
        }
        return thickness
    }

    private fun calculateDrawSegmentSpecIfNeeded(model: MultiEntriesModel) {
        if (isScaleCalculated) return
        val measuredWidth = getMeasuredWidth(model)
        if (isHorizontalScrollEnabled) {
            maxScrollAmount = maxOf(0f, measuredWidth - bounds.width())
            drawScale = 1f
        } else {
            maxScrollAmount = 0f
            drawScale = minOf(bounds.width() / measuredWidth, 1f)
        }
        columns.forEach { column -> column.thicknessScale = drawScale }
        scaledSpacing = spacing * drawScale
        scaledInnerSpacing = innerSpacing * drawScale
        isScaleCalculated = true
    }

}