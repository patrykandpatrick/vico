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

package com.patrykandpatrick.vico.core.common.component

import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.insets.Insets
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerLabelFormatter
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarkerLabelFormatter
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.common.extension.averageOf
import com.patrykandpatrick.vico.core.common.extension.ceil
import com.patrykandpatrick.vico.core.common.extension.doubled
import com.patrykandpatrick.vico.core.common.extension.half
import com.patrykandpatrick.vico.core.common.extension.orZero
import com.patrykandpatrick.vico.core.common.position.VerticalPosition

/**
 * The default [CartesianMarker] implementation.
 *
 * @param label the [TextComponent] for the label.
 * @param labelPosition specifies the position of the label.
 * @param indicator drawn at the marked points.
 * @param guideline drawn vertically through the marked points.
 */
public open class CartesianMarkerComponent(
    public val label: TextComponent,
    public val labelPosition: LabelPosition = LabelPosition.Top,
    public val indicator: Component? = null,
    public val guideline: LineComponent? = null,
) : CartesianMarker {
    protected val tempBounds: RectF = RectF()

    protected val TextComponent.tickSizeDp: Float
        get() = ((background as? ShapeComponent)?.shape as? MarkerCorneredShape)?.tickSizeDp.orZero

    /**
     * The indicator size (in dp).
     */
    public var indicatorSizeDp: Float = 0f

    /**
     * An optional lambda function that allows for applying the color associated with a given data entry to a
     * [Component].
     */
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null

    /**
     * The [CartesianMarkerLabelFormatter] for this marker.
     */
    public var labelFormatter: CartesianMarkerLabelFormatter = DefaultCartesianMarkerLabelFormatter()

    /**
     * Creates a [CartesianMarkerComponent] with [LabelPosition.Top].
     *
     * @param label the [TextComponent] for the label.
     * @param indicator drawn at the marked points.
     * @param guideline drawn vertically through the marked points.
     */
    @Deprecated(
        "Use the primary constructor, which has `labelPosition` and `minimumWidth` parameters and default " +
            "values for `indicator` and `guideline`. (If you’re using named arguments, ignore this warning. " +
            "The deprecated constructor is more specific, but the primary one matches and will be used once " +
            "the deprecated one has been removed.)",
    )
    public constructor(label: TextComponent, indicator: Component?, guideline: LineComponent?) :
        this(label, LabelPosition.Top, indicator, guideline)

    override fun draw(
        context: CartesianDrawContext,
        bounds: RectF,
        markedEntries: List<CartesianMarker.EntryModel>,
        chartValues: ChartValues,
    ): Unit =
        with(context) {
            drawGuideline(context, bounds, markedEntries)
            val halfIndicatorSize = indicatorSizeDp.half.pixels

            markedEntries.forEachIndexed { _, model ->
                onApplyEntryColor?.invoke(model.color)
                indicator?.draw(
                    context,
                    model.location.x - halfIndicatorSize,
                    model.location.y - halfIndicatorSize,
                    model.location.x + halfIndicatorSize,
                    model.location.y + halfIndicatorSize,
                )
            }
            drawLabel(context, bounds, markedEntries, chartValues)
        }

    protected fun drawLabel(
        context: CartesianDrawContext,
        bounds: RectF,
        markedEntries: List<CartesianMarker.EntryModel>,
        chartValues: ChartValues,
    ): Unit =
        with(context) {
            val text = labelFormatter.getLabel(markedEntries, chartValues)
            val entryX = markedEntries.averageOf { it.location.x }
            val labelBounds =
                label.getTextBounds(
                    context = context,
                    text = text,
                    width = bounds.width().toInt(),
                    outRect = tempBounds,
                )
            val halfOfTextWidth = labelBounds.width().half
            val x = overrideXPositionToFit(entryX, bounds, halfOfTextWidth)
            extraStore[MarkerCorneredShape.tickXKey] = entryX
            val tickPosition: MarkerCorneredShape.TickPosition
            val y: Float
            val verticalPosition: VerticalPosition
            if (labelPosition == LabelPosition.Top) {
                tickPosition = MarkerCorneredShape.TickPosition.Bottom
                y = bounds.top - label.tickSizeDp.pixels
                verticalPosition = VerticalPosition.Top
            } else {
                val topEntryY = markedEntries.minOf { it.location.y }
                val flip =
                    labelPosition == LabelPosition.AroundPoint &&
                        topEntryY - labelBounds.height() - label.tickSizeDp.pixels < bounds.top
                tickPosition =
                    if (flip) MarkerCorneredShape.TickPosition.Top else MarkerCorneredShape.TickPosition.Bottom
                y = topEntryY + (if (flip) 1 else -1) * label.tickSizeDp.pixels
                verticalPosition = if (flip) VerticalPosition.Bottom else VerticalPosition.Top
            }
            extraStore[MarkerCorneredShape.tickPositionKey] = tickPosition

            label.drawText(
                context = context,
                text = text,
                textX = x,
                textY = y,
                verticalPosition = verticalPosition,
                maxTextWidth = minOf(bounds.right - x, x - bounds.left).doubled.ceil.toInt(),
            )
        }

    protected fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        halfOfTextWidth: Float,
    ): Float =
        when {
            xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
            xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
            else -> xPosition
        }

    protected fun drawGuideline(
        context: CartesianDrawContext,
        bounds: RectF,
        markedEntries: List<CartesianMarker.EntryModel>,
    ) {
        markedEntries
            .map { it.location.x }
            .toSet()
            .forEach { x ->
                guideline?.drawVertical(
                    context,
                    bounds.top,
                    bounds.bottom,
                    x,
                )
            }
    }

    override fun getInsets(
        context: CartesianMeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ) {
        if (labelPosition == LabelPosition.AroundPoint) return
        with(context) { outInsets.top = label.getHeight(context) + label.tickSizeDp.pixels }
    }

    /**
     * Specifies the position of a [CartesianMarkerComponent]’s label.
     */
    public enum class LabelPosition {
        /**
         * Positions the label at the top of the [CartesianChart]. Sufficient room is made.
         */
        Top,

        /**
         * Positions the label above the topmost marked point or, if there isn’t enough room, below it.
         */
        AroundPoint,

        /**
         * Positions the label above the topmost marked point. Sufficient room is made at the top of the
         * [CartesianChart].
         */
        AbovePoint,
    }
}
