package pl.patrykgoworowski.liftchart_core.data_set.bar

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_core.data_set.mergeable.AbstractMergedDataSet
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_core.defaults.DEF_BAR_WIDTH
import kotlin.math.roundToInt


class MergedBarDataSet public constructor() :
    AbstractMergedDataSet<BarDataSet<AnyEntry>, MergedBarDataSet>() {

    private var maxY: Float = 0f
    private val barRect = RectF()

    private var drawBarWidth = DEF_BAR_WIDTH
    private var drawBarSpacing = DEF_BAR_SPACING
    private var drawBarInnerSpacing = DEF_BAR_SPACING

    var barWidth: Float = DEF_BAR_WIDTH
    var barSpacing: Float = DEF_BAR_SPACING
    var barInnerSpacing: Float = DEF_BAR_INNER_SPACING

    public var groupMode: GroupMode = GroupMode.Grouped

    public constructor(vararg dataSets: BarDataSet<AnyEntry>) : this() {
        this.dataSets.addAll(dataSets)
    }

    override fun getMeasuredWidth(): Int {
        maxY = groupMode.calculateMaxY(dataSets)

        val multiplier = when (groupMode) {
            GroupMode.Overlay,
            GroupMode.Stack, -> 1
            GroupMode.Grouped -> dataSets.size
        }
        val length = (dataSets.maxX - dataSets.minX) / dataSets.step
        val segmentWidth = (barWidth * multiplier) + (barInnerSpacing * (multiplier - 1))
        return ((segmentWidth * (length + 1)) + (barSpacing * length)).roundToInt()
    }

    override fun setBounds(bounds: RectF) {
        this.bounds.set(bounds)
        dataSets.forEach { dataSet -> dataSet.setBounds(bounds) }
        calculateDrawSegmentSpec(bounds)
    }

    override fun draw(
        canvas: Canvas,
        animationOffset: Float
    ) {
        if (dataSets.isEmpty()) return
        val heightMultiplier = bounds.height() / maxY
        val bottom = bounds.bottom
        val step = dataSets.step

        var drawingStart: Float

        var height: Float
        var startX: Float
        var entryOffset: Float

        when (groupMode) {
            GroupMode.Overlay -> dataSets.forEach { barDataSet ->
                barDataSet.draw(canvas, animationOffset)
            }
            GroupMode.Stack -> {
                val heightMap = HashMap<Float, Float>()
                dataSets.forEachIndexed { index, dataSet ->
                    drawingStart = bounds.left
                    dataSet.entries.forEach { entry ->
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        height = entry.y * heightMultiplier
                        entryOffset = (drawBarWidth + drawBarSpacing) * entry.x / step
                        startX = drawingStart + entryOffset
                        barRect.set(
                            startX,
                            bottom - (height + cumulatedHeight),
                            startX + drawBarWidth,
                            bottom - cumulatedHeight
                        )
                        dataSet.drawBar(canvas, entry, bounds, barRect, animationOffset)
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                }
            }
            GroupMode.Grouped -> {
                val segmentSize = (drawBarWidth * dataSets.size) +
                        (drawBarInnerSpacing * (dataSets.size - 1)) + drawBarSpacing

                dataSets.forEachIndexed { index, dataSet ->
                    drawingStart = bounds.left + ((drawBarWidth + drawBarInnerSpacing) * index)
                    dataSet.entries.forEach { entry ->
                        height = entry.y * heightMultiplier
                        entryOffset = (segmentSize) * entry.x
                        startX = drawingStart + entryOffset
                        barRect.set(startX, bottom - height, startX + drawBarWidth, bottom)
                        dataSet.drawBar(canvas, entry, bounds, barRect, animationOffset)
                    }
                }
            }
        }
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

    override fun add(vararg other: BarDataSet<AnyEntry>): MergedBarDataSet {
        dataSets.addAll(other)
        maxY = groupMode.calculateMaxY(dataSets)
        return this
    }

    override fun remove(vararg other: BarDataSet<AnyEntry>): MergedBarDataSet {
        dataSets.removeAll(other)
        maxY = groupMode.calculateMaxY(dataSets)
        return this
    }

    enum class GroupMode {
        Overlay, Stack, Grouped;

        fun calculateMaxY(dataSets: Collection<BarDataSet<AnyEntry>>): Float = when (this) {
            Overlay, Grouped -> dataSets.maxY
            Stack -> {
                dataSets.fold(HashMap<Float, Float>()) { map, dataSet ->
                    dataSet.entries.forEach { entry ->
                        map[entry.x] = map.getOrElse(entry.x) { 0f } + entry.y
                    }
                    map
                }.values.maxOrNull() ?: 0f
            }
        }

    }
}