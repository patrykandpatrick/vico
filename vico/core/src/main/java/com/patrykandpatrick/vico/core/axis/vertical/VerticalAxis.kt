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

package com.patrykandpatrick.vico.core.axis.vertical

import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.BaseAxis
import com.patrykandpatrick.vico.core.axis.setTo
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Outside
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.VerticalLabelPosition.Center
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.HorizontalInsets
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.component.text.HorizontalPosition
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.getEnd
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.translate

private const val TITLE_ABS_ROTATION_DEGREES = 90f

/**
 * An implementation of [Axis] used for vertical axes. This class extends [BaseAxis].
 *
 * @see Axis
 * @see BaseAxis
 */
public class VerticalAxis<Position : AxisPosition.Vertical>(
    override val position: Position,
) : BaseAxis<Position>() {
    private val areLabelsOutsideAtStartOrInsideAtEnd
        get() =
            horizontalLabelPosition == Outside && position is AxisPosition.Vertical.Start ||
                horizontalLabelPosition == Inside && position is AxisPosition.Vertical.End

    private val textHorizontalPosition: HorizontalPosition
        get() = if (areLabelsOutsideAtStartOrInsideAtEnd) HorizontalPosition.Start else HorizontalPosition.End

    /**
     * Determines for what _y_ values this [VerticalAxis] is to display labels, ticks, and guidelines.
     */
    public var itemPlacer: AxisItemPlacer.Vertical = AxisItemPlacer.Vertical.step()

    /**
     * Defines the horizontal position of each axis label relative to the axis line.
     */
    public var horizontalLabelPosition: HorizontalLabelPosition = Outside

    /**
     * Defines the vertical position of each axis label relative to its corresponding tick.
     */
    public var verticalLabelPosition: VerticalLabelPosition = Center

    override fun drawBehindChart(context: ChartDrawContext): Unit =
        with(context) {
            var centerY: Float
            val yRange = chartValues.getYRange(position)
            val maxLabelHeight = getMaxLabelHeight()
            val lineValues =
                itemPlacer.getLineValues(this, bounds.height(), maxLabelHeight, position)
                    ?: itemPlacer.getLabelValues(this, bounds.height(), maxLabelHeight, position)

            lineValues.forEach { lineValue ->
                centerY = bounds.bottom - bounds.height() * (lineValue - yRange.minY) / yRange.length +
                    getLineCanvasYCorrection(guidelineThickness, lineValue)

                guideline?.takeIf {
                    isNotInRestrictedBounds(
                        left = chartBounds.left,
                        top = centerY - guidelineThickness.half,
                        right = chartBounds.right,
                        bottom = centerY + guidelineThickness.half,
                    )
                }?.drawHorizontal(
                    context = context,
                    left = chartBounds.left,
                    right = chartBounds.right,
                    centerY = centerY,
                )
            }
            val axisLineExtensionLength = if (itemPlacer.getShiftTopLines(this)) tickThickness else 0f
            axisLine?.drawVertical(
                context = context,
                top = bounds.top - axisLineExtensionLength,
                bottom = bounds.bottom + axisLineExtensionLength,
                centerX =
                    if (position.isLeft(isLtr = isLtr)) {
                        bounds.right - axisThickness.half
                    } else {
                        bounds.left + axisThickness.half
                    },
            )
        }

    override fun drawAboveChart(context: ChartDrawContext): Unit =
        with(context) {
            val label = label
            val labelValues = itemPlacer.getLabelValues(this, bounds.height(), getMaxLabelHeight(), position)
            val tickLeftX = getTickLeftX()
            val tickRightX = tickLeftX + axisThickness + tickLength
            val labelX = if (areLabelsOutsideAtStartOrInsideAtEnd == isLtr) tickLeftX else tickRightX
            var tickCenterY: Float
            val yRange = chartValues.getYRange(position)

            labelValues.forEach { labelValue ->
                tickCenterY = bounds.bottom - bounds.height() * (labelValue - yRange.minY) / yRange.length +
                    getLineCanvasYCorrection(tickThickness, labelValue)

                tick?.drawHorizontal(
                    context = context,
                    left = tickLeftX,
                    right = tickRightX,
                    centerY = tickCenterY,
                )

                label ?: return@forEach
                drawLabel(
                    label = label,
                    labelText = valueFormatter.formatValue(labelValue, chartValues, position),
                    labelX = labelX,
                    tickCenterY = tickCenterY,
                )
            }

            title?.let { title ->
                titleComponent?.drawText(
                    context = this,
                    text = title,
                    textX = if (position.isStart) bounds.getStart(isLtr = isLtr) else bounds.getEnd(isLtr = isLtr),
                    textY = bounds.centerY(),
                    horizontalPosition = if (position.isStart) HorizontalPosition.End else HorizontalPosition.Start,
                    verticalPosition = VerticalPosition.Center,
                    rotationDegrees = TITLE_ABS_ROTATION_DEGREES * if (position.isStart) -1f else 1f,
                    maxTextHeight = bounds.height().toInt(),
                )
            }
        }

    override fun updateHorizontalDimensions(
        context: MeasureContext,
        horizontalDimensions: MutableHorizontalDimensions,
    ): Unit = Unit

    private fun ChartDrawContext.drawLabel(
        label: TextComponent,
        labelText: CharSequence,
        labelX: Float,
        tickCenterY: Float,
    ) {
        val textBounds =
            label.getTextBounds(this, labelText, rotationDegrees = labelRotationDegrees).apply {
                translate(
                    x = labelX,
                    y = tickCenterY - centerY(),
                )
            }

        if (
            horizontalLabelPosition == Outside ||
            isNotInRestrictedBounds(
                left = textBounds.left,
                top = textBounds.top,
                right = textBounds.right,
                bottom = textBounds.bottom,
            )
        ) {
            label.drawText(
                context = this,
                text = labelText,
                textX = labelX,
                textY = tickCenterY,
                horizontalPosition = textHorizontalPosition,
                verticalPosition = verticalLabelPosition.textPosition,
                rotationDegrees = labelRotationDegrees,
                maxTextWidth =
                    when (sizeConstraint) {
                        // Let the `TextComponent` use as much width as it needs, based on the measuring phase.
                        is SizeConstraint.Auto -> Int.MAX_VALUE
                        else -> (bounds.width() - tickLength - axisThickness).toInt()
                    },
            )
        }
    }

    private fun MeasureContext.getTickLeftX(): Float {
        val onLeft = position.isLeft(isLtr = isLtr)
        val base = if (onLeft) bounds.right else bounds.left
        return when {
            onLeft && horizontalLabelPosition == Outside -> base - axisThickness - tickLength
            onLeft && horizontalLabelPosition == Inside -> base - axisThickness
            horizontalLabelPosition == Outside -> base
            horizontalLabelPosition == Inside -> base - tickLength
            else -> error("Unexpected combination of axis position and label position")
        }
    }

    override fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit =
        with(context) {
            val desiredWidth = getDesiredWidth(availableHeight)

            outInsets.set(
                start = if (position.isStart) desiredWidth else 0f,
                end = if (position.isEnd) desiredWidth else 0f,
            )
        }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ): Unit =
        with(context) {
            val maxLabelHeight = getMaxLabelHeight()
            val maxLineThickness = maxOf(axisThickness, tickThickness)
            outInsets.set(
                top =
                    itemPlacer.getTopVerticalAxisInset(
                        context,
                        verticalLabelPosition,
                        maxLabelHeight,
                        maxLineThickness,
                    ),
                bottom =
                    itemPlacer.getBottomVerticalAxisInset(
                        context,
                        verticalLabelPosition,
                        maxLabelHeight,
                        maxLineThickness,
                    ),
            )
        }

    /**
     * Calculates the optimal width for this [VerticalAxis], accounting for the value of [sizeConstraint].
     */
    private fun MeasureContext.getDesiredWidth(height: Float) =
        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto -> {
                val titleComponentWidth =
                    title?.let { title ->
                        titleComponent?.getWidth(
                            context = this,
                            text = title,
                            rotationDegrees = TITLE_ABS_ROTATION_DEGREES,
                            height = bounds.height().toInt(),
                        )
                    }.orZero
                val labelSpace =
                    when (horizontalLabelPosition) {
                        Outside -> getMaxLabelWidth(height)
                        Inside -> 0f
                    }
                (labelSpace + titleComponentWidth + axisThickness + tickLength)
                    .coerceIn(minimumValue = constraint.minSizeDp.pixels, maximumValue = constraint.maxSizeDp.pixels)
            }
            is SizeConstraint.Exact -> constraint.sizeDp.pixels
            is SizeConstraint.Fraction -> canvasBounds.width() * constraint.fraction
            is SizeConstraint.TextWidth ->
                label?.getWidth(
                    context = this,
                    text = constraint.text,
                    rotationDegrees = labelRotationDegrees,
                ).orZero + tickLength + axisThickness.half
        }

    private fun MeasureContext.getMaxLabelHeight() =
        label?.let { label ->
            itemPlacer
                .getHeightMeasurementLabelValues(this, position)
                .maxOfOrNull { value ->
                    label.getHeight(this, valueFormatter.formatValue(value, chartValues, position))
                }
        }.orZero

    private fun MeasureContext.getMaxLabelWidth(axisHeight: Float) =
        label?.let { label ->
            itemPlacer
                .getWidthMeasurementLabelValues(this, axisHeight, getMaxLabelHeight(), position)
                .maxOfOrNull { value -> label.getWidth(this, valueFormatter.formatValue(value, chartValues, position)) }
        }.orZero

    private fun ChartDrawContext.getLineCanvasYCorrection(
        thickness: Float,
        y: Float,
    ) = if (y == chartValues.getYRange(position).maxY && itemPlacer.getShiftTopLines(this)) {
        -thickness.half
    } else {
        thickness.half
    }

    /**
     * Defines the horizontal position of each of a vertical axis’s labels relative to the axis line.
     */
    public enum class HorizontalLabelPosition {
        Outside,
        Inside,
    }

    /**
     * Defines the vertical position of each of a horizontal axis’s labels relative to the label’s corresponding tick.
     *
     * @param textPosition the label position.
     *
     * @see VerticalPosition
     */
    public enum class VerticalLabelPosition(public val textPosition: VerticalPosition) {
        Center(VerticalPosition.Center),
        Top(VerticalPosition.Top),
        Bottom(VerticalPosition.Bottom),
    }

    /**
     * A subclass of [BaseAxis.Builder] used to build [VerticalAxis] instances.
     */
    public class Builder<Position : AxisPosition.Vertical>(
        builder: BaseAxis.Builder<Position>? = null,
    ) : BaseAxis.Builder<Position>(builder) {
        /**
         * Determines for what _y_ values this [VerticalAxis] is to display labels, ticks, and guidelines.
         */
        public var itemPlacer: AxisItemPlacer.Vertical = AxisItemPlacer.Vertical.step()

        /**
         * Defines the horizontal position of each axis label relative to the axis line.
         */
        public var horizontalLabelPosition: HorizontalLabelPosition = Outside

        /**
         * Defines the vertical position of each axis label relative to its corresponding tick.
         */
        public var verticalLabelPosition: VerticalLabelPosition = Center

        /**
         * Creates a [VerticalAxis] instance with the properties from this [Builder].
         */
        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : Position> build(): VerticalAxis<T> {
            val position =
                when (T::class.java) {
                    AxisPosition.Vertical.Start::class.java -> AxisPosition.Vertical.Start
                    AxisPosition.Vertical.End::class.java -> AxisPosition.Vertical.End
                    else -> throw IllegalStateException("Got unknown AxisPosition class ${T::class.java.name}")
                } as Position
            return setTo(VerticalAxis(position)).also { axis ->
                axis.itemPlacer = itemPlacer
                axis.horizontalLabelPosition = horizontalLabelPosition
                axis.verticalLabelPosition = verticalLabelPosition
            } as VerticalAxis<T>
        }
    }
}

/**
 * A convenience function that creates a [VerticalAxis] instance.
 *
 * @param block a lambda function yielding [VerticalAxis.Builder] as its receiver.
 */
public inline fun <reified Position : AxisPosition.Vertical> createVerticalAxis(
    block: VerticalAxis.Builder<Position>.() -> Unit = {},
): VerticalAxis<Position> = VerticalAxis.Builder<Position>().apply(block).build()
