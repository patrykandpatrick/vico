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
import com.patrykandpatryk.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.chart.BaseChart
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.draw.segmentWidth
import com.patrykandpatryk.vico.core.chart.forEachInIndexed
import com.patrykandpatryk.vico.core.chart.put
import com.patrykandpatryk.vico.core.chart.segment.MutableSegmentProperties
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.chart.values.ChartValues
import com.patrykandpatryk.vico.core.chart.values.ChartValuesManager
import com.patrykandpatryk.vico.core.component.shape.LineComponent
import com.patrykandpatryk.vico.core.component.text.TextComponent
import com.patrykandpatryk.vico.core.component.text.VerticalPosition
import com.patrykandpatryk.vico.core.component.text.inBounds
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.context.layoutDirectionMultiplier
import com.patrykandpatryk.vico.core.entry.ChartEntry
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.extension.getRepeating
import com.patrykandpatryk.vico.core.extension.getStart
import com.patrykandpatryk.vico.core.extension.half
import com.patrykandpatryk.vico.core.extension.orZero
import com.patrykandpatryk.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatryk.vico.core.formatter.ValueFormatter
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
 * @param targetVerticalAxisPosition if this is set, any [com.patrykandpatryk.vico.core.axis.AxisRenderer] with an
 * [AxisPosition] equal to the provided value will use the [ChartValues] provided by this chart.
 * This is meant to be used with [com.patrykandpatryk.vico.core.chart.composed.ComposedChart].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels in degrees.
 */
public open class ColumnChart(
    public var columns: List<LineComponent>,
    public var spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
    public var innerSpacingDp: Float = DefaultDimens.COLUMN_INSIDE_SPACING,
    public var mergeMode: MergeMode = MergeMode.Grouped,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    public var dataLabel: TextComponent? = null,
    public var dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    public var dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
    public var dataLabelRotationDegrees: Float = 0f,
) : BaseChart<ChartEntryModel>() {

    /**
     * Creates a [ColumnChart] with a common style for all columns.
     *
     * @param column a [LineComponent] defining the appearance of the columns.
     * @param spacingDp the horizontal padding between the edges of chart segments and the columns they contain.
     * @param targetVerticalAxisPosition if this is set, any [com.patrykandpatryk.vico.core.axis.AxisRenderer] with an
     * [AxisPosition] equal to the provided value will use the [ChartValues] provided by this chart.
     * This is meant to be used with [com.patrykandpatryk.vico.core.chart.composed.ComposedChart].
     */
    public constructor(
        column: LineComponent,
        spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
        targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    ) : this(columns = listOf(column), spacingDp = spacingDp, targetVerticalAxisPosition = targetVerticalAxisPosition)

    /**
     * Creates [ColumnChart] without any [columns]. At least one [LineComponent] must be added to [columns] before
     * the chart is rendered.
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

            entryCollection.forEachInIndexed(range = chartValues.minX..chartValues.maxX) { entryIndex, entry ->

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
                        thicknessScale = chartScale,
                    )
                ) {
                    updateMarkerLocationMap(entry, columnTop, columnCenterX, column)
                    column.drawVertical(this, columnTop, columnBottom, columnCenterX, chartScale)
                }

                drawDataLabel(index, model, column.thicknessDp, entry, entryIndex, columnCenterX, columnTop)
            }
        }
    }

    @LongParameterListDrawFunction
    private fun ChartDrawContext.drawDataLabel(
        entryCollectionIndex: Int,
        model: ChartEntryModel,
        columnThicknessDp: Float,
        entry: ChartEntry,
        entryIndex: Int,
        x: Float,
        y: Float,
    ) {
        if (mergeMode == MergeMode.Stack && entryCollectionIndex != model.entries.lastIndex) return

        dataLabel?.let { textComponent ->

            val canUseSegmentWidth =
                mergeMode == MergeMode.Stack ||
                    mergeMode == MergeMode.Grouped && model.entries.size == 1
            val maxWidth = when {
                canUseSegmentWidth -> segmentWidth
                mergeMode == MergeMode.Grouped ->
                    (columnThicknessDp + 2 * minOf(spacingDp, innerSpacingDp.half)).wholePixels
                else -> error(message = "Encountered an unexpected `MergeMode`.")
            } * chartScale
            val dataLabelValue = when (mergeMode) {
                MergeMode.Grouped -> entry.y
                MergeMode.Stack -> model.entries.fold(initial = 0f) { sum, entryCollection ->
                    sum + entryCollection.getOrNull(index = entryIndex)?.y.orZero
                }
            }
            val text = dataLabelValueFormatter.formatValue(
                value = dataLabelValue,
                chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
            )
            val dataLabelWidth = textComponent.getWidth(
                context = this,
                text = text,
                rotationDegrees = dataLabelRotationDegrees,
            ).coerceAtMost(maximumValue = maxWidth)

            if (x - dataLabelWidth.half > bounds.right ||
                x + dataLabelWidth.half < bounds.left
            ) return

            val verticalPosition = dataLabelVerticalPosition.inBounds(
                y = y,
                bounds = bounds,
                componentHeight = textComponent.getHeight(
                    context = this,
                    text = text,
                    width = maxWidth.toInt(),
                    rotationDegrees = dataLabelRotationDegrees,
                ),
            )
            textComponent.drawText(
                context = this,
                text = text,
                textX = x,
                textY = y,
                verticalPosition = verticalPosition,
                maxTextWidth = maxWidth.toInt(),
                rotationDegrees = dataLabelRotationDegrees,
            )
        }
    }

    private fun updateMarkerLocationMap(
        entry: ChartEntry,
        columnTop: Float,
        columnCenterX: Float,
        column: LineComponent,
    ) {
        if (columnCenterX in bounds.left..bounds.right) {
            entryLocationMap.put(
                x = columnCenterX,
                y = columnTop.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = column.color,
            )
        }
    }

    override fun updateChartValues(chartValuesManager: ChartValuesManager, model: ChartEntryModel) {
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: minX ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: maxX ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: minY ?: min(model.minY, 0f),
            maxY = axisValuesOverrider?.getMaxY(model) ?: maxY ?: mergeMode.getMaxY(model),
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
