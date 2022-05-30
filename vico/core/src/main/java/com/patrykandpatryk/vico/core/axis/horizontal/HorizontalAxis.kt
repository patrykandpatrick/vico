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

package com.patrykandpatryk.vico.core.axis.horizontal

import kotlin.math.ceil
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.setTo
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.insets.Insets
import com.patrykandpatryk.vico.core.component.text.VerticalPosition
import com.patrykandpatryk.vico.core.context.DrawContext
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.context.layoutDirectionMultiplier
import com.patrykandpatryk.vico.core.extension.getStart
import com.patrykandpatryk.vico.core.extension.half
import com.patrykandpatryk.vico.core.extension.orZero
import com.patrykandpatryk.vico.core.throwable.UnknownAxisPositionException
import kotlin.math.abs

/**
 * A subclass of [com.patrykandpatryk.vico.core.axis.AxisRenderer] used for horizontal axes, used either at the top
 * or at the bottom of a chart. It uses [Axis] as its base implementation.
 *
 * @see com.patrykandpatryk.vico.core.axis.AxisRenderer
 * @see Axis
 */
public class HorizontalAxis<Position : AxisPosition.Horizontal>(
    override val position: Position,
) : Axis<Position>() {

    private val AxisPosition.Horizontal.textVerticalPosition: VerticalPosition
        get() = if (isBottom) VerticalPosition.Bottom else VerticalPosition.Top

    /**
     * Defines the tick placement.
     */
    public var tickType: TickType = TickType.Minor

    override fun drawBehindChart(context: ChartDrawContext): Unit = with(context) {
        val clipRestoreCount = canvas.save()
        val tickMarkTop = if (position.isBottom) bounds.top else bounds.bottom - tickLength
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val chartValues = chartValuesManager.getChartValues()
        val step = chartValues.stepX

        canvas.clipRect(
            bounds.left - if (tickType == TickType.Minor) tickThickness.half else 0f,
            minOf(bounds.top, chartBounds.top),
            bounds.right + if (tickType == TickType.Minor) tickThickness.half else 0f,
            maxOf(bounds.bottom, chartBounds.bottom),
        )

        val entryLength = getEntryLength(segmentProperties.segmentWidth)
        val tickCount = tickType.getTickCount(entryLength)
        val tickDrawStep = segmentProperties.segmentWidth
        val scrollAdjustment = (abs(x = horizontalScroll) / tickDrawStep).toInt()
        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop
        var textCenter = bounds.getStart(isLtr = isLtr) + layoutDirectionMultiplier *
            (tickDrawStep.half + tickDrawStep * scrollAdjustment) - horizontalScroll

        var tickCenter = getTickDrawCenter(tickType, horizontalScroll, tickDrawStep, scrollAdjustment, textCenter)
        var valueIndex: Float = chartValues.minX + scrollAdjustment * step

        for (index in 0 until tickCount) {
            guideline?.takeIf {
                it.fitsInVertical(
                    context = context,
                    top = chartBounds.top,
                    bottom = chartBounds.bottom,
                    centerX = tickCenter,
                    boundingBox = chartBounds,
                )
            }?.drawVertical(context = context, top = chartBounds.top, bottom = chartBounds.bottom, centerX = tickCenter)

            tick?.drawVertical(context = context, top = tickMarkTop, bottom = tickMarkBottom, centerX = tickCenter)

            if (index < entryLength) {
                label?.drawText(
                    context = context,
                    text = valueFormatter.formatValue(valueIndex, chartValues),
                    textX = textCenter,
                    textY = textY,
                    verticalPosition = position.textVerticalPosition,
                    maxTextWidth = tickDrawStep.toInt(),
                    rotationDegrees = labelRotationDegrees,
                )

                valueIndex += step
            }

            tickCenter += layoutDirectionMultiplier * tickDrawStep
            textCenter += layoutDirectionMultiplier * tickDrawStep
        }

        axisLine?.drawHorizontal(
            context = context,
            left = chartBounds.left,
            right = chartBounds.right,
            centerY = (if (position.isBottom) bounds.top else bounds.bottom) + axisThickness.half,
        )

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    override fun drawAboveChart(context: ChartDrawContext): Unit = Unit

    private fun getEntryLength(segmentWidth: Float) =
        ceil(bounds.width() / segmentWidth).toInt() + 1

    private fun TickType.getTickCount(entryLength: Int) = when (this) {
        TickType.Minor -> entryLength + 1
        TickType.Major -> entryLength
    }

    private fun DrawContext.getTickDrawCenter(
        tickType: TickType,
        scrollX: Float,
        tickDrawStep: Float,
        scrollAdjustment: Int,
        textDrawCenter: Float,
    ) = when (tickType) {
        TickType.Major -> textDrawCenter
        TickType.Minor -> bounds.getStart(isLtr = isLtr) +
            layoutDirectionMultiplier * (tickDrawStep * scrollAdjustment) - scrollX
    }

    override fun getInsets(
        context: ChartDrawContext,
        outInsets: Insets,
    ): Unit = with(context) {
        with(outInsets) {
            setHorizontal(
                if (tickType == TickType.Minor) tickThickness.half
                else 0f
            )
            top = if (position.isTop) getDesiredHeight(context) else 0f
            bottom = if (position.isBottom) getDesiredHeight(context) else 0f
        }
    }

    private fun getDesiredHeight(context: ChartDrawContext): Float = with(context) {
        val labelWidth by lazy { segmentProperties.segmentWidth.toInt() }
        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto -> (
                label?.let { label ->
                    getLabelsToMeasure().maxOf { labelText ->
                        label.getHeight(
                            context = this,
                            text = labelText,
                            width = labelWidth,
                            rotationDegrees = labelRotationDegrees,
                        ).orZero
                    }
                }.orZero + (if (position.isBottom) axisThickness else 0f) + tickLength
                ).coerceIn(constraint.minSizeDp.pixels, constraint.maxSizeDp.pixels)
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

    private fun MeasureContext.getLabelsToMeasure(): List<CharSequence> {

        val chartValues = chartValuesManager.getChartValues()

        return listOf(
            chartValues.minX,
            (chartValues.maxX - chartValues.minX).half,
            chartValues.maxX,
        ).map { x -> valueFormatter.formatValue(value = x, chartValues = chartValues) }
    }

    /**
     * Defines the tick placement.
     */
    public enum class TickType {
        /**
         * The tick will be placed at the edges of each section on the horizontal axis.
         *```
         * —————————————
         * |   |   |   |
         *   1   2   3
         * ```
         */
        Minor,

        /**
         * The tick will be placed at the center of each section on the horizontal axis.
         *```
         * —————————————
         *   |   |   |
         *   1   2   3
         * ```
         */
        Major,
    }

    /**
     * A subclass of base [Axis.Builder] used to build instances of [HorizontalAxis].
     */
    public class Builder<Position : AxisPosition.Horizontal>(
        builder: Axis.Builder<Position>? = null,
    ) : Axis.Builder<Position>(builder) {

        /**
         * Defines the tick placement.
         */
        public var tickType: TickType = TickType.Minor

        /**
         * Creates an instance of [HorizontalAxis] using the properties set in this [Builder].
         */
        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : Position> build(): HorizontalAxis<T> {
            val position = when (T::class.java) {
                AxisPosition.Horizontal.Top::class.java -> AxisPosition.Horizontal.Top
                AxisPosition.Horizontal.Bottom::class.java -> AxisPosition.Horizontal.Bottom
                else -> throw UnknownAxisPositionException(T::class.java)
            } as Position
            return setTo(HorizontalAxis(position = position)).also { axis ->
                axis.tickType = tickType
            } as HorizontalAxis<T>
        }
    }
}

/**
 * A convenience function that creates an instance of [HorizontalAxis].
 *
 * @param block a lambda function yielding [HorizontalAxis.Builder] as its receiver.
 */
public inline fun <reified Position : AxisPosition.Horizontal> createHorizontalAxis(
    block: HorizontalAxis.Builder<Position>.() -> Unit = {},
): HorizontalAxis<Position> = HorizontalAxis.Builder<Position>().apply(block).build()
