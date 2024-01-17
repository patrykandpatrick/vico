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

package com.patrykandpatrick.vico.core.component.marker

import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.averageOf
import com.patrykandpatrick.vico.core.extension.ceil
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.marker.DefaultMarkerLabelFormatter
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter

/**
 * The default implementation of the [Marker] interface.
 *
 * @param label the [TextComponent] used to draw the label.
 * @param labelPosition the [Marker.LabelPosition] to set the position of the marker label
 * @param indicator an optional indicator drawn at a given point belonging to the data entry.
 * @param guideline an optional line drawn from the bottom of the chart to the bottom edge of the [label].
 */
public open class MarkerComponent(
    public val label: TextComponent,
    public val indicator: Component?,
    public val guideline: LineComponent?,
    private val labelPosition: Marker.LabelPosition = Marker.LabelPosition.Top,
) : Marker {
    private val tempBounds = RectF()

    private val TextComponent.tickSizeDp: Float
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
     * The [MarkerLabelFormatter] for this marker.
     */
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter()

    override fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
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

    private fun drawLabel(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
        chartValues: ChartValues,
    ): Unit = with(context) {
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
        this[MarkerCorneredShape.TICK_X_KEY] = entryX

        label.drawText(
            context = context,
            text = text,
            textX = x,
            textY = getLabelY(bounds, labelBounds, markedEntries.last()),
            verticalPosition = VerticalPosition.Bottom,
            maxTextWidth = minOf(bounds.right - x, x - bounds.left).doubled.ceil.toInt(),
        )
    }

    private fun DrawContext.getLabelY(
        bounds: RectF,
        labelBounds: RectF,
        referenceMarkerModel: Marker.EntryModel,
    ): Float {
        return when (labelPosition) {
            Marker.LabelPosition.Top -> getLabelYTop(bounds, labelBounds)

            is Marker.LabelPosition.AboveIndicator -> getLabelYAboveIndicator(
                referenceMarkerModel,
                labelBounds,
                labelPosition.spacingDp,
            )
        }
    }

    private fun DrawContext.getLabelYTop(bounds: RectF, labelBounds: RectF): Float {
        return bounds.top - labelBounds.height() - label.tickSizeDp.pixels
    }

    private fun DrawContext.getLabelYAboveIndicator(
        referenceMarkerModel: Marker.EntryModel,
        labelBounds: RectF,
        additionalSpacing: Float,
    ): Float {
        return referenceMarkerModel.location.y - labelBounds.height() - label.tickSizeDp.pixels - indicatorSizeDp - additionalSpacing
    }

    private fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        halfOfTextWidth: Float,
    ): Float =
        when {
            xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
            xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
            else -> xPosition
        }

    private fun drawGuideline(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
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
        context: MeasureContext,
        outInsets: Insets,
        horizontalDimensions: HorizontalDimensions,
    ): Unit =
        with(context) {
            outInsets.top = label.getHeight(context) + label.tickSizeDp.pixels
        }
}
