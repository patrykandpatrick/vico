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
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.chart.insets.HorizontalInsets
import pl.patrykgoworowski.vico.core.chart.insets.Insets
import pl.patrykgoworowski.vico.core.component.text.HorizontalPosition
import pl.patrykgoworowski.vico.core.component.text.VerticalPosition
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.extension.translate
import pl.patrykgoworowski.vico.core.throwable.UnknownAxisPositionException

private const val LABELS_KEY = "labels"

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

        val tickLeftX =
            if (isLabelOutsideOnLeftOrInsideOnRight) bounds.right - (axisThickness.half + tickLength)
            else bounds.left

        val tickRightX =
            if (isLabelOutsideOnLeftOrInsideOnRight) bounds.right
            else bounds.left + axisThickness.half + tickLength

        val labelX = if (isLabelOutsideOnLeftOrInsideOnRight) tickLeftX else tickRightX

        var tickCenterY: Float

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

    private fun MeasureContext.getDrawLabelCount(availableHeight: Int): Int {
        label?.let { label ->

            fun getLabelHeight(value: Float, valueIndex: Int): Float =
                label.getHeight(this, valueFormatter.formatValue(value, valueIndex, chartModel))

            val avgHeight = arrayOf(
                getLabelHeight(chartModel.minY, 0),
                getLabelHeight((chartModel.maxY + chartModel.minY) / 2, chartModel.lengthY.half.toInt()),
                getLabelHeight(chartModel.maxY, chartModel.lengthY.toInt()),
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
            for (index in maxLabelCount downTo 0) {
                val value = chartModel.maxY - step * index
                labels += valueFormatter.formatValue(value, index, chartModel)
            }
            putExtra(LABELS_KEY, labels)
            labels
        }
    }

    override fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets
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
        outInsets: Insets
    ): Unit = with(context) {
        val halfLabelHeight = label?.getHeight(context = context)?.half.orZero
        outInsets.set(
            top = halfLabelHeight - axisThickness,
            bottom = halfLabelHeight
        )
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
                context.width * constraint.fraction
            is SizeConstraint.TextWidth ->
                label?.getWidth(context = this, text = constraint.text).orZero + tickLength + axisThickness.half
        }
    }

    private fun MeasureContext.getMaxLabelWidth(labels: List<String>): Float = when (horizontalLabelPosition) {
        Outside -> label?.let { label -> labels.maxOf { label.getWidth(this, it) } }.orZero
        Inside -> 0f
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
