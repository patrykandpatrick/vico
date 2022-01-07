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
import pl.patrykgoworowski.vico.core.chart.insets.Insets
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.cornered.MarkerCorneredShape
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.component.text.VerticalPosition
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

    private val tempBounds = RectF()

    public var indicatorSizeDp: Float = 0f
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter

    override fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ): Unit = with(context) {
        drawGuideline(context, bounds, markedEntries)
        val halfIndicatorSize = indicatorSizeDp.half.pixels

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
        val labelBounds = label.getTextBounds(context, text, outRect = tempBounds)
        val halfOfTextWidth = labelBounds.width().half
        val x = overrideXPositionToFit(entryX, bounds, halfOfTextWidth)
        this[MarkerCorneredShape.tickXKey] = entryX

        label.drawText(
            context = context,
            text = text,
            textX = x,
            textY = bounds.top - labelBounds.height(),
            verticalPosition = VerticalPosition.Top,
        )
    }

    private fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        halfOfTextWidth: Float,
    ): Float = when {
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
        outInsets: Insets
    ) {
        outInsets.top = label.getHeight(context)
    }
}
