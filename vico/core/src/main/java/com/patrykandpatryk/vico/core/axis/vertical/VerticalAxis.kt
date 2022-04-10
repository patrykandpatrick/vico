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

package com.patrykandpatryk.vico.core.axis.vertical

import com.patrykandpatryk.vico.core.DEF_LABEL_COUNT
import com.patrykandpatryk.vico.core.DEF_LABEL_SPACING
import com.patrykandpatryk.vico.core.axis.Axis
import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.model.ChartModel
import com.patrykandpatryk.vico.core.axis.setTo
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Outside
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis.VerticalLabelPosition.Center
import com.patrykandpatryk.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatryk.vico.core.chart.insets.HorizontalInsets
import com.patrykandpatryk.vico.core.chart.insets.Insets
import com.patrykandpatryk.vico.core.component.text.HorizontalPosition
import com.patrykandpatryk.vico.core.component.text.VerticalPosition
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.extension.half
import com.patrykandpatryk.vico.core.extension.orZero
import com.patrykandpatryk.vico.core.extension.translate
import com.patrykandpatryk.vico.core.throwable.UnknownAxisPositionException

private const val LABELS_KEY = "labels"

/**
 * A subclass of [com.patrykandpatryk.vico.core.axis.AxisRenderer] used for vertical axes, used either at the start or
 * at the end of end of a chart. It uses [Axis] as its base implementation.
 *
 * @see com.patrykandpatryk.vico.core.axis.AxisRenderer
 * @see Axis
 */
public class VerticalAxis<Position : AxisPosition.Vertical>(
    override val position: Position,
) : Axis<Position>() {

    private val isLeft = position.isLeft(isLtr)

    private val isLabelOutsideOnLeftOrInsideOnRight: Boolean
        get() = horizontalLabelPosition == Outside && isLeft ||
            horizontalLabelPosition == Inside && !isLeft

    private val textHorizontalPosition: HorizontalPosition
        get() = if (isLabelOutsideOnLeftOrInsideOnRight) HorizontalPosition.End else HorizontalPosition.Start

    /**
     * The maximum label count.
     */
    public var maxLabelCount: Int = DEF_LABEL_COUNT

    /**
     * The label spacing in dp.
     */
    public var labelSpacing: Float = DEF_LABEL_SPACING

    /**
     * Defines a horizontal label position relative to the axis line.
     */
    public var horizontalLabelPosition: HorizontalLabelPosition = Outside

    /**
     * Defines a vertical label position relative to the tick line.
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
                    bottom = centerY + guidelineThickness.half,
                )
            }?.drawHorizontal(
                context = context,
                left = chartBounds.left,
                right = chartBounds.right,
                centerY = centerY
            )
        }
        axisLine?.drawVertical(
            context = context,
            top = bounds.top,
            bottom = bounds.bottom + axisThickness,
            centerX =
            if (isLeft) bounds.right else bounds.left
        )
    }

    override fun drawAboveChart(context: ChartDrawContext): Unit = with(context) {
        val label = label
        val labelCount = getDrawLabelCount(bounds.height().toInt())

        val labels = getLabels(chartModel, labelCount)

        val tickLeftX = getTickLeftX()

        val tickRightX = tickLeftX + axisThickness.half + tickLength

        val labelX = if (isLabelOutsideOnLeftOrInsideOnRight) tickLeftX else tickRightX

        var tickCenterY: Float

        (0..labelCount).forEach { index ->
            tickCenterY =
                bounds.bottom - bounds.height() / labelCount * index + tickThickness.half

            tick?.drawHorizontal(
                context = context,
                left = tickLeftX,
                right = tickRightX,
                tickCenterY
            )

            label ?: return@forEach
            val labelText = labels.getOrNull(index) ?: return@forEach
            val textBounds = label.getTextBounds(context, labelText).translate(labelX, tickCenterY)
            if (
                horizontalLabelPosition == Outside ||
                isNotInRestrictedBounds(
                    left = textBounds.left,
                    top = textBounds.top,
                    right = textBounds.right,
                    bottom = textBounds.bottom
                )
            ) {
                label.drawText(
                    context = context,
                    text = labelText,
                    textX = labelX,
                    textY = tickCenterY,
                    horizontalPosition = textHorizontalPosition,
                    verticalPosition = verticalLabelPosition.textPosition,
                    maxTextWidth = when (sizeConstraint) {
                        is SizeConstraint.Auto ->
                            // Let the `TextComponent` use as much width as it needs, based on measuring phase.
                            Int.MAX_VALUE
                        else ->
                            (bounds.width() - tickLength - axisThickness.half).toInt()
                    }
                )
            }
        }
    }

    private fun MeasureContext.getTickLeftX(): Float = when {
        position.isLeft(isLtr) && horizontalLabelPosition == Outside ->
            bounds.right - (axisThickness.half + tickLength)
        position.isLeft(isLtr) && horizontalLabelPosition == Inside ->
            bounds.right
        position.isRight(isLtr) && horizontalLabelPosition == Outside ->
            bounds.left
        position.isRight(isLtr) && horizontalLabelPosition == Inside ->
            bounds.left - (axisThickness.half + tickLength)
        else -> throw IllegalStateException("Unexpected case while determining tick left X position.")
    }

    private fun MeasureContext.getDrawLabelCount(availableHeight: Int): Int {
        label?.let { label ->

            fun getLabelHeight(value: Float): Float =
                label.getHeight(this, valueFormatter.formatValue(value, chartModel))

            val avgHeight = arrayOf(
                getLabelHeight(chartModel.minY),
                getLabelHeight((chartModel.maxY + chartModel.minY) / 2),
                getLabelHeight(chartModel.maxY),
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
        chartModel: ChartModel,
        maxLabelCount: Int = this@VerticalAxis.maxLabelCount,
    ): List<String> {
        return if (hasExtra(LABELS_KEY)) {
            getExtra(LABELS_KEY)
        } else {
            labels.clear()
            val step = (chartModel.maxY - chartModel.minY) / maxLabelCount
            for (index in 0..maxLabelCount) {
                val value = chartModel.minY + step * index
                labels += valueFormatter.formatValue(value, chartModel)
            }
            putExtra(LABELS_KEY, labels)
            labels
        }
    }

    override fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit = with(context) {
        val labels = getLabels(chartModel = chartModel, maxLabelCount = getDrawLabelCount(availableHeight.toInt()))

        val desiredWidth = getDesiredWidth(context, labels)

        outInsets.set(
            start = if (position.isStart) desiredWidth else 0f,
            end = if (position.isEnd) desiredWidth else 0f,
        )
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
    ): Unit = with(context) {
        val labelHeight = label?.getHeight(context = context).orZero
        val lineThickness = maxOf(axisThickness, tickThickness)
        when (verticalLabelPosition) {
            Center -> outInsets.set(
                top = labelHeight.half - lineThickness,
                bottom = labelHeight.half
            )
            VerticalLabelPosition.Top -> outInsets.set(
                top = labelHeight - lineThickness,
                bottom = lineThickness
            )
            VerticalLabelPosition.Bottom -> outInsets.set(
                top = lineThickness.half,
                bottom = labelHeight
            )
        }
    }

    /**
     * Calculates a width of this [VerticalAxis] according to constraints set in [sizeConstraint].
     */
    private fun getDesiredWidth(
        context: MeasureContext,
        labels: List<String>,
    ): Float = with(context) {
        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto ->
                (getMaxLabelWidth(labels) + axisThickness.half + tickLength)
                    .coerceAtLeast(constraint.minSizeDp.pixels)
                    .coerceAtMost(constraint.maxSizeDp.pixels)
            is SizeConstraint.Exact ->
                constraint.sizeDp.pixels
            is SizeConstraint.Fraction ->
                canvasBounds.width() * constraint.fraction
            is SizeConstraint.TextWidth ->
                label?.getWidth(context = this, text = constraint.text).orZero + tickLength + axisThickness.half
        }
    }

    private fun MeasureContext.getMaxLabelWidth(labels: List<String>): Float = when (horizontalLabelPosition) {
        Outside -> label?.let { label -> labels.maxOf { label.getWidth(this, it) } }.orZero
        Inside -> 0f
    }

    /**
     * Defines a horizontal label position relative to the axis line.
     */
    public enum class HorizontalLabelPosition {
        Outside, Inside
    }

    /**
     * Defines a vertical label position relative to the tick line.
     *
     * @param textPosition a vertical text position definition used internally by
     * [com.patrykandpatryk.vico.core.component.text.TextComponent].
     *
     * @see VerticalPosition
     */
    public enum class VerticalLabelPosition(public val textPosition: VerticalPosition) {
        Center(VerticalPosition.Center),
        Top(VerticalPosition.Bottom),
        Bottom(VerticalPosition.Top),
    }

    /**
     * A subclass of base [Axis.Builder] used to build instances of [VerticalAxis].
     */
    public class Builder<Position : AxisPosition.Vertical>(
        builder: Axis.Builder<Position>? = null,
    ) : Axis.Builder<Position>(builder) {
        /**
         * The maximum label count.
         */
        public var maxLabelCount: Int = DEF_LABEL_COUNT

        /**
         * The label spacing in dp.
         */
        public var labelSpacing: Float = DEF_LABEL_SPACING

        /**
         * Defines a horizontal label position relative to the axis line.
         */
        public var horizontalLabelPosition: HorizontalLabelPosition = Outside

        /**
         * Defines a vertical label position relative to the tick line.
         */
        public var verticalLabelPosition: VerticalLabelPosition = Center

        /**
         * Creates an instance of [VerticalAxis] using the properties set in this [Builder].
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
 * A convenience function that creates an instance of [VerticalAxis].
 *
 * @param block a lambda function yielding [VerticalAxis.Builder] as its receiver.
 */
public inline fun <reified Position : AxisPosition.Vertical> createVerticalAxis(
    block: VerticalAxis.Builder<Position>.() -> Unit = {},
): VerticalAxis<Position> = VerticalAxis.Builder<Position>().apply(block).build()
