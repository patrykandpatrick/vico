/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.core.axis.vertical

import pl.patrykgoworowski.vico.core.DEF_LABEL_COUNT
import pl.patrykgoworowski.vico.core.DEF_LABEL_SPACING
import pl.patrykgoworowski.vico.core.axis.Axis
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.model.ChartModel
import pl.patrykgoworowski.vico.core.axis.setTo
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Inside
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Outside
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis.VerticalLabelPosition.Center
import pl.patrykgoworowski.vico.core.component.text.HorizontalPosition
import pl.patrykgoworowski.vico.core.component.text.VerticalPosition
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.chart.insets.Insets
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.throwable.UnknownAxisPositionException

public class VerticalAxis<Position : AxisPosition.Vertical>(
    override val position: Position,
) : Axis<Position>() {

    private val isLeft = position.isLeft(isLtr)

    private val isLabelOutsideOnLeftOrInsideOnRight: Boolean
        get() = (horizontalLabelPosition == Outside && isLeft) ||
            (horizontalLabelPosition == Inside && !isLeft)

    private val textHorizontalPosition: HorizontalPosition
        get() = if (isLabelOutsideOnLeftOrInsideOnRight) HorizontalPosition.End else HorizontalPosition.Start

    public var maxLabelCount: Int = DEF_LABEL_COUNT
    public var labelSpacing: Float = DEF_LABEL_SPACING

    public var horizontalLabelPosition: HorizontalLabelPosition = Outside
    public var verticalLabelPosition: VerticalLabelPosition = Center

    override fun drawBehindChart(
        context: ChartDrawContext,
    ): Unit = with(context) {
        val drawLabelCount = getDrawLabelCount(bounds.height().toInt())

        val axisStep = bounds.height() / drawLabelCount

        var centerY: Float

        for (index in 0..drawLabelCount) {
            centerY = bounds.bottom - (axisStep * index) + guidelineThickness.half

            guideline?.takeIf {
                it.fitsInHorizontal(
                    context = context,
                    left = chartBounds.left,
                    right = chartBounds.right,
                    centerY = centerY,
                    boundingBox = chartBounds
                )
            }?.drawHorizontal(
                context = context,
                left = chartBounds.left,
                right = chartBounds.right,
                centerY = centerY
            )
        }
        axis?.drawVertical(
            context = context,
            top = bounds.top,
            bottom = bounds.bottom + axisThickness,
            centerX =
            if (isLeft) bounds.right - axisThickness.half
            else bounds.left + axisThickness.half
        )
        Unit
    }

    override fun drawAboveChart(context: ChartDrawContext): Unit = with(context) {
        val label = label
        val labelCount = getDrawLabelCount(bounds.height().toInt())

        val labels = getLabels(context.chartModel, labelCount)
        val labelHeight = label?.getHeight(includeMargin = false, context = this).orZero
        val labelTextHeight = label?.getHeight(
            includePadding = false,
            includeMargin = false,
            context = this,
        ).orZero

        val tickLeftX =
            if (isLabelOutsideOnLeftOrInsideOnRight) bounds.right - (axisThickness + tickLength)
            else bounds.left

        val tickRightX =
            if (isLabelOutsideOnLeftOrInsideOnRight) bounds.right
            else bounds.left + axisThickness + tickLength

        val labelX = if (isLabelOutsideOnLeftOrInsideOnRight) tickLeftX else tickRightX

        var tickCenterY: Float
        val textPosition = verticalLabelPosition.textPosition

        (0..labelCount).forEach { index ->
            tickCenterY =
                bounds.bottom - ((bounds.height() / labelCount) * index) + tickThickness.half

            tick?.drawHorizontal(
                context = context,
                left = tickLeftX,
                right = tickRightX,
                tickCenterY
            )

            label ?: return@forEach
            val labelTop = label
                .getTextTopPosition(this, textPosition, tickCenterY, labelTextHeight)
            val labelText = labels.getOrNull(index) ?: return@forEach
            if (
                horizontalLabelPosition == Outside ||
                isNotInRestrictedBounds(
                    left = labelX,
                    top = labelTop - labelHeight.half,
                    right = labelX + 1,
                    bottom = labelTop + labelHeight.half
                )
            ) {
                label.drawText(
                    context = context,
                    text = labelText,
                    textX = labelX,
                    textY = tickCenterY,
                    horizontalPosition = textHorizontalPosition,
                    verticalPosition = verticalLabelPosition.textPosition,
                )
            }
        }
        label?.clearLayoutCache() ?: Unit
    }

    private fun MeasureContext.getDrawLabelCount(availableHeight: Int): Int {
        label?.let { label ->
            val height = label.getHeight(context = this)
            var result = 0f
            var addition: Float
            for (i in 0 until maxLabelCount) {
                addition = if (i > 0) height + labelSpacing else height
                if (result + addition > availableHeight) return i
                result += addition
            }
        }
        return maxLabelCount
    }

    private fun getLabels(
        chartModel: ChartModel,
        maxLabelCount: Int = this.maxLabelCount,
    ): List<String> {
        labels.clear()
        val step = (chartModel.maxY - chartModel.minY) / maxLabelCount
        for (index in maxLabelCount downTo 0) {
            val value = chartModel.maxY - (step * index)
            labels += valueFormatter.formatValue(value, index, chartModel)
        }
        return labels
    }

    override fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        chartModel: ChartModel,
        outInsets: Insets
    ): Unit = with(context) {
        val labels = getLabels(
            chartModel = chartModel,
            maxLabelCount = getDrawLabelCount(availableHeight.toInt()),
        )

        val desiredWidth = getDesiredWidth(context, labels)

        outInsets.set(
            start = if (position.isStart) desiredWidth else 0f,
            end = if (position.isEnd) desiredWidth else 0f,
        )
    }

    override fun getVerticalInsets(
        context: MeasureContext,
        chartModel: ChartModel,
        outInsets: Insets
    ): Unit = with(context) {
        val halfLabelHeight = label?.getHeight(context = context)?.half.orZero
        outInsets.set(
            top = halfLabelHeight - axisThickness,
            bottom = halfLabelHeight
        )
    }

    override fun getDesiredHeight(context: MeasureContext): Int = 0

    /**
     * Calculates a width of this [VerticalAxis] by calculating:
     * — Widest label width from passed [labels],
     * — [axisThickness],
     * — [tickLengthDp].
     * @return Width of this [VerticalAxis] that should be enough to fit its contents
     * in [drawBehindChart] and [drawAboveChart] functions.
     */
    override fun getDesiredWidth(
        context: MeasureContext,
        labels: List<String>,
    ): Float = with(context) {
        val maxLabelAndTickWidth = when (horizontalLabelPosition) {
            Outside -> label?.let { label ->
                labels.maxOf { label.getWidth(this, it) }
            }.orZero + tickLength
            Inside -> 0f
        }
        return axisThickness.half + maxLabelAndTickWidth
    }

    public enum class HorizontalLabelPosition {
        Outside, Inside
    }

    public enum class VerticalLabelPosition(public val textPosition: VerticalPosition) {
        Center(VerticalPosition.Center),
        Top(VerticalPosition.Bottom),
        Bottom(VerticalPosition.Top),
    }

    public class Builder(builder: Axis.Builder? = null) : Axis.Builder(builder) {
        public var maxLabelCount: Int = DEF_LABEL_COUNT
        public var labelSpacing: Float = DEF_LABEL_SPACING
        public var horizontalLabelPosition: HorizontalLabelPosition = Outside
        public var verticalLabelPosition: VerticalLabelPosition = Center

        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : AxisPosition.Vertical> build(): VerticalAxis<T> {
            val position = when (T::class.java) {
                AxisPosition.Vertical.Start::class.java -> AxisPosition.Vertical.Start
                AxisPosition.Vertical.End::class.java -> AxisPosition.Vertical.End
                else -> throw UnknownAxisPositionException(T::class.java)
            }
            return setTo(VerticalAxis(position = position)).also { axis ->
                axis.maxLabelCount = maxLabelCount
                axis.labelSpacing = labelSpacing
                axis.horizontalLabelPosition = horizontalLabelPosition
                axis.verticalLabelPosition = verticalLabelPosition
            } as VerticalAxis<T>
        }
    }
}

public inline fun <reified T : AxisPosition.Vertical> createVerticalAxis(
    block: VerticalAxis.Builder.() -> Unit = {},
): VerticalAxis<T> = VerticalAxis.Builder().apply(block).build()
