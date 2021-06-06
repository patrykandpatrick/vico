package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.constants.ERR_COLUMN_LIST_EMPTY
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.MutableSegmentProperties
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.extension.getRepeating
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.round
import pl.patrykgoworowski.liftchart_common.extension.set
import kotlin.math.roundToInt

open class MergedColumnDataSetRenderer public constructor(
    public val columns: List<RectComponent>,
    public var spacing: Float = DEF_MERGED_BAR_SPACING,
    public var innerSpacing: Float = DEF_MERGED_BAR_INNER_SPACING,
    public var mergeMode: MergeMode = MergeMode.Grouped
) : DataSetRenderer<MultiEntriesModel> {

    private val heightMap = HashMap<Float, Float>()
    override val bounds: RectF = RectF()

    private var drawScale: Float = 1f
    private var isScaleCalculated = false
    private var scaledSpacing = spacing
    private var scaledInnerSpacing = innerSpacing

    private val segmentProperties = MutableSegmentProperties()

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
        model: MultiEntriesModel
    ) {
        if (model.entryCollections.isEmpty()) return

        calculateDrawSegmentSpecIfNeeded(model)

        val heightMultiplier = bounds.height() / mergeMode.getMaxY(model)
        val bottom = bounds.bottom
        val step = model.step

        var drawingStart: Float

        var height: Float
        var columnCenterX: Float
        var entryOffset: Float
        var column: RectComponent
        var columnTop: Float
        var columnBottom: Float

        val segmentSize = getSegmentSize(model.entryCollections.size)

        model.entryCollections.forEachIndexed { index, entryCollection ->

            drawingStart = getDrawingStart(index)

            entryCollection.forEach { entry ->
                height = entry.y * heightMultiplier
                entryOffset = (segmentSize + scaledSpacing) * (entry.x - model.minX) / step
                columnCenterX = drawingStart + entryOffset
                column = columns.getRepeating(index)

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        columnTop = (bottom - (height + cumulatedHeight)).round
                        columnBottom = (bottom - cumulatedHeight).round
                        columnCenterX += segmentSize.half

                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    MergeMode.Grouped -> {
                        columnTop = bottom - height
                        columnBottom = bottom
                        columnCenterX += column.scaledThickness.half
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
            MergeMode.Stack -> columns.maxOf { it.thickness }
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
        drawScale = minOf(bounds.width() / measuredWidth, 1f)
        columns.forEach { column -> column.thicknessScale = drawScale }
        scaledSpacing = spacing * drawScale
        scaledInnerSpacing = innerSpacing * drawScale
        isScaleCalculated = true
    }

}