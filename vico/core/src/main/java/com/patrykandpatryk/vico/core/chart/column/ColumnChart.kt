/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatryk.vico.core.chart.column

import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.axis.model.MutableChartModel
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.chart.forEachIn
import com.patrykandpatryk.vico.core.chart.put
import com.patrykandpatryk.vico.core.chart.BaseChart
import com.patrykandpatryk.vico.core.chart.segment.MutableSegmentProperties
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.extension.getRepeating
import com.patrykandpatryk.vico.core.extension.half
import com.patrykandpatryk.vico.core.extension.orZero
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.marker.Marker
import kotlin.math.ceil
import kotlin.math.min

/**
 * [ColumnChart] displays data in vertical columns.
 * It supports rendering multiple columns for multiple sets of data.
 *
 * @property columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
 * @property spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
 * @property innerSpacingDp the spacing between the columns contained in chart segments. This has no effect on
 * segments that contain a single column only.
 * @property mergeMode defines the way multiple columns are rendered in the [ColumnChart].
 */
public open class ColumnChart(
    public var columns: List<LineComponent>,
    public var spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
    public var innerSpacingDp: Float = DefaultDimens.COLUMN_INSIDE_SPACING,
    public var mergeMode: MergeMode = MergeMode.Grouped,
) : BaseChart<ChartEntryModel>() {

    public constructor(
        column: LineComponent,
        spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
    ) : this(columns = listOf(column), spacingDp = spacingDp)

    public constructor() : this(emptyList())

    private val heightMap = HashMap<Float, Float>()
    private val segmentProperties = MutableSegmentProperties()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    override fun drawChart(
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        entryLocationMap.clear()
        drawChartInternal(
            model = model,
            cellWidth = segmentProperties.cellWidth,
            spacing = segmentProperties.marginWidth,
        )
        heightMap.clear()
    }

    private fun ChartDrawContext.drawChartInternal(
        model: ChartEntryModel,
        cellWidth: Float,
        spacing: Float,
    ) {
        val yRange = ((maxY ?: mergeMode.getMaxY(model)) - minY.orZero).takeIf { it != 0f } ?: return
        val heightMultiplier = bounds.height() / yRange
        val heightReduce = minY.orZero * heightMultiplier

        var drawingStart: Float
        var height: Float
        var columnCenterX: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val bottomCompensation = if (minY.orZero < 0f) minY.orZero * heightMultiplier else 0f

        val defCellWidth = getCellWidth(model.entries.size) * chartScale

        model.entries.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            drawingStart = getDrawingStart(
                entryCollectionIndex = index,
                segmentCompensation = (cellWidth - defCellWidth) / 2,
                spacing = spacing,
                columnWidth = column.thicknessDp.pixels * chartScale,
            ) - horizontalScroll

            entryCollection.forEachIn(model.drawMinX..model.drawMaxX) { entry ->
                height = entry.y * heightMultiplier - heightReduce
                columnCenterX = drawingStart +
                    (cellWidth + spacing) * (entry.x - model.drawMinX) / model.stepX

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        columnBottom = (bounds.bottom + bottomCompensation - cumulatedHeight)
                            .coerceIn(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += cellWidth.half
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    MergeMode.Grouped -> {
                        columnBottom = (bounds.bottom + bottomCompensation)
                            .coerceIn(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += column.thicknessDp.pixels * chartScale
                    }
                }

                if (column.intersectsVertical(
                        context = this,
                        top = columnTop,
                        bottom = columnBottom,
                        centerX = columnCenterX,
                        boundingBox = bounds,
                        thicknessScale = chartScale
                    )
                ) {
                    updateMarkerLocationMap(entry, columnTop, columnCenterX, column)
                    column.drawVertical(this, columnTop, columnBottom, columnCenterX, chartScale)
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
        entryLocationMap.put(
            x = ceil(columnCenterX),
            y = columnTop.coerceIn(bounds.top, bounds.bottom),
            entry = entry,
            color = column.color
        )
    }

    override fun setToChartModel(chartModel: MutableChartModel, model: ChartEntryModel) {
        chartModel.minY = minY ?: min(model.minY, 0f)
        chartModel.maxY = maxY ?: mergeMode.getMaxY(model)
        chartModel.minX = minX ?: model.minX
        chartModel.maxX = maxX ?: model.maxX
        chartModel.chartEntryModel = model
    }

    override fun getSegmentProperties(
        context: MeasureContext,
        model: ChartEntryModel,
    ): SegmentProperties = with(context) {
        segmentProperties.set(cellWidth = context.getCellWidth(model.entries.size), marginWidth = spacingDp.pixels)
    }

    private fun MeasureContext.getCellWidth(
        entryCollectionSize: Int,
    ): Float = when (mergeMode) {
        MergeMode.Stack ->
            columns.maxOf { it.thicknessDp.pixels }
        MergeMode.Grouped ->
            getCumulatedThickness(entryCollectionSize) + (innerSpacingDp.pixels * (entryCollectionSize - 1))
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
                    getCumulatedThickness(entryCollectionIndex) * chartScale +
                    innerSpacingDp.pixels * chartScale * entryCollectionIndex
        }
    }

    private fun MeasureContext.getCumulatedThickness(count: Int): Float {
        var thickness = 0f
        for (i in 0 until count) {
            thickness += columns.getRepeating(i).thicknessDp * density
        }
        return thickness
    }

    /**
     * Defines the way multiple columns are rendered in the [ColumnChart].
     */
    public enum class MergeMode {

        /**
         * Columns with the same x-axis values will be placed next to each other in groups.
         */
        Grouped,

        /**
         * Columns with the same x-axis values will be placed on top of each other.
         */
        Stack;

        /**
         * Returns the maximum y-axis value, taking into account the current [MergeMode].
         */
        public fun getMaxY(model: ChartEntryModel): Float = when (this) {
            Grouped -> model.maxY
            Stack -> model.stackedMaxY
        }
    }
}
