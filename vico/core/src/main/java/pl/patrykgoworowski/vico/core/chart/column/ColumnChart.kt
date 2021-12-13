/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.chart.column

import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.axis.model.MutableChartModel
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.chart.forEachIn
import pl.patrykgoworowski.vico.core.chart.put
import pl.patrykgoworowski.vico.core.chart.BaseChart
import pl.patrykgoworowski.vico.core.chart.segment.MutableSegmentProperties
import pl.patrykgoworowski.vico.core.chart.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.entry.ChartEntry
import pl.patrykgoworowski.vico.core.extension.between
import pl.patrykgoworowski.vico.core.extension.getRepeating
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.inClip
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.marker.Marker
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

public open class ColumnChart(
    public var columns: List<LineComponent>,
    public var spacingDp: Float = Dimens.COLUMN_OUTSIDE_SPACING,
    public var innerSpacingDp: Float = Dimens.COLUMN_INSIDE_SPACING,
    public var mergeMode: MergeMode = MergeMode.Grouped
) : BaseChart<ChartEntryModel>() {

    public constructor(
        column: LineComponent,
        spacingDp: Float = Dimens.COLUMN_OUTSIDE_SPACING,
    ) : this(columns = listOf(column), spacingDp = spacingDp)

    public constructor() : this(emptyList())

    private val heightMap = HashMap<Float, Float>()
    private val segmentProperties = MutableSegmentProperties()

    override val markerLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    override fun getMeasuredWidth(context: MeasureContext, model: ChartEntryModel): Int = with(context) {
        val length = model.getDrawnEntryCount()
        val segmentWidth = getCellWidth(model.entries.size, false)
        return (segmentWidth * length + spacingDp.pixels * length).roundToInt()
    }

    override fun drawChart(
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        canvas.inClip(bounds) {
            markerLocationMap.clear()
            calculateDrawSegmentSpecIfNeeded(model)
            maxScrollAmount = if (isHorizontalScrollEnabled) maxOf(
                a = 0f,
                b = segmentProperties.segmentWidth * model.getDrawnEntryCount() - bounds.width(),
            ) else 0f
            drawChartInternal(
                model = model,
                cellWidth = segmentProperties.cellWidth,
                spacing = segmentProperties.marginWidth,
            )
            heightMap.clear()
        }
    }

    private fun ChartDrawContext.drawChartInternal(
        model: ChartEntryModel,
        cellWidth: Float,
        spacing: Float,
    ) {
        val yRange =
            ((maxY ?: mergeMode.getMaxY(model)) - minY.orZero).takeIf { it != 0f } ?: return
        val heightMultiplier = bounds.height() / yRange

        var drawingStart: Float
        var height: Float
        var columnCenterX: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val bottomCompensation = if (minY.orZero < 0f) minY.orZero * heightMultiplier else 0f

        val defCellWidth = getCellWidth(model.entries.size)

        model.entries.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            drawingStart = getDrawingStart(
                entryCollectionIndex = index,
                segmentCompensation = (cellWidth - defCellWidth) / 2,
                spacing = spacing,
                columnWidth = column.thicknessDp.pixels * drawScale,
            ) - horizontalScroll

            entryCollection.forEachIn(model.drawMinX..model.drawMaxX) { entry ->
                height = entry.y * heightMultiplier
                columnCenterX = drawingStart +
                    (cellWidth + spacing) * (entry.x - model.minX) / model.step

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        columnBottom = (bounds.bottom + bottomCompensation - cumulatedHeight)
                            .between(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += cellWidth.half * drawScale
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    MergeMode.Grouped -> {
                        columnBottom = (bounds.bottom + bottomCompensation)
                            .between(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += column.thicknessDp.pixels * drawScale
                    }
                }

                if (column.intersectsVertical(
                        context = this,
                        top = columnTop,
                        bottom = columnBottom,
                        centerX = columnCenterX,
                        boundingBox = bounds,
                        thicknessScale = drawScale
                    )
                ) {
                    updateMarkerLocationMap(entry, columnTop, columnCenterX, column)
                    column.drawVertical(this, columnTop, columnBottom, columnCenterX, drawScale)
                }
            }
        }
    }

    private fun updateMarkerLocationMap(
        entry: ChartEntry,
        columnTop: Float,
        columnCenterX: Float,
        column: LineComponent,
    ) {
        markerLocationMap.put(
            x = ceil(columnCenterX),
            y = columnTop.between(bounds.top, bounds.bottom),
            entry = entry,
            color = column.color
        )
    }

    override fun setToAxisModel(axisModel: MutableChartModel, model: ChartEntryModel) {
        axisModel.minY = minY ?: min(model.minY, 0f)
        axisModel.maxY = maxY ?: mergeMode.getMaxY(model)
        axisModel.minX = minX ?: model.minX
        axisModel.maxX = maxX ?: model.maxX
        axisModel.chartEntryModel = model
    }

    override fun getSegmentProperties(
        context: MeasureContext,
        model: ChartEntryModel
    ): SegmentProperties = with(context) {
        calculateDrawSegmentSpecIfNeeded(model)
        segmentProperties.apply {
            cellWidth = context.getCellWidth(model.entries.size)
            marginWidth = spacingDp.pixels * drawScale
        }
    }

    private fun MeasureContext.getCellWidth(
        entryCollectionSize: Int,
        scaled: Boolean = true,
    ): Float = when (mergeMode) {
        MergeMode.Stack ->
            columns.maxOf { it.thicknessDp.pixels }.applyScale(scaled)
        MergeMode.Grouped ->
            getCumulatedThickness(entryCollectionSize, density, scaled) +
                (innerSpacingDp.pixels.applyScale(scaled) * (entryCollectionSize - 1))
    }

    private fun MeasureContext.getDrawingStart(
        entryCollectionIndex: Int,
        segmentCompensation: Float,
        columnWidth: Float,
        spacing: Float,
    ): Float {
        val baseLeft = bounds.left + spacing.half
        return when (mergeMode) {
            MergeMode.Stack ->
                baseLeft
            MergeMode.Grouped ->
                baseLeft + segmentCompensation - columnWidth.half +
                    getCumulatedThickness(entryCollectionIndex, density, true) +
                    (innerSpacingDp.pixels * drawScale * entryCollectionIndex)
        }
    }

    private fun getCumulatedThickness(
        count: Int,
        density: Float,
        scaled: Boolean,
    ): Float {
        var thickness = 0f
        for (i in 0 until count) {
            thickness += columns.getRepeating(i).thicknessDp * density.applyScale(scaled)
        }
        return thickness
    }

    private fun Float.applyScale(applyScale: Boolean) = if (applyScale) this * drawScale else this
}
