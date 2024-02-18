/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.cartesian.layer

import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.cartesian.formatter.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.put
import com.patrykandpatrick.vico.core.cartesian.model.ColumnCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.model.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.model.forEachInIndexed
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.cartesian.values.MutableChartValues
import com.patrykandpatrick.vico.core.common.DefaultDimens
import com.patrykandpatrick.vico.core.common.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.common.ExtraStore
import com.patrykandpatrick.vico.core.common.MutableExtraStore
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.extension.doubled
import com.patrykandpatrick.vico.core.common.extension.getRepeating
import com.patrykandpatrick.vico.core.common.extension.getStart
import com.patrykandpatrick.vico.core.common.extension.half
import com.patrykandpatrick.vico.core.common.position.VerticalPosition
import com.patrykandpatrick.vico.core.common.position.inBounds
import kotlin.math.abs

/**
 * [ColumnCartesianLayer] displays data as vertical bars. It can group and stack columns.
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each column collection. If the list contains a single element, all columns have the same appearance.
 * @param spacingDp the distance between neighboring column collections.
 * @param innerSpacingDp the distance between neighboring grouped columns.
 * @param mergeMode defines how columns should be drawn in column collections.
 * @param verticalAxisPosition the position of the [VerticalAxis] with which the [ColumnCartesianLayer] should be
 * associated. Use this for independent [CartesianLayer] scaling.
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [CartesianValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
 * @param drawingModelInterpolator interpolates the [ColumnCartesianLayer]’s [ColumnCartesianLayerDrawingModel]s.
 */
public open class ColumnCartesianLayer(
    public var columns: List<LineComponent>,
    public var spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
    public var innerSpacingDp: Float = DefaultDimens.COLUMN_INSIDE_SPACING,
    public var mergeMode: (ColumnCartesianLayerModel) -> MergeMode = { MergeMode.Grouped },
    public var verticalAxisPosition: AxisPosition.Vertical? = null,
    public var dataLabel: TextComponent? = null,
    public var dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    public var dataLabelValueFormatter: CartesianValueFormatter = DecimalFormatValueFormatter(),
    public var dataLabelRotationDegrees: Float = 0f,
    public var drawingModelInterpolator: DrawingModelInterpolator<
        ColumnCartesianLayerDrawingModel.ColumnInfo,
        ColumnCartesianLayerDrawingModel,
        > = DefaultDrawingModelInterpolator(),
) : BaseCartesianLayer<ColumnCartesianLayerModel>() {
    /**
     * Creates a [ColumnCartesianLayer] with a common style for all columns.
     *
     * @param column a [LineComponent] defining the appearance of the columns.
     * @param spacingDp the distance between neighboring column collections.
     * @param verticalAxisPosition the position of the [VerticalAxis] with which the [ColumnCartesianLayer] should
     * be associated. Use this for independent [CartesianLayer] scaling.
     */
    public constructor(
        column: LineComponent,
        spacingDp: Float = DefaultDimens.COLUMN_OUTSIDE_SPACING,
        verticalAxisPosition: AxisPosition.Vertical? = null,
    ) : this(columns = listOf(column), spacingDp = spacingDp, verticalAxisPosition = verticalAxisPosition)

    /**
     * Creates a [ColumnCartesianLayer] instance with [columns] set to an empty list. The list must be populated before
     * the chart is drawn.
     */
    public constructor() : this(emptyList())

    /**
     * When [mergeMode] is set to [MergeMode.Stacked], this maps the x-axis value of every column collection to a pair
     * containing the bottom coordinate of the collection’s bottommost column and the top coordinate of the collection’s
     * topmost column. This hash map is used by [drawInternal] and [drawChartInternal].
     */
    protected val heightMap: HashMap<Float, Pair<Float, Float>> = HashMap()

    /**
     * Holds information on the [ColumnCartesianLayer]’s horizontal dimensions.
     */
    protected val horizontalDimensions: MutableHorizontalDimensions = MutableHorizontalDimensions()

    protected val drawingModelKey: ExtraStore.Key<ColumnCartesianLayerDrawingModel> = ExtraStore.Key()

    override val entryLocationMap: HashMap<Float, MutableList<CartesianMarker.EntryModel>> = HashMap()

    override fun drawInternal(
        context: CartesianChartDrawContext,
        model: ColumnCartesianLayerModel,
    ): Unit =
        with(context) {
            entryLocationMap.clear()
            drawChartInternal(
                chartValues = chartValues,
                model = model,
                drawingModel = model.extraStore.getOrNull(drawingModelKey),
            )
            heightMap.clear()
        }

    protected open fun CartesianChartDrawContext.drawChartInternal(
        chartValues: ChartValues,
        model: ColumnCartesianLayerModel,
        drawingModel: ColumnCartesianLayerDrawingModel?,
    ) {
        val yRange = chartValues.getYRange(verticalAxisPosition)
        val heightMultiplier = bounds.height() / yRange.length

        var drawingStart: Float
        var height: Float
        var columnCenterX: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val zeroLinePosition = bounds.bottom + yRange.minY / yRange.length * bounds.height()
        val mergeMode = mergeMode(model)

        model.series.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            drawingStart = getDrawingStart(index, model.series.size, mergeMode) - horizontalScroll

            entryCollection.forEachInIndexed(chartValues.minX..chartValues.maxX) { entryIndex, entry, _ ->

                val columnInfo = drawingModel?.getOrNull(index)?.get(entry.x)
                height = (columnInfo?.height ?: (abs(entry.y) / yRange.length)) * bounds.height()
                val xSpacingMultiplier = (entry.x - chartValues.minX) / chartValues.xStep
                check(xSpacingMultiplier % 1f == 0f) { "Each entry’s x value must be a multiple of the x step." }
                columnCenterX = drawingStart +
                    (horizontalDimensions.xSpacing * xSpacingMultiplier + column.thicknessDp.half.pixels * zoom) *
                    layoutDirectionMultiplier

                when (mergeMode) {
                    MergeMode.Stacked -> {
                        val (stackedNegHeight, stackedPosHeight) = heightMap.getOrElse(entry.x) { 0f to 0f }
                        columnBottom = zeroLinePosition +
                            if (entry.y < 0f) {
                                height + stackedNegHeight
                            } else {
                                -stackedPosHeight
                            }

                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        heightMap[entry.x] =
                            if (entry.y < 0f) {
                                stackedNegHeight + height to stackedPosHeight
                            } else {
                                stackedNegHeight to stackedPosHeight + height
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
                        thicknessScale = zoom,
                    )
                ) {
                    updateMarkerLocationMap(entry, columnSignificantY, columnCenterX, column, entryIndex)
                    column.drawVertical(this, columnTop, columnBottom, columnCenterX, zoom, drawingModel?.opacity ?: 1f)
                }

                if (mergeMode == MergeMode.Grouped) {
                    drawDataLabel(
                        modelEntriesSize = model.series.size,
                        columnThicknessDp = column.thicknessDp,
                        dataLabelValue = entry.y,
                        x = columnCenterX,
                        y = columnSignificantY,
                        isFirst = index == 0 && entry.x == chartValues.minX,
                        isLast = index == model.series.lastIndex && entry.x == chartValues.maxX,
                        mergeMode = mergeMode,
                    )
                } else if (index == model.series.lastIndex) {
                    val yValues = heightMap[entry.x]
                    drawStackedDataLabel(
                        modelEntriesSize = model.series.size,
                        columnThicknessDp = column.thicknessDp,
                        negativeY = yValues?.first,
                        positiveY = yValues?.second,
                        x = columnCenterX,
                        zeroLinePosition = zeroLinePosition,
                        heightMultiplier = heightMultiplier,
                        isFirst = entry.x == chartValues.minX,
                        isLast = entry.x == chartValues.maxX,
                        mergeMode = mergeMode,
                    )
                }
            }
        }
    }

    protected open fun CartesianChartDrawContext.drawStackedDataLabel(
        modelEntriesSize: Int,
        columnThicknessDp: Float,
        negativeY: Float?,
        positiveY: Float?,
        x: Float,
        zeroLinePosition: Float,
        heightMultiplier: Float,
        isFirst: Boolean,
        isLast: Boolean,
        mergeMode: MergeMode,
    ) {
        if (positiveY != null && positiveY > 0f) {
            val y = zeroLinePosition - positiveY * heightMultiplier
            drawDataLabel(modelEntriesSize, columnThicknessDp, positiveY, x, y, isFirst, isLast, mergeMode)
        }
        if (negativeY != null && negativeY < 0f) {
            val y = zeroLinePosition + abs(negativeY) * heightMultiplier
            drawDataLabel(modelEntriesSize, columnThicknessDp, negativeY, x, y, isFirst, isLast, mergeMode)
        }
    }

    protected open fun CartesianChartDrawContext.drawDataLabel(
        modelEntriesSize: Int,
        columnThicknessDp: Float,
        dataLabelValue: Float,
        x: Float,
        y: Float,
        isFirst: Boolean,
        isLast: Boolean,
        mergeMode: MergeMode,
    ) {
        dataLabel?.let { textComponent ->

            val canUseXSpacing =
                mergeMode == MergeMode.Stacked ||
                    mergeMode == MergeMode.Grouped && modelEntriesSize == 1
            var maxWidth =
                when {
                    canUseXSpacing -> horizontalDimensions.xSpacing
                    mergeMode == MergeMode.Grouped ->
                        (columnThicknessDp + minOf(spacingDp, innerSpacingDp).half).pixels * zoom

                    else -> error(message = "Encountered an unexpected `MergeMode`.")
                }
            if (isFirst && horizontalLayout is HorizontalLayout.FullWidth) {
                maxWidth = maxWidth.coerceAtMost(horizontalDimensions.startPadding.doubled)
            }
            if (isLast && horizontalLayout is HorizontalLayout.FullWidth) {
                maxWidth = maxWidth.coerceAtMost(horizontalDimensions.endPadding.doubled)
            }
            val text =
                dataLabelValueFormatter.formatValue(
                    value = dataLabelValue,
                    chartValues = chartValues,
                    verticalAxisPosition = verticalAxisPosition,
                )
            val dataLabelWidth =
                textComponent.getWidth(
                    context = this,
                    text = text,
                    rotationDegrees = dataLabelRotationDegrees,
                ).coerceAtMost(maximumValue = maxWidth)

            if (x - dataLabelWidth.half > bounds.right || x + dataLabelWidth.half < bounds.left) return

            val labelVerticalPosition =
                if (dataLabelValue < 0f) dataLabelVerticalPosition.negative() else dataLabelVerticalPosition

            val verticalPosition =
                labelVerticalPosition.inBounds(
                    y = y,
                    bounds = bounds,
                    componentHeight =
                        textComponent.getHeight(
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
        entry: ColumnCartesianLayerModel.Entry,
        columnTop: Float,
        columnCenterX: Float,
        column: LineComponent,
        index: Int,
    ) {
        if (columnCenterX > bounds.left - 1 && columnCenterX < bounds.right + 1) {
            entryLocationMap.put(
                x = columnCenterX,
                y = columnTop.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = column.solidOrStrokeColor,
                index = index,
            )
        }
    }

    override fun updateChartValues(
        chartValues: MutableChartValues,
        model: ColumnCartesianLayerModel,
    ) {
        val mergeMode = mergeMode(model)
        chartValues.tryUpdate(
            axisPosition = verticalAxisPosition,
            minX = axisValueOverrider?.getMinX(model) ?: model.minX,
            maxX = axisValueOverrider?.getMaxX(model) ?: model.maxX,
            minY = axisValueOverrider?.getMinY(model) ?: mergeMode.getMinY(model).coerceAtMost(0f),
            maxY =
                axisValueOverrider?.getMaxY(model)
                    ?: if (model.minY == 0f && model.maxY == 0f) 1f else mergeMode.getMaxY(model).coerceAtLeast(0f),
        )
    }

    override fun updateHorizontalDimensions(
        context: CartesianMeasureContext,
        horizontalDimensions: MutableHorizontalDimensions,
        model: ColumnCartesianLayerModel,
    ) {
        with(context) {
            val columnCollectionWidth =
                getColumnCollectionWidth(if (model.series.isNotEmpty()) model.series.size else 1, mergeMode(model))
            val xSpacing = columnCollectionWidth + spacingDp.pixels
            when (val horizontalLayout = horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    horizontalDimensions.ensureValuesAtLeast(
                        xSpacing = xSpacing,
                        scalableStartPadding = xSpacing.half,
                        scalableEndPadding = xSpacing.half,
                    )
                }

                is HorizontalLayout.FullWidth -> {
                    horizontalDimensions.ensureValuesAtLeast(
                        xSpacing = xSpacing,
                        scalableStartPadding =
                            columnCollectionWidth.half +
                                horizontalLayout.scalableStartPaddingDp.pixels,
                        scalableEndPadding = columnCollectionWidth.half + horizontalLayout.scalableEndPaddingDp.pixels,
                        unscalableStartPadding = horizontalLayout.unscalableStartPaddingDp.pixels,
                        unscalableEndPadding = horizontalLayout.unscalableEndPaddingDp.pixels,
                    )
                }
            }
        }
    }

    protected open fun CartesianMeasureContext.getColumnCollectionWidth(
        entryCollectionSize: Int,
        mergeMode: MergeMode,
    ): Float =
        when (mergeMode) {
            MergeMode.Stacked ->
                columns.take(entryCollectionSize).maxOf { it.thicknessDp.pixels }

            MergeMode.Grouped ->
                getCumulatedThickness(entryCollectionSize) + innerSpacingDp.pixels * (entryCollectionSize - 1)
        }

    protected open fun CartesianChartDrawContext.getDrawingStart(
        entryCollectionIndex: Int,
        entryCollectionCount: Int,
        mergeMode: MergeMode,
    ): Float {
        val mergeModeComponent =
            when (mergeMode) {
                MergeMode.Grouped ->
                    getCumulatedThickness(entryCollectionIndex) + innerSpacingDp.pixels * entryCollectionIndex

                MergeMode.Stacked -> 0f
            }
        return bounds.getStart(isLtr) + (
            horizontalDimensions.startPadding +
                (mergeModeComponent - getColumnCollectionWidth(entryCollectionCount, mergeMode).half) * zoom
        ) * layoutDirectionMultiplier
    }

    protected open fun CartesianMeasureContext.getCumulatedThickness(count: Int): Float {
        var thickness = 0f
        for (i in 0..<count) {
            thickness += columns.getRepeating(i).thicknessDp * density
        }
        return thickness
    }

    /**
     * Defines how a [ColumnCartesianLayer] should draw columns in column collections.
     */
    public enum class MergeMode {
        /**
         * Columns with the same x-axis values will be placed next to each other in groups.
         */
        Grouped,

        /**
         * Columns with the same x-axis values will be placed on top of each other.
         */
        Stacked,

        ;

        /**
         * Returns the minimum y-axis value, taking into account the current [MergeMode].
         */
        public fun getMinY(model: ColumnCartesianLayerModel): Float =
            when (this) {
                Grouped -> model.minY
                Stacked -> model.minAggregateY
            }

        /**
         * Returns the maximum y-axis value, taking into account the current [MergeMode].
         */
        public fun getMaxY(model: ColumnCartesianLayerModel): Float =
            when (this) {
                Grouped -> model.maxY
                Stacked -> model.maxAggregateY
            }
    }

    override fun prepareForTransformation(
        model: ColumnCartesianLayerModel?,
        extraStore: MutableExtraStore,
        chartValues: ChartValues,
    ) {
        drawingModelInterpolator.setModels(
            old = extraStore.getOrNull(drawingModelKey),
            new = model?.toDrawingModel(chartValues),
        )
    }

    override suspend fun transform(
        extraStore: MutableExtraStore,
        fraction: Float,
    ) {
        drawingModelInterpolator
            .transform(fraction)
            ?.let { extraStore[drawingModelKey] = it }
            ?: extraStore.remove(drawingModelKey)
    }

    private fun ColumnCartesianLayerModel.toDrawingModel(chartValues: ChartValues) =
        series
            .map { series ->
                series.associate { entry ->
                    entry.x to
                        ColumnCartesianLayerDrawingModel.ColumnInfo(
                            height = abs(entry.y) / chartValues.getYRange(verticalAxisPosition).length,
                        )
                }
            }
            .let(::ColumnCartesianLayerDrawingModel)
}
