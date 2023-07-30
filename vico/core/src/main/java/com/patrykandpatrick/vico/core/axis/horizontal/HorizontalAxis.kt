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

package com.patrykandpatrick.vico.core.axis.horizontal

import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.setTo
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.isBoundOf
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.throwable.UnknownAxisPositionException
import kotlin.math.min

/**
 * An implementation of [AxisRenderer] used for horizontal axes. This class extends [Axis].
 *
 * @see AxisRenderer
 * @see Axis
 */
public class HorizontalAxis<Position : AxisPosition.Horizontal>(
    override val position: Position,
) : Axis<Position>() {

    private val AxisPosition.Horizontal.textVerticalPosition: VerticalPosition
        get() = if (isBottom) VerticalPosition.Bottom else VerticalPosition.Top

    /**
     * How often labels (and their corresponding ticks and guidelines) should be drawn.
     */
    @Deprecated(
        """`labelSpacing` is being replaced by `AxisItemPlacer.Horizontal`. Create a base implementation with the
            desired spacing via `AxisItemPlacer.Horizontal.default`, and use the `itemPlacer` field to apply it to this
            `HorizontalAxis`.""",
    )
    public var labelSpacing: Int = 1
        set(value) {
            field = value
            @Suppress("DEPRECATION")
            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = value, offset = labelOffset)
        }

    /**
     * The number of labels (and, for [HorizontalLayout.FullWidth], their corresponding ticks and guidelines) to skip
     * from the start.
     */
    @Deprecated(
        """`labelOffset` is being replaced by `AxisItemPlacer.Horizontal`. Create a base implementation with the desired
            offset via `AxisItemPlacer.Horizontal.default`, and use the `itemPlacer` field to apply it to this
            `HorizontalAxis`.""",
    )
    public var labelOffset: Int = 0
        set(value) {
            field = value
            @Suppress("DEPRECATION")
            itemPlacer = AxisItemPlacer.Horizontal.default(spacing = labelSpacing, offset = value)
        }

    /**
     * Determines for what _x_ values this [HorizontalAxis] is to display labels, ticks, and guidelines.
     */
    @Suppress("DEPRECATION")
    public var itemPlacer: AxisItemPlacer.Horizontal = AxisItemPlacer.Horizontal.default(labelSpacing, labelOffset)

    override fun drawBehindChart(context: ChartDrawContext): Unit = with(context) {
        val clipRestoreCount = canvas.save()
        val tickMarkTop = if (position.isBottom) bounds.top else bounds.bottom - tickLength
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val chartValues = chartValuesManager.getChartValues()

        canvas.clipRect(
            bounds.left - itemPlacer.getStartHorizontalAxisInset(this, horizontalDimensions, tickThickness),
            minOf(bounds.top, chartBounds.top),
            bounds.right + itemPlacer.getEndHorizontalAxisInset(this, horizontalDimensions, tickThickness),
            maxOf(bounds.bottom, chartBounds.bottom),
        )

        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop
        val fullXRange = getFullXRange(horizontalDimensions)
        val baseCanvasX = bounds.getStart(isLtr) - horizontalScroll + horizontalDimensions.startPadding *
            layoutDirectionMultiplier
        val firstVisibleX = fullXRange.start + horizontalScroll / horizontalDimensions.xSpacing * chartValues.xStep *
            layoutDirectionMultiplier
        val lastVisibleX = firstVisibleX + bounds.width() / horizontalDimensions.xSpacing * chartValues.xStep
        val visibleXRange = firstVisibleX..lastVisibleX
        val labelValues = itemPlacer.getLabelValues(this, visibleXRange, fullXRange)
        val lineValues = itemPlacer.getLineValues(this, visibleXRange, fullXRange)

        labelValues.forEachIndexed { index, x ->
            val canvasX = baseCanvasX + x / chartValues.xStep * horizontalDimensions.xSpacing *
                layoutDirectionMultiplier
            val previousX = labelValues.getOrNull(index - 1) ?: (fullXRange.start.doubled - x)
            val nextX = labelValues.getOrNull(index + 1) ?: (fullXRange.endInclusive.doubled - x)
            val maxWidth = (min(x - previousX, nextX - x) / chartValues.xStep * horizontalDimensions.xSpacing).toInt()

            label?.drawText(
                context = context,
                text = valueFormatter.formatValue(x, chartValues),
                textX = canvasX,
                textY = textY,
                verticalPosition = position.textVerticalPosition,
                maxTextWidth = maxWidth,
                maxTextHeight = (bounds.height() - tickLength - axisThickness.half).toInt(),
                rotationDegrees = labelRotationDegrees,
            )

            if (lineValues == null) {
                tick?.drawVertical(
                    context = this,
                    top = tickMarkTop,
                    bottom = tickMarkBottom,
                    centerX = canvasX + getLinesCorrectionX(x, fullXRange),
                )
            }
        }

        lineValues?.forEach { x ->
            tick?.drawVertical(
                context = this,
                top = tickMarkTop,
                bottom = tickMarkBottom,
                centerX = baseCanvasX + x / chartValues.xStep * horizontalDimensions.xSpacing +
                    getLinesCorrectionX(x, fullXRange) * layoutDirectionMultiplier,
            )
        }

        val axisLineExtend = if (itemPlacer.getShiftExtremeTicks(context)) {
            tickThickness
        } else {
            tickThickness.half
        }

        axisLine?.drawHorizontal(
            context = context,
            left = chartBounds.left - axisLineExtend,
            right = chartBounds.right + axisLineExtend,
            centerY = (if (position.isBottom) bounds.top else bounds.bottom) + axisThickness.half,
        )

        title?.let { title ->
            titleComponent?.drawText(
                context = context,
                textX = bounds.centerX(),
                textY = if (position.isTop) bounds.top else bounds.bottom,
                verticalPosition = if (position.isTop) VerticalPosition.Bottom else VerticalPosition.Top,
                maxTextWidth = bounds.width().toInt(),
                text = title,
            )
        }

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)

        drawGuidelines(baseCanvasX, fullXRange, labelValues, lineValues)
    }

    private fun ChartDrawContext.drawGuidelines(
        baseCanvasX: Float,
        fullXRange: ClosedFloatingPointRange<Float>,
        labelValues: List<Float>,
        lineValues: List<Float>?,
    ) {
        val guideline = guideline ?: return
        val clipRestoreCount = canvas.save()
        canvas.clipRect(chartBounds)

        val chartValues = chartValuesManager.getChartValues()

        if (lineValues == null) {
            labelValues.forEach { x ->
                val canvasX = baseCanvasX + x / chartValues.xStep * horizontalDimensions.xSpacing *
                    layoutDirectionMultiplier

                guideline
                    .takeUnless { x.isBoundOf(fullXRange) }
                    ?.drawVertical(this, chartBounds.top, chartBounds.bottom, canvasX)
            }
        } else {
            lineValues.forEach { x ->
                val canvasX = baseCanvasX + x / chartValues.xStep * horizontalDimensions.xSpacing +
                    getLinesCorrectionX(x, fullXRange) * layoutDirectionMultiplier

                guideline
                    .takeUnless { x.isBoundOf(fullXRange) }
                    ?.drawVertical(this, chartBounds.top, chartBounds.bottom, canvasX)
            }
        }

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    private fun ChartDrawContext.getLinesCorrectionX(
        entryX: Float,
        fullXRange: ClosedFloatingPointRange<Float>,
    ): Float =
        when {
            itemPlacer.getShiftExtremeTicks(this).not() -> 0f
            entryX == fullXRange.start -> -tickThickness.half
            entryX == fullXRange.endInclusive -> tickThickness.half
            else -> 0f
        }

    override fun drawAboveChart(context: ChartDrawContext): Unit = Unit

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ): Unit = with(context) {
        val scaledHorizontalDimensions = horizontalDimensions.scaled(chartScale)
        with(outInsets) {
            start = itemPlacer.getStartHorizontalAxisInset(context, scaledHorizontalDimensions, tickThickness)
            end = itemPlacer.getEndHorizontalAxisInset(context, scaledHorizontalDimensions, tickThickness)
            top = if (position.isTop) getDesiredHeight(context, scaledHorizontalDimensions) else 0f
            bottom = if (position.isBottom) getDesiredHeight(context, scaledHorizontalDimensions) else 0f
        }
    }

    private fun MeasureContext.getFullXRange(
        horizontalDimensions: HorizontalDimensions,
    ): ClosedFloatingPointRange<Float> = with(horizontalDimensions) {
        val chartValues = chartValuesManager.getChartValues()
        val start = chartValues.minX - startPadding / xSpacing * chartValues.xStep
        val end = chartValues.maxX + endPadding / xSpacing * chartValues.xStep
        start..end
    }

    private fun getDesiredHeight(
        context: MeasureContext,
        horizontalDimensions: HorizontalDimensions,
    ): Float = with(context) {
        val chartValues = chartValuesManager.getChartValues()
        val fullXRange = getFullXRange(horizontalDimensions)
        val labelClearance = itemPlacer.getMeasuredLabelClearance(this, horizontalDimensions, fullXRange)
        val maxLabelWidth = (labelClearance * horizontalDimensions.xSpacing).toInt()

        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto -> {
                val labelHeight = label?.let { label ->
                    itemPlacer
                        .getMeasuredLabelValues(this, horizontalDimensions, fullXRange)
                        .map { valueFormatter.formatValue(it, chartValues) }
                        .maxOf { labelText ->
                            label.getHeight(
                                context = this,
                                text = labelText,
                                width = maxLabelWidth,
                                rotationDegrees = labelRotationDegrees,
                            ).orZero
                        }
                }.orZero
                val titleComponentHeight = title?.let { title ->
                    titleComponent?.getHeight(
                        context = context,
                        width = bounds.width().toInt(),
                        text = title,
                    )
                }.orZero
                (labelHeight + titleComponentHeight + (if (position.isBottom) axisThickness else 0f) + tickLength)
                    .coerceAtMost(maximumValue = canvasBounds.height() / MAX_HEIGHT_DIVISOR)
                    .coerceIn(minimumValue = constraint.minSizeDp.pixels, maximumValue = constraint.maxSizeDp.pixels)
            }
            is SizeConstraint.Exact -> constraint.sizeDp.pixels
            is SizeConstraint.Fraction -> canvasBounds.height() * constraint.fraction
            is SizeConstraint.TextWidth -> label?.getHeight(
                context = this,
                text = constraint.text,
                width = maxLabelWidth,
                rotationDegrees = labelRotationDegrees,
            ).orZero
        }
    }

    /**
     * A subclass of [Axis.Builder] used to build [HorizontalAxis] instances.
     */
    public class Builder<Position : AxisPosition.Horizontal>(
        builder: Axis.Builder<Position>? = null,
    ) : Axis.Builder<Position>(builder) {

        /**
         * How often labels (and their corresponding ticks and guidelines) should be drawn.
         */
        @Deprecated(
            """`labelSpacing` is being replaced by `AxisItemPlacer.Horizontal`. Create a base implementation with the
                desired spacing via `AxisItemPlacer.Horizontal.default`, and use the `itemPlacer` field to apply it to
                this `HorizontalAxis.Builder`.""",
        )
        public var labelSpacing: Int = 1
            set(value) {
                field = value
                @Suppress("DEPRECATION")
                itemPlacer = AxisItemPlacer.Horizontal.default(spacing = value, offset = labelOffset)
            }

        /**
         * The number of labels (and, for [HorizontalLayout.FullWidth], their corresponding ticks and guidelines) to
         * skip from the start.
         */
        @Deprecated(
            """`labelOffset` is being replaced by `AxisItemPlacer.Horizontal`. Create a base implementation with the
                desired offset via `AxisItemPlacer.Horizontal.default`, and use the `itemPlacer` field to apply it to
                this `HorizontalAxis.Builder`.""",
        )
        public var labelOffset: Int = 0
            set(value) {
                field = value
                @Suppress("DEPRECATION")
                itemPlacer = AxisItemPlacer.Horizontal.default(spacing = labelSpacing, offset = value)
            }

        /**
         * Determines for what _x_ values the [HorizontalAxis] is to display labels, ticks, and guidelines.
         */
        @Suppress("DEPRECATION")
        public var itemPlacer: AxisItemPlacer.Horizontal = AxisItemPlacer.Horizontal.default(labelSpacing, labelOffset)

        /**
         * Creates a [HorizontalAxis] instance with the properties from this [Builder].
         */
        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : Position> build(): HorizontalAxis<T> {
            val position = when (T::class.java) {
                AxisPosition.Horizontal.Top::class.java -> AxisPosition.Horizontal.Top
                AxisPosition.Horizontal.Bottom::class.java -> AxisPosition.Horizontal.Bottom
                else -> throw UnknownAxisPositionException(T::class.java)
            } as Position
            @Suppress("DEPRECATION")
            return setTo(HorizontalAxis(position = position)).also { axis ->
                axis.labelSpacing = labelSpacing
                axis.labelOffset = labelOffset
                axis.itemPlacer = itemPlacer
            } as HorizontalAxis<T>
        }
    }

    internal companion object {
        const val MAX_HEIGHT_DIVISOR = 3f
    }
}

/**
 * A convenience function that creates a [HorizontalAxis] instance.
 *
 * @param block a lambda function yielding [HorizontalAxis.Builder] as its receiver.
 */
public inline fun <reified Position : AxisPosition.Horizontal> createHorizontalAxis(
    block: HorizontalAxis.Builder<Position>.() -> Unit = {},
): HorizontalAxis<Position> = HorizontalAxis.Builder<Position>().apply(block).build()
