/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

import com.patrykandpatrick.vico.core.DEF_LABEL_COUNT
import com.patrykandpatrick.vico.core.DEF_LABEL_SPACING
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.setTo
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Outside
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.VerticalLabelPosition.Center
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.HorizontalInsets
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.text.HorizontalPosition
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.getOrPutExtra
import com.patrykandpatrick.vico.core.extension.getEnd
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.translate
import com.patrykandpatrick.vico.core.throwable.UnknownAxisPositionException

private const val LABELS_KEY = "labels"
private const val TITLE_ABS_ROTATION_DEGREES = 90f

/**
 * An implementation of [AxisRenderer] used for vertical axes. This class extends [Axis].
 *
 * @see AxisRenderer
 * @see Axis
 */
public class VerticalAxis<Position : AxisPosition.Vertical>(
    override val position: Position,
) : Axis<Position>() {

    private val areLabelsOutsideAtStartOrInsideAtEnd
        get() = horizontalLabelPosition == Outside && position is AxisPosition.Vertical.Start ||
            horizontalLabelPosition == Inside && position is AxisPosition.Vertical.End

    private val textHorizontalPosition: HorizontalPosition
        get() = if (areLabelsOutsideAtStartOrInsideAtEnd) HorizontalPosition.Start else HorizontalPosition.End

    /**
     * The maximum label count.
     */
    public var maxLabelCount: Int = DEF_LABEL_COUNT

    /**
     * The label spacing (in dp).
     */
    public var labelSpacing: Float = DEF_LABEL_SPACING

    /**
     * Defines the horizontal position of each axis label relative to the axis line.
     */
    public var horizontalLabelPosition: HorizontalLabelPosition = Outside

    /**
     * Defines the vertical position of each axis label relative to its corresponding tick.
     */
    public var verticalLabelPosition: VerticalLabelPosition = Center

    override fun drawBehindChart(
        context: ChartDrawContext,
    ): Unit = with(context) {
        val drawLabelCount = getDrawLabelCount(bounds.height().toInt())

        val axisStep = bounds.height() / drawLabelCount

        var centerY: Float

        for (index in 0..drawLabelCount) {
            centerY = bounds.bottom - axisStep * index + guidelineThickness.half

            guideline?.takeIf {
                isNotInRestrictedBounds(
                    left = chartBounds.left,
                    top = centerY - guidelineThickness.half,
                    right = chartBounds.right,
                    bottom = centerY - guidelineThickness.half,
                )
            }?.drawHorizontal(
                context = context,
                left = chartBounds.left,
                right = chartBounds.right,
                centerY = centerY,
            )
        }
        axisLine?.drawVertical(
            context = context,
            top = bounds.top,
            bottom = bounds.bottom + axisThickness,
            centerX = if (position.isLeft(isLtr = isLtr)) bounds.right else bounds.left,
        )
    }

    override fun drawAboveChart(context: ChartDrawContext): Unit = with(context) {
        val label = label
        val labelCount = getDrawLabelCount(bounds.height().toInt())

        val labels = getLabels(labelCount)

        val tickLeftX = getTickLeftX()

        val tickRightX = tickLeftX + axisThickness.half + tickLength

        val labelX = if (areLabelsOutsideAtStartOrInsideAtEnd == isLtr) tickLeftX else tickRightX

        var tickCenterY: Float

        (0..labelCount).forEach { index ->
            tickCenterY = bounds.bottom - bounds.height() / labelCount * index + tickThickness.half

            tick?.drawHorizontal(
                context = context,
                left = tickLeftX,
                right = tickRightX,
                centerY = tickCenterY,
            )

            label ?: return@forEach
            val labelText = labels.getOrNull(index) ?: return@forEach
            drawLabel(
                label = label,
                labelText = labelText,
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

    private fun ChartDrawContext.drawLabel(
        label: TextComponent,
        labelText: CharSequence,
        labelX: Float,
        tickCenterY: Float,
    ) {
        val textBounds = label.getTextBounds(this, labelText, rotationDegrees = labelRotationDegrees).apply {
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
                maxTextWidth = when (sizeConstraint) {
                    // Let the `TextComponent` use as much width as it needs, based on the measuring phase.
                    is SizeConstraint.Auto -> Int.MAX_VALUE
                    else -> (bounds.width() - tickLength - axisThickness.half).toInt()
                },
            )
        }
    }

    private fun MeasureContext.getTickLeftX(): Float {
        val onLeft = position.isLeft(isLtr = isLtr)
        val base = if (onLeft) bounds.right else bounds.left
        return if (onLeft == (horizontalLabelPosition == Outside)) base - axisThickness.half - tickLength else base
    }

    private fun MeasureContext.getDrawLabelCount(availableHeight: Int): Int {
        label?.let { label ->

            val chartValues = chartValuesManager.getChartValues(position)

            fun getLabelHeight(value: Float): Float =
                label.getHeight(
                    context = this,
                    text = valueFormatter.formatValue(value, chartValues),
                    rotationDegrees = labelRotationDegrees,
                )

            val avgHeight = arrayOf(
                getLabelHeight(chartValues.minY),
                getLabelHeight((chartValues.maxY + chartValues.minY) / 2),
                getLabelHeight(chartValues.maxY),
            ).maxOrNull().orZero

            var result = 0f
            for (count in 0 until maxLabelCount) {
                if (result + avgHeight > availableHeight) return count
                result += avgHeight
            }
        }
        return maxLabelCount
    }

    private fun MeasureContext.getLabels(
        maxLabelCount: Int = this@VerticalAxis.maxLabelCount,
    ): List<CharSequence> {
        val chartValues = chartValuesManager.getChartValues(position)
        val cacheKey = LABELS_KEY + position + maxLabelCount
        return getOrPutExtra(key = cacheKey) {
            labels.clear()
            val step = (chartValues.maxY - chartValues.minY) / maxLabelCount
            for (index in 0..maxLabelCount) {
                val value = chartValues.minY + step * index
                labels += valueFormatter.formatValue(value, chartValues)
            }
            labels
        }
    }

    override fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit = with(context) {
        val labels = getLabels(maxLabelCount = getDrawLabelCount(availableHeight.toInt()))

        val desiredWidth = getDesiredWidth(context, labels)

        outInsets.set(
            start = if (position.isStart) desiredWidth else 0f,
            end = if (position.isEnd) desiredWidth else 0f,
        )
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(context) {
        val labelHeight = label?.getHeight(context = context).orZero
        val lineThickness = maxOf(axisThickness, tickThickness)
        when (verticalLabelPosition) {
            Center -> outInsets.set(
                top = labelHeight.half - lineThickness,
                bottom = labelHeight.half,
            )
            VerticalLabelPosition.Top -> outInsets.set(
                top = labelHeight - lineThickness,
                bottom = lineThickness,
            )
            VerticalLabelPosition.Bottom -> outInsets.set(
                top = lineThickness.half,
                bottom = labelHeight,
            )
        }
    }

    /**
     * Calculates the optimal width for this [VerticalAxis], accounting for the value of [sizeConstraint].
     */
    private fun getDesiredWidth(
        context: MeasureContext,
        labels: List<CharSequence>,
    ): Float = with(context) {
        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto -> {
                val titleComponentWidth = title?.let { title ->
                    titleComponent?.getWidth(
                        context = this,
                        text = title,
                        rotationDegrees = TITLE_ABS_ROTATION_DEGREES,
                        height = bounds.height().toInt(),
                    )
                }.orZero
                (getMaxLabelWidth(labels = labels) + titleComponentWidth + axisThickness.half + tickLength)
                    .coerceIn(minimumValue = constraint.minSizeDp.pixels, maximumValue = constraint.maxSizeDp.pixels)
            }
            is SizeConstraint.Exact -> constraint.sizeDp.pixels
            is SizeConstraint.Fraction -> canvasBounds.width() * constraint.fraction
            is SizeConstraint.TextWidth -> label?.getWidth(
                context = this,
                text = constraint.text,
                rotationDegrees = labelRotationDegrees,
            ).orZero + tickLength + axisThickness.half
        }
    }

    private fun MeasureContext.getMaxLabelWidth(labels: List<CharSequence>): Float = when (horizontalLabelPosition) {
        Outside -> label?.let { label ->
            labels.maxOfOrNull { label.getWidth(this, it, rotationDegrees = labelRotationDegrees) }
        }.orZero
        Inside -> 0f
    }

    /**
     * Defines the horizontal position of each of a vertical axis’s labels relative to the axis line.
     */
    public enum class HorizontalLabelPosition {
        Outside, Inside
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
     * A subclass of [Axis.Builder] used to build [VerticalAxis] instances.
     */
    public class Builder<Position : AxisPosition.Vertical>(
        builder: Axis.Builder<Position>? = null,
    ) : Axis.Builder<Position>(builder) {
        /**
         * The maximum label count.
         */
        public var maxLabelCount: Int = DEF_LABEL_COUNT

        /**
         * The label spacing (in dp).
         */
        public var labelSpacing: Float = DEF_LABEL_SPACING

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
            val position = when (T::class.java) {
                AxisPosition.Vertical.Start::class.java -> AxisPosition.Vertical.Start
                AxisPosition.Vertical.End::class.java -> AxisPosition.Vertical.End
                else -> throw UnknownAxisPositionException(T::class.java)
            } as Position
            return setTo(VerticalAxis(position)).also { axis ->
                axis.maxLabelCount = maxLabelCount
                axis.labelSpacing = labelSpacing
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
