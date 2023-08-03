/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.column

import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.BaseChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.forEachInRelativelyIndexed
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.put
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.component.text.inBounds
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.patrykandpatrick.vico.core.marker.Marker
import kotlin.math.abs

/**
 * [ColumnChart] displays data as vertical bars. It can group and stack columns.
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each column collection. If the list contains a single element, all columns have the same appearance.
 * @param spacingDp the distance between neighboring column collections.
 * @param innerSpacingDp the distance between neighboring grouped columns.
 * @param mergeMode defines how columns should be drawn in column collections.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
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
    public var dataLabelRotationDegrees: Float = 0f
) : BaseChart<ChartEntryModel>() {

    /**
     * Creates a [ColumnChart] with a common style for all columns.
     *
     * @param column a [LineComponent] defining the appearance of the columns.
     * @param spacingDp the distance between neighboring column collections.
     * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
     * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
     */
    public constructor(
        column: LineComponent,
        spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
        targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    ) : this(columns = listOf(column), spacingDp = spacingDp, targetVerticalAxisPosition = targetVerticalAxisPosition)

    /**
     * Creates a [ColumnChart] instance with [columns] set to an empty list. The list must be populated before the chart
     * is drawn.
     */
    public constructor() : this(emptyList())

    /**
     * When [mergeMode] is set to [MergeMode.Stack], this maps the x-axis value of every column collection to a pair
     * containing the bottom coordinate of the collection’s bottommost column and the top coordinate of the collection’s
     * topmost column. This hash map is used by [drawChart] and [drawChartInternal].
     */
    protected val heightMap: HashMap<Float, Pair<Float, Float>> = HashMap()

    /**
     * Holds information on the [ColumnChart]’s horizontal dimensions.
     */
    protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()
    override var markerMoveWithPoint: Boolean = false

    override fun drawChart(
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        entryLocationMap.clear()
        drawChartInternal(
            chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
            model = model,
        )
        heightMap.clear()
    }

    protected open fun ChartDrawContext.drawChartInternal(
        chartValues: ChartValues,
        model: ChartEntryModel,
    ) {
        val yRange = (chartValues.maxY - chartValues.minY).takeIf { it != 0f } ?: return
        val heightMultiplier = bounds.height() / yRange

        var drawingStart: Float
        var height: Float
        var columnCenterX: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val zeroLinePosition = bounds.bottom + chartValues.minY / yRange * bounds.height()

        model.entries.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            drawingStart = getDrawingStart(index, model.entries.size) - horizontalScroll

            entryCollection.forEachInRelativelyIndexed(chartValues.minX..chartValues.maxX) { entryIndex, entry ->

                height = abs(entry.y) * heightMultiplier
                val xSpacingMultiplier = (entry.x - chartValues.minX) / chartValues.xStep
                check(xSpacingMultiplier % 1f == 0f) { "Each entry’s x value must be a multiple of the x step." }
                columnCenterX = drawingStart +
                    (horizontalDimensions.xSpacing * xSpacingMultiplier + column.thicknessDp.half.pixels * chartScale) *
                    layoutDirectionMultiplier

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val (stackedNegY, stackedPosY) = heightMap.getOrElse(entry.x) { 0f to 0f }
                        columnBottom = zeroLinePosition +
                            if (entry.y < 0f) {
                                height + abs(stackedNegY) * heightMultiplier
                            } else {
                                -stackedPosY * heightMultiplier
                            }

                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        heightMap[entry.x] =
                            if (entry.y < 0f) {
                                stackedNegY + entry.y to stackedPosY
                            } else {
                                stackedNegY to stackedPosY + entry.y
                            }
                    }

                    MergeMode.Grouped -> {
                        columnBottom = zeroLinePosition + if (entry.y < 0f) height else 0f
                        columnTop = columnBottom - height
                    }
                }

                val columnSignificantY = if (entry.y < 0f) columnBottom else columnTop

                if (column.intersectsVertical(
                        context = this,
                        top = columnTop,
                        bottom = columnBottom,
                        centerX = columnCenterX,
                        boundingBox = bounds,
                        thicknessScale = chartScale,
                    )
                ) {
                    updateMarkerLocationMap(entry, columnSignificantY, columnCenterX, column, entryIndex)
                    column.drawVertical(this, columnTop, columnBottom, columnCenterX, chartScale)
                }

                if (mergeMode == MergeMode.Grouped) {
                    drawDataLabel(
                        modelEntriesSize = model.entries.size,
                        columnThicknessDp = column.thicknessDp,
                        dataLabelValue = entry.y,
                        x = columnCenterX,
                        y = columnSignificantY,
                        isFirst = index == 0 && entry.x == chartValues.minX,
                        isLast = index == model.entries.lastIndex && entry.x == chartValues.maxX,
                    )
                } else if (index == model.entries.lastIndex) {
                    val yValues = heightMap[entry.x]
                    drawStackedDataLabel(
                        modelEntriesSize = model.entries.size,
                        columnThicknessDp = column.thicknessDp,
                        negativeY = yValues?.first,
                        positiveY = yValues?.second,
                        x = columnCenterX,
                        zeroLinePosition = zeroLinePosition,
                        heightMultiplier = heightMultiplier,
                        isFirst = entry.x == chartValues.minX,
                        isLast = entry.x == chartValues.maxX,
                    )
                }
            }
        }
    }

    protected open fun ChartDrawContext.drawStackedDataLabel(
        modelEntriesSize: Int,
        columnThicknessDp: Float,
        negativeY: Float?,
        positiveY: Float?,
        x: Float,
        zeroLinePosition: Float,
        heightMultiplier: Float,
        isFirst: Boolean,
        isLast: Boolean,
    ) {
        if (positiveY != null && positiveY > 0f) {
            val y = zeroLinePosition - positiveY * heightMultiplier
            drawDataLabel(modelEntriesSize, columnThicknessDp, positiveY, x, y, isFirst, isLast)
        }
        if (negativeY != null && negativeY < 0f) {
            val y = zeroLinePosition + abs(negativeY) * heightMultiplier
            drawDataLabel(modelEntriesSize, columnThicknessDp, negativeY, x, y, isFirst, isLast)
        }
    }

    protected open fun ChartDrawContext.drawDataLabel(
        modelEntriesSize: Int,
        columnThicknessDp: Float,
        dataLabelValue: Float,
        x: Float,
        y: Float,
        isFirst: Boolean,
        isLast: Boolean,
    ) {
        dataLabel?.let { textComponent ->

            val canUseXSpacing =
                mergeMode == MergeMode.Stack ||
                    mergeMode == MergeMode.Grouped && modelEntriesSize == 1
            var maxWidth = when {
                canUseXSpacing -> horizontalDimensions.xSpacing
                mergeMode == MergeMode.Grouped ->
                    (columnThicknessDp + minOf(spacingDp, innerSpacingDp).half).pixels * chartScale

                else -> error(message = "Encountered an unexpected `MergeMode`.")
            }
            if (isFirst && horizontalLayout is HorizontalLayout.FullWidth) {
                maxWidth = maxWidth.coerceAtMost(horizontalDimensions.startPadding.doubled)
            }
            if (isLast && horizontalLayout is HorizontalLayout.FullWidth) {
                maxWidth = maxWidth.coerceAtMost(horizontalDimensions.endPadding.doubled)
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

            if (x - dataLabelWidth.half > bounds.right || x + dataLabelWidth.half < bounds.left) return

            val labelVerticalPosition =
                if (dataLabelValue < 0f) dataLabelVerticalPosition.negative() else dataLabelVerticalPosition

            val verticalPosition = labelVerticalPosition.inBounds(
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

    protected open fun updateMarkerLocationMap(
        entry: ChartEntry,
        columnTop: Float,
        columnCenterX: Float,
        column: LineComponent,
        index: Int,
    ) {
        if (columnCenterX in bounds.left..bounds.right) {
            entryLocationMap.put(
                x = columnCenterX,
                y = columnTop.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = column.color,
                index = index,
            )
        }
    }

    override fun updateChartValues(chartValuesManager: ChartValuesManager, model: ChartEntryModel, xStep: Float?) {
        @Suppress("DEPRECATION_ERROR")
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: minX ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: maxX ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: minY ?: mergeMode.getMinY(model),
            maxY = axisValuesOverrider?.getMaxY(model) ?: maxY ?: mergeMode.getMaxY(model),
            xStep = xStep ?: model.xGcd,
            chartEntryModel = model,
            axisPosition = targetVerticalAxisPosition,
        )
    }

    override fun getHorizontalDimensions(
        context: MeasureContext,
        model: ChartEntryModel,
    ): HorizontalDimensions = with(context) {
        val columnCollectionWidth = getColumnCollectionWidth(if (model.entries.isNotEmpty()) model.entries.size else 1)
        horizontalDimensions.apply {
            xSpacing = columnCollectionWidth + spacingDp.pixels
            when (val horizontalLayout = horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    scalableStartPadding = xSpacing.half
                    scalableEndPadding = scalableStartPadding
                }
                is HorizontalLayout.FullWidth -> {
                    scalableStartPadding = columnCollectionWidth.half + horizontalLayout.startPaddingDp.pixels
                    scalableEndPadding = columnCollectionWidth.half + horizontalLayout.endPaddingDp.pixels
                }
            }
        }
    }

    protected open fun MeasureContext.getColumnCollectionWidth(
        entryCollectionSize: Int,
    ): Float = when (mergeMode) {
        MergeMode.Stack ->
            columns.take(entryCollectionSize).maxOf { it.thicknessDp.pixels }

        MergeMode.Grouped ->
            getCumulatedThickness(entryCollectionSize) + innerSpacingDp.pixels * (entryCollectionSize - 1)
    }

    protected open fun MeasureContext.getDrawingStart(entryCollectionIndex: Int, entryCollectionCount: Int): Float {
        val mergeModeComponent = when (mergeMode) {
            MergeMode.Grouped ->
                getCumulatedThickness(entryCollectionIndex) + innerSpacingDp.pixels * entryCollectionIndex

            MergeMode.Stack -> 0f
        }
        return bounds.getStart(isLtr) + (
            horizontalDimensions.scaled(chartScale).startPadding +
                (mergeModeComponent - getColumnCollectionWidth(entryCollectionCount).half) * chartScale
            ) * layoutDirectionMultiplier
    }

    protected open fun MeasureContext.getCumulatedThickness(count: Int): Float {
        var thickness = 0f
        for (i in 0..<count) {
            thickness += columns.getRepeating(i).thicknessDp * density
        }
        return thickness
    }

    /**
     * Defines how a [ColumnChart] should draw columns in column collections.
     */
    public enum class MergeMode {

        /**
         * Columns with the same x-axis values will be placed next to each other in groups.
         */
        Grouped,

        /**
         * Columns with the same x-axis values will be placed on top of each other.
         */
        Stack,

        ;

        /**
         * Returns the minimum y-axis value, taking into account the current [MergeMode].
         */
        public fun getMinY(model: ChartEntryModel): Float = when (this) {
            Grouped -> model.minY.coerceAtMost(0f)
            Stack -> model.stackedNegativeY.coerceAtMost(0f)
        }

        /**
         * Returns the maximum y-axis value, taking into account the current [MergeMode].
         */
        public fun getMaxY(model: ChartEntryModel): Float = when (this) {
            Grouped -> model.maxY
            Stack -> model.stackedPositiveY
        }
    }
}
