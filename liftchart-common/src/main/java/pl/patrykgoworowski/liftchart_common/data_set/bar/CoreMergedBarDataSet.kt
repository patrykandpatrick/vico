package pl.patrykgoworowski.liftchart_common.data_set.bar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.BarPathCreator
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.DefaultBarPath
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryCollection
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.extension.getOrDefault
import pl.patrykgoworowski.liftchart_common.extension.getRepeatingOrDefault
import kotlin.math.roundToInt

typealias AnyBarDataSet = CoreBarDataSet<AnyEntry>

open class CoreMergedBarDataSet<T: AnyEntry> public constructor(
) : DataSetRenderer() {

    private val entryCollections = ArrayList<EntryCollection<T>>()
    private val paints = ArrayList<Paint>()
    private val barRect = RectF()
    private val barPath = Path()
    private val heightMap = HashMap<Float, Float>()

    private var maxY: Float = 0f
    private var drawBarWidth = 0f
    private var drawBarSpacing = 0f
    private var drawBarInnerSpacing = 0f

    val barPathCreators = ArrayList<BarPathCreator>()

    var barWidth: Float = DEF_BAR_WIDTH
    var barSpacing: Float = DEF_BAR_SPACING
    var barInnerSpacing: Float = DEF_BAR_INNER_SPACING

    override val bounds: RectF = RectF()

    public var groupMode: GroupMode = GroupMode.Grouped

    public constructor(entryCollections: Collection<EntryCollection<T>>): this() {
        this.entryCollections.addAll(entryCollections)
    }

    public constructor(vararg entryCollections: EntryCollection<T>) : this() {
        this.entryCollections.addAll(entryCollections)
    }

    fun setColors(colors: List<Int>) {
        colors.forEachIndexed { index, color ->
            paints.getOrDefault(index) { Paint() }.color = color
        }
    }

    override fun getMeasuredWidth(): Int {
        maxY = groupMode.calculateMaxY(entryCollections)

        val multiplier = when (groupMode) {
            GroupMode.Stack -> 1
            GroupMode.Grouped -> entryCollections.size
        }
        val length = (entryCollections.maxX - entryCollections.minX) / entryCollections.step
        val segmentWidth = (barWidth * multiplier) + (barInnerSpacing * (multiplier - 1))
        return ((segmentWidth * (length + 1)) + (barSpacing * length)).roundToInt()
    }

    override fun setBounds(bounds: RectF) {
        this.bounds.set(bounds)
        calculateDrawSegmentSpec(bounds)
    }

    override fun draw(
        canvas: Canvas,
        animationOffset: Float
    ) {
        if (entryCollections.isEmpty()) return
        val heightMultiplier = bounds.height() / maxY
        val bottom = bounds.bottom
        val step = entryCollections.step

        var drawingStart: Float

        var height: Float
        var left: Float
        var entryOffset: Float

        entryCollections.forEachIndexed { index, entryCollection ->

            val paint = paints.getRepeatingOrDefault(index) { Paint() }
            val barPathCreator = barPathCreators.getRepeatingOrDefault(index) { DefaultBarPath() }

            drawingStart = getDrawingStart(index)

            entryCollection.entries.forEach { entry ->
                height = entry.y * heightMultiplier
                entryOffset = getSegmentSize() * entry.x / step
                left = drawingStart + entryOffset
                when (groupMode) {
                    GroupMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        barRect.set(
                            left,
                            bottom - (height + cumulatedHeight),
                            left + drawBarWidth,
                            bottom - cumulatedHeight
                        )
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    GroupMode.Grouped -> {
                        barRect.set(left, bottom - height, left + drawBarWidth, bottom)
                    }
                }
                barPath.reset()
                barPathCreator.drawBarPath(canvas, paint, barPath, bounds, barRect, animationOffset, entry)
            }
        }
        heightMap.clear()
    }

    private fun getSegmentSize(): Float = when (groupMode) {
            GroupMode.Stack -> drawBarWidth + drawBarSpacing
            GroupMode.Grouped -> (drawBarWidth * entryCollections.size) +
                    (drawBarInnerSpacing * (entryCollections.size - 1)) + drawBarSpacing
        }

    private fun getDrawingStart(entryCollectionIndex: Int): Float = when (groupMode) {
        GroupMode.Stack -> bounds.left
        GroupMode.Grouped -> bounds.left + ((drawBarWidth + drawBarInnerSpacing) * entryCollectionIndex)
    }

    private fun calculateDrawSegmentSpec(bounds: RectF) {
        val measuredWidth = getMeasuredWidth()
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