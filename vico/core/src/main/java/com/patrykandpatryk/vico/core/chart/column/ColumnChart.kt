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
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.chart.values.ChartValues
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.chart.forEachIn
import com.patrykandpatryk.vico.core.chart.put
import com.patrykandpatryk.vico.core.chart.BaseChart
import com.patrykandpatryk.vico.core.chart.segment.MutableSegmentProperties
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.chart.values.ChartValuesManager
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.extension.getRepeating
import com.patrykandpatryk.vico.core.extension.half
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.context.layoutDirectionMultiplier
import com.patrykandpatryk.vico.core.extension.getStart
import com.patrykandpatryk.vico.core.marker.Marker
import kotlin.math.min

/**
 * [ColumnChart] displays data in vertical columns.
 * It supports rendering multiple columns for multiple sets of data.
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
 * @param spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
 * @param innerSpacingDp the spacing between the columns contained in chart segments. This has no effect on
 * segments that contain a single column only.
 * @param mergeMode defines the way multiple columns are rendered in the [ColumnChart].
 * @param targetVerticalAxisPosition if set, an [com.patrykandpatryk.vico.core.axis.AxisRenderer] with an [AxisPosition]
 * equal to the [targetVerticalAxisPosition] will use [ChartValues] provided by this chart.
 * It’s meant to be used with [com.patrykandpatryk.vico.core.chart.composed.ComposedChart].
 */
public open class ColumnChart(
    public var columns: List<LineComponent>,
    public var spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
    public var innerSpacingDp: Float = DefaultDimens.COLUMN_INSIDE_SPACING,
    public var mergeMode: MergeMode = MergeMode.Grouped,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null,
) : BaseChart<ChartEntryModel>() {

    /**
     * Creates a [ColumnChart] with one column style.
     *
     * @param column a [LineComponent] defining a look of the column.
     * @param spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
     * @param targetVerticalAxisPosition if set, an [com.patrykandpatryk.vico.core.axis.AxisRenderer] with an
     * [AxisPosition] equal to the [targetVerticalAxisPosition] will use [ChartValues] provided by this chart.
     * It’s meant to be used with [com.patrykandpatryk.vico.core.chart.composed.ComposedChart].
     */
    public constructor(
        column: LineComponent,
        spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
        targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    ) : this(columns = listOf(column), spacingDp = spacingDp, targetVerticalAxisPosition = targetVerticalAxisPosition)

    /**
     * Creates [ColumnChart] without any [columns]. At least one [LineComponent] must be added to [columns] before
     * rendering the chart.
     */
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
            chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
            model = model,
            cellWidth = segmentProperties.cellWidth,
            spacing = segmentProperties.marginWidth,
        )
        heightMap.clear()
    }

    private fun ChartDrawContext.drawChartInternal(
        chartValues: ChartValues,
        model: ChartEntryModel,
        cellWidth: Float,
        spacing: Float,
    ) {

        val yRange = (chartValues.maxY - chartValues.minY).takeIf { it != 0f } ?: return
        val heightMultiplier = bounds.height() / yRange
        val heightReduce = chartValues.minY * heightMultiplier

        var drawingStart: Float
        var height: Float
        var columnCenterX: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val bottomCompensation = if (chartValues.minY < 0f) chartValues.minY * heightMultiplier else 0f

        val defCellWidth = getCellWidth(model.entries.size) * chartScale

        model.entries.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            drawingStart = getDrawingStart(
                entryCollectionIndex = index,
                segmentCompensation = (cellWidth - defCellWidth) / 2,
                spacing = spacing,
                columnWidth = column.thicknessDp.pixels * chartScale,
            ) - horizontalScroll

            entryCollection.forEachIn(chartValues.minX..chartValues.maxX) { entry ->
                height = entry.y * heightMultiplier - heightReduce
                columnCenterX = drawingStart + layoutDirectionMultiplier *
                    (cellWidth + spacing) * (entry.x - chartValues.minX) / model.stepX

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val cumulatedHeight = heightMap.getOrElse(entry.x) { 0f }
                        columnBottom = (bounds.bottom + bottomCompensation - cumulatedHeight)
                            .coerceIn(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += layoutDirectionMultiplier * cellWidth.half
                        heightMap[entry.x] = cumulatedHeight + height
                    }
                    MergeMode.Grouped -> {
                        columnBottom = (bounds.bottom + bottomCompensation)
                            .coerceIn(bounds.top, bounds.bottom)
                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += layoutDirectionMultiplier * column.thicknessDp.pixels * chartScale
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
            x = columnCenterX,
            y = columnTop.coerceIn(bounds.top, bounds.bottom),
            entry = entry,
            color = column.color,
        )
    }

    override fun updateChartValues(chartValuesManager: ChartValuesManager, model: ChartEntryModel) {
        chartValuesManager.updateBy(
            minX = minX ?: model.minX,
            maxX = maxX ?: model.maxX,
            minY = minY ?: min(model.minY, 0f),
            maxY = maxY ?: mergeMode.getMaxY(model),
            chartEntryModel = model,
            axisPosition = targetVerticalAxisPosition,
        )
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
            getCumulatedThickness(entryCollectionSize) + innerSpacingDp.pixels * (entryCollectionSize - 1)
    }

    private fun MeasureContext.getDrawingStart(
        entryCollectionIndex: Int,
        segmentCompensation: Float,
        columnWidth: Float,
        spacing: Float,
    ): Float {
        val baseStart = bounds.getStart(isLtr = isLtr) + layoutDirectionMultiplier * spacing.half
        return when (mergeMode) {
            MergeMode.Stack -> baseStart
            MergeMode.Grouped -> {
                val offset = segmentCompensation - columnWidth.half +
                    getCumulatedThickness(entryCollectionIndex) * chartScale +
                    innerSpacingDp.pixels * chartScale * entryCollectionIndex
                baseStart + layoutDirectionMultiplier * offset
            }
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
