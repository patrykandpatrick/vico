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

package com.patrykandpatrick.vico.core.axis.horizontal

import android.graphics.RectF
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.setTo
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.throwable.UnknownAxisPositionException
import kotlin.math.abs
import kotlin.math.ceil

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
     * Defines the tick placement.
     */
    @Deprecated(message = "The tick type is now defined by `tickPosition`.", replaceWith = ReplaceWith("tickPosition"))
    @Suppress("DEPRECATION")
    public var tickType: TickType? = null
        set(value) {
            field = value
            if (value != null) tickPosition = TickPosition.fromTickType(value)
        }

    /**
     * Defines the tick placement.
     */
    public var tickPosition: TickPosition = TickPosition.Edge

    override fun drawBehindChart(context: ChartDrawContext): Unit = with(context) {
        val clipRestoreCount = canvas.save()
        val tickMarkTop = if (position.isBottom) bounds.top else bounds.bottom - tickLength
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val chartValues = chartValuesManager.getChartValues()
        val step = chartValues.stepX

        canvas.clipRect(
            bounds.left - tickPosition.getTickInset(tickThickness),
            minOf(bounds.top, chartBounds.top),
            bounds.right + tickPosition.getTickInset(tickThickness),
            maxOf(bounds.bottom, chartBounds.bottom),
        )

        val tickDrawStep = segmentProperties.segmentWidth
        val scrollAdjustment = (abs(x = horizontalScroll) / tickDrawStep).toInt()
        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop

        val labelPositionOffset = when (segmentProperties.labelPositionOrDefault) {
            LabelPosition.Start -> 0f
            LabelPosition.Center -> tickDrawStep.half
        }

        var textCenter = bounds.getStart(isLtr = isLtr) + layoutDirectionMultiplier *
            (labelPositionOffset + tickDrawStep * scrollAdjustment) - horizontalScroll

        var tickCenter = getTickDrawCenter(tickPosition, horizontalScroll, tickDrawStep, scrollAdjustment, textCenter)

        forEachEntity(
            startSegmentX = chartValues.minX + scrollAdjustment * step,
            step = step,
            xRange = chartValues.minX..chartValues.maxX,
        ) { segmentX, shouldDrawLines, shouldDrawLabel ->

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
                    text = valueFormatter.formatValue(segmentX, chartValues),
                    textX = textCenter,
                    textY = textY,
                    verticalPosition = position.textVerticalPosition,
                    maxTextWidth = getMaxTextWidth(
                        tickDrawStep = tickDrawStep.toInt(),
                        spacing = tickPosition.spacing,
                        textX = textCenter,
                        bounds = chartBounds,
                    ),
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

    override fun drawAboveChart(context: ChartDrawContext): Unit = Unit

    private fun getEntryLength(segmentWidth: Float) =
        ceil(bounds.width() / segmentWidth).toInt() + 1

    private inline fun ChartDrawContext.forEachEntity(
        startSegmentX: Float,
        step: Float,
        xRange: ClosedFloatingPointRange<Float>,
        action: (valueIndex: Float, shouldDrawLines: Boolean, shouldDrawLabel: Boolean) -> Unit,
    ) {
        var segmentX = startSegmentX
        val entryLength = getEntryLength(segmentProperties.segmentWidth)

        for (index in 0 until tickPosition.getTickCount(entryLength = entryLength)) {

            val firstEntityConditionsMet = segmentX != xRange.start ||
                !segmentProperties.labelPositionOrDefault.skipFirstEntity ||
                tickPosition.offset > 0

            val shouldDrawLines = segmentX / step >= tickPosition.offset &&
                (segmentX / step - tickPosition.offset) % tickPosition.spacing == 0f &&
                firstEntityConditionsMet

            action(
                segmentX,
                shouldDrawLines,
                shouldDrawLines && segmentX in xRange && index < entryLength,
            )

            segmentX += step
        }
    }

    private fun DrawContext.getTickDrawCenter(
        tickPosition: TickPosition,
        scrollX: Float,
        tickDrawStep: Float,
        scrollAdjustment: Int,
        textDrawCenter: Float,
    ) = when (tickPosition) {
        is TickPosition.Center -> textDrawCenter
        is TickPosition.Edge -> bounds.getStart(isLtr = isLtr) + tickDrawStep * tickPosition.offset +
            layoutDirectionMultiplier * tickDrawStep * scrollAdjustment - scrollX
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(context) {
        with(outInsets) {
            setHorizontal(tickPosition.getTickInset(tickThickness))
            top = if (position.isTop) getDesiredHeight(context, segmentProperties) else 0f
            bottom = if (position.isBottom) getDesiredHeight(context, segmentProperties) else 0f
        }
    }

    private fun getDesiredHeight(
        context: MeasureContext,
        segmentProperties: SegmentProperties,
    ): Float = with(context) {

        val labelWidth =
            if (isHorizontalScrollEnabled) {
                segmentProperties.scaled(scale = chartScale).segmentWidth.toInt() * tickPosition.spacing
            } else Int.MAX_VALUE

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
    @Deprecated(
        message = "`TickType` has been replaced with `TickPosition`, which uses better naming and has more features.",
        replaceWith = ReplaceWith(
            expression = "TickPosition",
            imports = arrayOf(
                "com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis.TickPosition",
            ),
        ),
    )
    public enum class TickType {
        /**
         * A tick will be drawn at either edge of each chart segment.
         *
         * ```
         * —————————————
         * |   |   |   |
         *   1   2   3
         * ```
         */
        @Deprecated(
            message = """`TickType` has been replaced with `TickPosition`, which uses better naming and has more
                features.""",
            replaceWith = ReplaceWith(
                expression = "TickPosition.Edge",
                imports = arrayOf(
                    "com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis.TickPosition",
                ),
            ),
        )
        Minor,

        /**
         * A tick will be drawn at the center of each chart segment.
         *
         * ```
         * —————————————
         *   |   |   |
         *   1   2   3
         * ```
         */
        @Deprecated(
            message = """`TickType` has been replaced with `TickPosition`, which uses better naming and has more
                features.""",
            replaceWith = ReplaceWith(
                expression = "TickPosition.Center()",
                imports = arrayOf("com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis.TickPosition"),
            ),
        )
        Major,
    }

    /**
     * Defines the position of a horizontal axis’s labels.
     */
    public enum class LabelPosition(internal val skipFirstEntity: Boolean) {
        Start(skipFirstEntity = true),
        Center(skipFirstEntity = false),
    }

    /**
     * Defines the position of a horizontal axis’s ticks. [HorizontalAxis.TickPosition.Center] allows for offset and
     * spacing customization.
     *
     * @param offset the index at which ticks and labels start to be drawn. The default is 0.
     * @param spacing defines how often ticks should be drawn, where 1 means a tick is drawn for each entry,
     * 2 means a tick is drawn for every second entry, and so on.
     */
    public sealed class TickPosition(
        public val offset: Int,
        public val spacing: Int,
    ) {

        /**
         * Returns the tick count required by this [TickPosition].
         */
        public abstract fun getTickCount(entryLength: Int): Int

        /**
         * Returns the chart inset required by this [TickPosition].
         */
        public abstract fun getTickInset(tickThickness: Float): Float

        /**
         * A tick will be drawn at either edge of each chart segment.
         *
         * ```
         * —————————————————
         * |   |   |   |   |
         *   1   2   3   4
         * ```
         */
        public object Edge : TickPosition(offset = 0, spacing = 1) {

            override fun getTickCount(entryLength: Int): Int = entryLength + 1

            override fun getTickInset(tickThickness: Float): Float = tickThickness.half
        }

        /**
         * A tick will be drawn at the center of each chart segment.
         *
         * ```
         * ————————————————
         *   |   |   |   |
         *   1   2   3   4
         * ```
         *
         * [offset] is the index at which ticks and labels start to be drawn. Setting [offset] to 2 gives this result:
         *
         * ```
         * ————————————————
         *           |   |
         *           3   4
         * ```
         *
         * [spacing] defines how often ticks should be drawn. Setting [spacing] to 2 gives this result:
         *
         * ```
         * ————————————————
         *   |       |
         *   1       3
         * ```
         */
        public class Center(
            offset: Int = 0,
            spacing: Int = 1,
        ) : TickPosition(offset = offset, spacing = spacing) {

            public constructor(spacing: Int) : this(offset = spacing, spacing = spacing)

            init {
                require(offset >= 0) { "`offset` cannot be negative. Received $offset." }
                require(spacing >= 1) { "`spacing` cannot be less than 1. Received $spacing." }
            }

            override fun getTickCount(entryLength: Int): Int = entryLength

            override fun getTickInset(tickThickness: Float): Float = 0f
        }

        public companion object {

            /**
             * Returns the [TickPosition] corresponding to the given [TickType].
             */
            @Suppress("DEPRECATION")
            public fun fromTickType(type: TickType): TickPosition = when (type) {
                TickType.Minor -> Edge
                TickType.Major -> Center()
            }
        }
    }

    /**
     * A subclass of [Axis.Builder] used to build [HorizontalAxis] instances.
     */
    public class Builder<Position : AxisPosition.Horizontal>(
        builder: Axis.Builder<Position>? = null,
    ) : Axis.Builder<Position>(builder) {

        /**
         * Defines the tick placement.
         */
        @Deprecated(
            message = "The tick type is now defined by `tickPosition`.",
            replaceWith = ReplaceWith("tickPosition"),
        )
        @Suppress("DEPRECATION")
        public var tickType: TickType? = null

        /**
         * Defines the tick placement.
         */
        public var tickPosition: TickPosition = TickPosition.Edge

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
                @Suppress("DEPRECATION")
                tickType?.also { axis.tickType = it }
                axis.tickPosition = tickPosition
            } as HorizontalAxis<T>
        }
    }

    internal companion object {
        const val MAX_HEIGHT_DIVISOR = 3f

        private fun MeasureContext.getMaxTextWidth(
            tickDrawStep: Int,
            spacing: Int,
            textX: Float,
            bounds: RectF,
        ): Int {
            val baseWidth = tickDrawStep * spacing
            val left = textX - baseWidth.half
            val right = textX + baseWidth.half

            return when {
                isHorizontalScrollEnabled -> baseWidth
                bounds.left > left -> baseWidth - (bounds.left - left).doubled
                bounds.right < right -> baseWidth - (right - bounds.right).doubled
                else -> baseWidth
            }.toInt()
        }
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
