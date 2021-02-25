package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.axis.model.MutableAxisModel
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_COLOR
import pl.patrykgoworowski.liftchart_common.defaults.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.extension.*
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape
import kotlin.math.roundToInt

open class MergedBarDataSetRenderer<Entry: AnyEntry> public constructor(
    colors: List<Int> = listOf(DEF_COLOR),
    var barWidth: Float = DEF_BAR_WIDTH,
    var barSpacing: Float = DEF_MERGED_BAR_SPACING,
    var barInnerSpacing: Float = DEF_MERGED_BAR_INNER_SPACING,
) : DataSetRenderer<MultiEntriesModel<Entry>> {

    private val paints = ArrayList<Paint>()
    private val barRect = RectF()
    private val barPath = Path()
    private val heightMap = HashMap<Float, Float>()
    private val axisModel = MutableAxisModel()

    private var isScaleCalculated = false
    private var drawBarWidth = 0f
    private var drawBarSpacing = 0f
    private var drawBarInnerSpacing = 0f

    val barPathCreators = ArrayList<Shape>()

    public var groupMode: MergeMode = MergeMode.Grouped

    override val bounds: RectF = RectF()

    init {
        setColors(colors)
    }

    fun setColors(colors: List<Int>) {
        colors.forEachIndexed { index, color ->
            paints.getOrDefault(index) { Paint() }.color = color
        }
    }

    override fun getMeasuredWidth(model: MultiEntriesModel<Entry>): Int {
        val multiplier = groupMode.getWidthMultiplier(model)
        val length = model.getEntriesLength()
        val segmentWidth = (barWidth * multiplier) + (barInnerSpacing * (multiplier - 1))
        return ((segmentWidth * length) + (barSpacing * length)).roundToInt()
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
        model: MultiEntriesModel<Entry>
    ) {
        if (model.entryCollections.isEmpty()) return

        calculateDrawSegmentSpecIfNeeded(model)

        val heightMultiplier = bounds.height() / groupMode.getMaxY(model)
        val bottom = bounds.bottom
        val step = model.step

        var drawingStart: Float

        var height: Float
        var left: Float
        var entryOffset: Float

        val segmentSize = getSegmentSize(model.entryCollections.size)

        model.entryCollections.forEachIndexed { index, entryCollection ->

            val paint = paints.getRepeatingOrDefault(index) { Paint() }
            val barPathCreator = barPathCreators.getRepeatingOrDefault(index) { RectShape() }

            drawingStart = getDrawingStart(index)

            entryCollection.forEach { entry ->
                height = entry.y * heightMultiplier
                entryOffset = (segmentSize + barSpacing) * (entry.x - model.minX) / step
                left = drawingStart + entryOffset
                when (groupMode) {
                    MergeMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        barRect.set(
                            left,
                            (bottom - (height + cumulatedHeight)).roundToInt(),
                            left + drawBarWidth,
                            (bottom - cumulatedHeight).roundToInt()
                        )
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    MergeMode.Grouped -> {
                        barRect.set(left, bottom - height, left + drawBarWidth, bottom)
                    }
                }
                barPath.reset()
                barPathCreator.drawEntryShape(canvas, paint, barPath, bounds, barRect, entry)
            }
        }
        heightMap.clear()
    }

    override fun getAxisModel(model: MultiEntriesModel<Entry>): AxisModel =
        axisModel.apply {
            calculateDrawSegmentSpecIfNeeded(model)
            minX = model.minX
            maxX = model.maxX
            minY = model.minY
            maxY = groupMode.getMaxY(model)
            step = model.step
            xSegmentWidth = getSegmentSize(model.entryCollections.size)
            xSegmentSpacing = drawBarSpacing
            entries.setAll(model.mergedEntries)
        }

    private fun getSegmentSize(entryCollectionSize: Int): Float = when (groupMode) {
            MergeMode.Stack -> drawBarWidth
            MergeMode.Grouped -> (drawBarWidth * entryCollectionSize) +
                    (drawBarInnerSpacing * (entryCollectionSize - 1))
        }

    private fun getDrawingStart(entryCollectionIndex: Int): Float {
        val baseLeft = bounds.left + drawBarSpacing.half
        return when (groupMode) {
            MergeMode.Stack -> baseLeft
            MergeMode.Grouped -> baseLeft + ((drawBarWidth + drawBarInnerSpacing) * entryCollectionIndex)
        }
    }

    private fun calculateDrawSegmentSpecIfNeeded(model: MultiEntriesModel<Entry>) {
        if (isScaleCalculated) return
        val measuredWidth = getMeasuredWidth(model)
        if (bounds.width() >= measuredWidth) {
            drawBarWidth = barWidth
            drawBarSpacing = barSpacing
            drawBarInnerSpacing = barInnerSpacing
        } else {
            val scale: Float = bounds.width() / measuredWidth
            drawBarWidth = barWidth * scale
            drawBarSpacing = barSpacing * scale
            drawBarInnerSpacing = barInnerSpacing * scale
        }
        isScaleCalculated = true
    }

}