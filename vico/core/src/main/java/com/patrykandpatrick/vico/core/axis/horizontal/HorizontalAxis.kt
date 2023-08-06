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
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.setTo
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.CartesianDrawContext
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.throwable.UnknownAxisPositionException
import kotlin.math.abs
import kotlin.math.ceil
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
    public var labelSpacing: Int = 1

    /**
     * The number of labels (and, for [HorizontalLayout.FullWidth], their corresponding ticks and guidelines) to skip
     * from the start.
     */
    public var labelOffset: Int = 0

    override fun drawBehindChart(context: CartesianChartDrawContext): Unit = with(context) {
        val clipRestoreCount = canvas.save()
        val tickMarkTop = if (position.isBottom) bounds.top else bounds.bottom - tickLength
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val chartValues = chartValuesManager.getChartValues()

        canvas.clipRect(
            bounds.left - horizontalLayout.getStartHorizontalAxisInset(horizontalDimensions, tickThickness),
            minOf(bounds.top, chartBounds.top),
            bounds.right + horizontalLayout.getEndHorizontalAxisInset(horizontalDimensions, tickThickness),
            maxOf(bounds.bottom, chartBounds.bottom),
        )

        val tickDrawStep = horizontalDimensions.xSpacing
        val scrollAdjustment =
            (abs((horizontalScroll - horizontalDimensions.startPadding).coerceAtLeast(0f)) / tickDrawStep).toInt()
        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop

        var textCenter = bounds.getStart(isLtr = isLtr) + layoutDirectionMultiplier * tickDrawStep * scrollAdjustment -
            horizontalScroll + horizontalDimensions.startPadding

        var tickCenter = getTickDrawCenter(horizontalScroll, tickDrawStep, scrollAdjustment, textCenter)

        forEachEntity(
            scrollAdjustment = scrollAdjustment,
            xRange = chartValues.minX..chartValues.maxX,
        ) { x, shouldDrawLines, shouldDrawLabel ->

            guideline
                ?.takeIf {
                    shouldDrawLines &&
                        it.fitsInVertical(
                            context = context,
                            top = chartBounds.top,
                            bottom = chartBounds.bottom,
                            centerX = tickCenter,
                            boundingBox = chartBounds,
                        )
                }?.drawVertical(
                    context = context,
                    top = chartBounds.top,
                    bottom = chartBounds.bottom,
                    centerX = tickCenter,
                )

            tick
                .takeIf { shouldDrawLines }
                ?.drawVertical(context = context, top = tickMarkTop, bottom = tickMarkBottom, centerX = tickCenter)

            label
                .takeIf { shouldDrawLabel }
                ?.drawText(
                    context = context,
                    text = valueFormatter.formatValue(x, chartValues),
                    textX = textCenter,
                    textY = textY,
                    verticalPosition = position.textVerticalPosition,
                    maxTextWidth = getMaxLabelWidth(x),
                    maxTextHeight = (bounds.height() - tickLength - axisThickness.half).toInt(),
                    rotationDegrees = labelRotationDegrees,
                )

            tickCenter += layoutDirectionMultiplier * tickDrawStep
            textCenter += layoutDirectionMultiplier * tickDrawStep
        }

        axisLine?.drawHorizontal(
            context = context,
            left = chartBounds.left,
            right = chartBounds.right,
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
    }

    override fun drawAboveChart(context: CartesianChartDrawContext): Unit = Unit

    private fun getEntryLength(xSpacing: Float) =
        ceil(bounds.width() / xSpacing).toInt() + 1

    private inline fun CartesianChartDrawContext.forEachEntity(
        scrollAdjustment: Int,
        xRange: ClosedFloatingPointRange<Float>,
        action: (x: Float, shouldDrawLines: Boolean, shouldDrawLabel: Boolean) -> Unit,
    ) {
        val chartValues = chartValuesManager.getChartValues()
        val entryLength = getEntryLength(horizontalDimensions.xSpacing)

        for (index in 0 until horizontalLayout.getHorizontalAxisLabelCount(entryLength)) {
            val relativeX = (scrollAdjustment + index) * chartValues.xStep
            val x = relativeX + xRange.start

            val firstLabelConditionsMet = relativeX != 0f || labelOffset == 0 &&
                (horizontalDimensions.startPadding > 0 || horizontalLayout is HorizontalLayout.Segmented)

            val lastLabelConditionsMet = x != xRange.endInclusive ||
                horizontalDimensions.endPadding > 0 || horizontalLayout is HorizontalLayout.Segmented

            val spacingAndOffsetConditionsMet = relativeX / chartValues.xStep >= labelOffset &&
                (relativeX / chartValues.xStep - labelOffset) % labelSpacing == 0f

            action(
                x,
                horizontalLayout is HorizontalLayout.Segmented || firstLabelConditionsMet && lastLabelConditionsMet &&
                    spacingAndOffsetConditionsMet,
                firstLabelConditionsMet && lastLabelConditionsMet && spacingAndOffsetConditionsMet && x in xRange,
            )
        }
    }

    private fun CartesianDrawContext.getTickDrawCenter(
        scrollX: Float,
        tickDrawStep: Float,
        scrollAdjustment: Int,
        textDrawCenter: Float,
    ) = when (horizontalLayout) {
        is HorizontalLayout.Segmented -> bounds.getStart(isLtr = isLtr) + tickDrawStep * labelOffset +
            layoutDirectionMultiplier * tickDrawStep * scrollAdjustment - scrollX

        is HorizontalLayout.FullWidth -> textDrawCenter
    }

    override fun getInsets(
        context: CartesianMeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ): Unit = with(context) {
        val scaledHorizontalDimensions = horizontalDimensions.scaled(chartScale)
        with(outInsets) {
            start = horizontalLayout.getStartHorizontalAxisInset(scaledHorizontalDimensions, tickThickness)
            end = horizontalLayout.getEndHorizontalAxisInset(scaledHorizontalDimensions, tickThickness)
            top = if (position.isTop) getDesiredHeight(context, scaledHorizontalDimensions) else 0f
            bottom = if (position.isBottom) getDesiredHeight(context, scaledHorizontalDimensions) else 0f
        }
    }

    private fun getDesiredHeight(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
    ): Float = with(context) {
        val labelWidth =
            if (isHorizontalScrollEnabled) {
                horizontalDimensions.xSpacing.toInt() * labelSpacing
            } else {
                Int.MAX_VALUE
            }

        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto -> {
                val labelHeight = label?.let { label ->
                    getLabelsToMeasure().maxOf { labelText ->
                        label.getHeight(
                            context = this,
                            text = labelText,
                            width = labelWidth,
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
                width = labelWidth,
                rotationDegrees = labelRotationDegrees,
            ).orZero
        }
    }

    private fun CartesianMeasureContext.getLabelsToMeasure(): List<CharSequence> {
        val chartValues = chartValuesManager.getChartValues()

        return listOf(
            chartValues.minX,
            (chartValues.maxX - chartValues.minX).half,
            chartValues.maxX,
        ).map { x -> valueFormatter.formatValue(value = x, chartValues = chartValues) }
    }

    private fun CartesianChartDrawContext.getMaxLabelWidth(x: Float): Int {
        val chartValues = chartValuesManager.getChartValues()
        val isFirst = ((x - chartValues.minX) / chartValues.xStep).toInt() == labelOffset
        val isLast = (chartValues.maxX - x) / chartValues.xStep < labelSpacing
        return when {
            horizontalLayout is HorizontalLayout.Segmented -> horizontalDimensions.xSpacing
            isFirst && isLast -> min(horizontalDimensions.startPadding, horizontalDimensions.endPadding).doubled

            isFirst -> (labelOffset * horizontalDimensions.xSpacing + horizontalDimensions.startPadding)
                .doubled
                .coerceAtMost(labelSpacing * horizontalDimensions.xSpacing)

            isLast -> ((chartValues.maxX - x) * horizontalDimensions.xSpacing + horizontalDimensions.endPadding)
                .doubled
                .coerceAtMost(labelSpacing * horizontalDimensions.xSpacing)

            else -> labelSpacing * horizontalDimensions.xSpacing
        }.toInt()
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
        public var labelSpacing: Int = 1

        /**
         * The number of labels (and, for [HorizontalLayout.FullWidth], their corresponding ticks and guidelines) to
         * skip from the start.
         */
        public var labelOffset: Int = 0

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
            return setTo(HorizontalAxis(position = position)).also { axis ->
                axis.labelSpacing = labelSpacing
                axis.labelOffset = labelOffset
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
