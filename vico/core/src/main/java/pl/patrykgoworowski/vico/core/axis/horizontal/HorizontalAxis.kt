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

package pl.patrykgoworowski.vico.core.axis.horizontal

import pl.patrykgoworowski.vico.core.axis.Axis
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.model.ChartModel
import pl.patrykgoworowski.vico.core.axis.setTo
import pl.patrykgoworowski.vico.core.component.text.VerticalPosition
import pl.patrykgoworowski.vico.core.chart.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.chart.insets.Insets
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.throwable.UnknownAxisPositionException
import kotlin.math.ceil

public class HorizontalAxis<Position : AxisPosition.Horizontal>(
    override val position: Position,
) : Axis<Position>() {

    private val AxisPosition.Horizontal.textVerticalPosition: VerticalPosition
        get() = if (isBottom) VerticalPosition.Top else VerticalPosition.Bottom

    public var tickType: TickType = TickType.Minor

    override fun drawBehindChart(context: ChartDrawContext): Unit = with(context) {
        val scrollX = context.horizontalScroll
        val clipRestoreCount = canvas.save()

        canvas.clipRect(
            bounds.left - if (tickType == TickType.Minor) tickThickness.half else 0f,
            minOf(bounds.top, chartBounds.top),
            bounds.right + if (tickType == TickType.Minor) tickThickness.half else 0f,
            maxOf(bounds.bottom, chartBounds.bottom)
        )

        val entryLength = getEntryLength(segmentProperties.segmentWidth)
        val tickCount = tickType.getTickCount(entryLength)
        val tickDrawStep = segmentProperties.segmentWidth
        val scrollAdjustment = (scrollX / tickDrawStep).toInt()
        var textDrawCenter =
            bounds.left + tickDrawStep.half - scrollX + (tickDrawStep * scrollAdjustment)
        var tickDrawCenter =
            tickType.getTickDrawCenter(scrollX, tickDrawStep, scrollAdjustment, textDrawCenter)

        val guidelineTop = chartBounds.top
        val guidelineBottom = chartBounds.bottom

        for (index in 0 until tickCount) {
            guideline?.run {
                takeIf {
                    it.fitsInVertical(
                        context = context,
                        top = guidelineTop,
                        bottom = guidelineBottom,
                        centerX = tickDrawCenter,
                        boundingBox = chartBounds,
                    )
                }?.drawVertical(
                    context = context,
                    top = guidelineTop,
                    bottom = guidelineBottom,
                    centerX = tickDrawCenter,
                )
            }

            tickDrawCenter += tickDrawStep
            textDrawCenter += tickDrawStep
        }

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    override fun drawAboveChart(context: ChartDrawContext): Unit = with(context) {
        val tickMarkTop = if (position.isBottom) bounds.top else bounds.bottom - tickLength
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val scrollX = horizontalScroll
        val clipRestoreCount = canvas.save()
        val step = chartModel.chartEntryModel.step

        canvas.clipRect(
            bounds.left - if (tickType == TickType.Minor) tickThickness.half else 0f,
            minOf(bounds.top, chartBounds.top),
            bounds.right + if (tickType == TickType.Minor) tickThickness.half else 0f,
            maxOf(bounds.bottom, chartBounds.bottom)
        )

        val entryLength = getEntryLength(segmentProperties.segmentWidth)
        val tickCount = tickType.getTickCount(entryLength)
        val tickDrawStep = segmentProperties.segmentWidth
        val scrollAdjustment = (scrollX / tickDrawStep).toInt()
        var textDrawCenter =
            bounds.left + tickDrawStep.half - scrollX + (tickDrawStep * scrollAdjustment)
        var tickDrawCenter =
            tickType.getTickDrawCenter(scrollX, tickDrawStep, scrollAdjustment, textDrawCenter)

        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop

        var valueIndex: Float = chartModel.minX + scrollAdjustment * step

        for (index in 0 until tickCount) {
            tick?.drawVertical(
                context = context,
                top = tickMarkTop,
                bottom = tickMarkBottom,
                centerX = tickDrawCenter
            )

            if (index < entryLength) {
                label?.drawText(
                    context = context,
                    text = valueFormatter.formatValue(valueIndex, index, context.chartModel),
                    textX = textDrawCenter,
                    textY = textY,
                    verticalPosition = position.textVerticalPosition,
                    width = tickDrawStep.toInt(),
                )

                valueIndex += step
            }
            tickDrawCenter += tickDrawStep
            textDrawCenter += tickDrawStep
        }

        axis?.drawHorizontal(
            context = context,
            left = chartBounds.left,
            right = chartBounds.right,
            centerY = (if (position.isBottom) bounds.top else bounds.bottom) + axisThickness.half
        )

        label?.clearLayoutCache()

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    private fun getEntryLength(segmentWidth: Float) =
        ceil(bounds.width() / segmentWidth).toInt() + 1

    private fun TickType.getTickCount(entryLength: Int) = when (this) {
        TickType.Minor -> entryLength + 1
        TickType.Major -> entryLength
    }

    private fun TickType.getTickDrawCenter(
        scrollX: Float,
        tickDrawStep: Float,
        scrollAdjustment: Int,
        textDrawCenter: Float,
    ) = when (this) {
        TickType.Minor -> bounds.left - scrollX + (tickDrawStep * scrollAdjustment)
        TickType.Major -> textDrawCenter
    }

    override fun getVerticalInsets(
        context: MeasureContext,
        chartModel: ChartModel,
        outInsets: Insets
    ): Unit = with(context) {
        with(outInsets) {
            setHorizontal(
                if (tickType == TickType.Minor) tickThickness.half
                else 0f
            )
            top = if (position.isTop) getDesiredHeight(context).toFloat() else 0f
            bottom = if (position.isBottom) getDesiredHeight(context).toFloat() else 0f
        }
    }

    override fun getDesiredHeight(context: MeasureContext): Int = with(context) {
        (if (position.isBottom) axisThickness else 0f) + tickLength + label?.getHeight(context = this).orZero
    }.toInt()

    override fun getDesiredWidth(context: MeasureContext, labels: List<String>): Float = 0f

    public enum class TickType {
        Minor, Major
    }

    public class Builder(builder: Axis.Builder? = null) : Axis.Builder(builder) {

        public var tickType: TickType = TickType.Minor

        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : AxisPosition.Horizontal> build(): HorizontalAxis<T> {
            val position = when (T::class.java) {
                AxisPosition.Horizontal.Top::class.java -> AxisPosition.Horizontal.Top
                AxisPosition.Horizontal.Bottom::class.java -> AxisPosition.Horizontal.Bottom
                else -> throw UnknownAxisPositionException(T::class.java)
            }
            return setTo(HorizontalAxis(position = position)).also { axis ->
                axis.tickType = tickType
            } as HorizontalAxis<T>
        }
    }
}

public inline fun <reified T : AxisPosition.Horizontal> createHorizontalAxis(
    block: HorizontalAxis.Builder.() -> Unit = {},
): HorizontalAxis<T> = HorizontalAxis.Builder().apply(block).build()
