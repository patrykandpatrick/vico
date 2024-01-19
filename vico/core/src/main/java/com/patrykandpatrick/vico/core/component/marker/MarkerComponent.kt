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
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent.LabelPosition.Top.getY
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
 * @param labelPosition the [LabelPosition] to set the position of the marker label
 * @param indicator an optional indicator drawn at a given point belonging to the data entry.
 * @param guideline an optional line drawn from the bottom of the chart to the bottom edge of the [label].
 */
public open class MarkerComponent(
    public val label: TextComponent,
    private val labelPosition: LabelPosition = LabelPosition.Top,
    public val indicator: Component?,
    public val guideline: LineComponent?,
) : Marker {
    private val tempBounds = RectF()

    public val TextComponent.tickSizeDp: Float
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

    /**
     * This sealed class represents the position where the label should be rendered
     */
    public sealed interface LabelPosition {
        public fun getY(
            labelTickSizeInPixels: Float,
            chartBounds: RectF,
            labelBounds: RectF,
            markerModel: Marker.EntryModel,
            indicatorSize: Float,
        ): Float

        /**
         * This is the default position.
         *
         * The label will be rendered on the top of the chart
         */
        public data object Top : LabelPosition {
            override fun getY(
                labelTickSizeInPixels: Float,
                chartBounds: RectF,
                labelBounds: RectF,
                markerModel: Marker.EntryModel,
                indicatorSize: Float,
            ): Float {
                return chartBounds.top - labelBounds.height() - labelTickSizeInPixels
            }
        }

        /**
         * The label will be rendered on the top of the indicator.
         *
         * For the case of the chart holds dynamic values, the label will update its position  one the indicator updates too.
         *
         * @param spacingDp it's an additional space between the indicator and the label. That makes the appearance
         * a bit more customizable for the case of custom indicators or custom label layouts.
         */
        public data class AboveIndicator(val spacingDp: Float = 2f) : LabelPosition {
            override fun getY(
                labelTickSizeInPixels: Float,
                chartBounds: RectF,
                labelBounds: RectF,
                markerModel: Marker.EntryModel,
                indicatorSize: Float,
            ): Float {
                return markerModel.location.y -
                    labelBounds.height() -
                    labelTickSizeInPixels -
                    indicatorSize -
                    spacingDp
            }
        }

        public companion object
    }

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
            this[MarkerCorneredShape.TICK_X_KEY] = entryX

            label.drawText(
                context = context,
                text = text,
                textX = x,
                textY =
                    labelPosition.getY(
                        labelTickSizeInPixels = label.tickSizeDp.pixels,
                        chartBounds = bounds,
                        labelBounds = labelBounds,
                        markerModel = markedEntries.last(),
                        indicatorSize = indicatorSizeDp,
                    ),
                verticalPosition = VerticalPosition.Bottom,
                maxTextWidth = minOf(bounds.right - x, x - bounds.left).doubled.ceil.toInt(),
            )
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
