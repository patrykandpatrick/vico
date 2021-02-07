package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.BarPathCreator
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.DefaultBarPath
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_COLOR
import pl.patrykgoworowski.liftchart_common.defaults.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.extension.getOrDefault
import pl.patrykgoworowski.liftchart_common.extension.getRepeatingOrDefault
import pl.patrykgoworowski.liftchart_common.extension.set
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
    private val bounds: RectF = RectF()

    private var drawBarWidth = 0f
    private var drawBarSpacing = 0f
    private var drawBarInnerSpacing = 0f

    val barPathCreators = ArrayList<BarPathCreator>()

    public var groupMode: MergeMode = MergeMode.Grouped

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
        val length = (model.maxX - model.minX) / model.step
        val segmentWidth = (barWidth * multiplier) + (barInnerSpacing * (multiplier - 1))
        return ((segmentWidth * (length + 1)) + (barSpacing * length)).roundToInt()
    }

    override fun setBounds(bounds: RectF, model: MultiEntriesModel<Entry>) {
        this.bounds.set(bounds)
        calculateDrawSegmentSpec(model)
    }

    override fun draw(
        canvas: Canvas,
        model: MultiEntriesModel<Entry>
    ) {
        if (model.entryCollections.isEmpty()) return
        val heightMultiplier = bounds.height() / groupMode.getMaxY(model)
        val bottom = bounds.bottom
        val step = model.step

        var drawingStart: Float

        var height: Float
        var left: Float
        var entryOffset: Float

        model.entryCollections.forEachIndexed { index, entryCollection ->

            val paint = paints.getRepeatingOrDefault(index) { Paint() }
            val barPathCreator = barPathCreators.getRepeatingOrDefault(index) { DefaultBarPath() }

            drawingStart = getDrawingStart(index)

            entryCollection.forEach { entry ->
                height = entry.y * heightMultiplier
                entryOffset = getSegmentSize(model.entryCollections.size) * entry.x / step
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
                barPathCreator.drawBarPath(canvas, paint, barPath, bounds, barRect, entry)
            }
        }
        heightMap.clear()
    }

    private fun getSegmentSize(entryCollectionSize: Int): Float = when (groupMode) {
            MergeMode.Stack -> drawBarWidth + drawBarSpacing
            MergeMode.Grouped -> (drawBarWidth * entryCollectionSize) +
                    (drawBarInnerSpacing * (entryCollectionSize - 1)) + drawBarSpacing
        }

    private fun getDrawingStart(entryCollectionIndex: Int): Float = when (groupMode) {
        MergeMode.Stack -> bounds.left
        MergeMode.Grouped -> bounds.left + ((drawBarWidth + drawBarInnerSpacing) * entryCollectionIndex)
    }

    private fun calculateDrawSegmentSpec(model: MultiEntriesModel<Entry>) {
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
    }

}