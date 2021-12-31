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

package pl.patrykgoworowski.vico.core.component.marker

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.model.ChartModel
import pl.patrykgoworowski.vico.core.chart.insets.Insets
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.cornered.MarkerCorneredShape
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.context.DrawContext
import pl.patrykgoworowski.vico.core.context.MeasureContext
import pl.patrykgoworowski.vico.core.extension.averageOf
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.marker.DefaultMarkerLabelFormatter
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.marker.MarkerLabelFormatter

public open class MarkerComponent(
    private val label: TextComponent,
    private val indicator: Component,
    private val guideline: LineComponent,
) : Marker {

    private val MeasureContext.markerHeight: Float
        get() = label.getHeight(this) // + shape.tickSize.pixels

    public var indicatorSize: Float = 0f
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter

    override fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ): Unit = with(context) {
        drawGuideline(context, bounds, markedEntries)
        val halfIndicatorSize = indicatorSize.half.pixels

        markedEntries.forEachIndexed { _, model ->
            onApplyEntryColor?.invoke(model.color)
            indicator.draw(
                context,
                model.location.x - halfIndicatorSize,
                model.location.y - halfIndicatorSize,
                model.location.x + halfIndicatorSize,
                model.location.y + halfIndicatorSize,
            )
        }
        drawLabel(context, bounds, markedEntries)
    }

    private fun drawLabel(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ): Unit = with(context) {
        val text = labelFormatter.getLabel(markedEntries)
        val entryX = markedEntries.averageOf { it.location.x }
        val x = overrideXPositionToFit(context, entryX, bounds, text)
        putExtra(MarkerCorneredShape.tickXKey, entryX)

        label.drawText(
            context = context,
            text = text,
            textX = x,
            textY = bounds.top +
                label.allLinesHeight.half +
                label.padding.topDp.pixels -
                context.markerHeight,
        )
    }

    private fun overrideXPositionToFit(
        context: MeasureContext,
        xPosition: Float,
        bounds: RectF,
        text: CharSequence,
    ): Float {
        val halfOfTextWidth = label.getWidth(context, text).half
        return when {
            xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
            xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
            else -> xPosition
        }
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
                guideline.drawVertical(
                    context,
                    bounds.top,
                    bounds.bottom,
                    x,
                )
            }
    }

    override fun getVerticalInsets(
        context: MeasureContext,
        chartModel: ChartModel,
        outInsets: Insets
    ): Unit = with(context) {
        outInsets.top = markerHeight
    }
}
