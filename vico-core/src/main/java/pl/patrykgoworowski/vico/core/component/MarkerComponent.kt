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

package pl.patrykgoworowski.vico.core.component

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.model.DataSetModel
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.corner.MarkerCorneredShape
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.dataset.insets.Insets
import pl.patrykgoworowski.vico.core.draw.DrawContext
import pl.patrykgoworowski.vico.core.extension.averageOf
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.marker.DefaultMarkerLabelFormatter
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.marker.MarkerLabelFormatter

public open class MarkerComponent(
    private val label: TextComponent,
    private val indicator: Component,
    private val guideline: LineComponent,
    shape: MarkerCorneredShape,
    markerBackgroundColor: Int,
    dynamicShader: DynamicShader? = null
) : Marker, ShapeComponent<MarkerCorneredShape>(shape, markerBackgroundColor, dynamicShader) {

    private val markerTempBounds = RectF()

    private val MeasureContext.markerHeight: Float
        get() = label.getHeight(this) + shape.tickSize.pixels

    public var indicatorSize: Float = 0f
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter

    override fun draw(
        context: DrawContext,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ) = with(context) {
        setParentBounds(bounds)
        applyShader(context, bounds.left, bounds.top, bounds.right, bounds.bottom)
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
    ) = with(context) {
        val text = labelFormatter.getLabel(markedEntries)
        val entryX = markedEntries.averageOf { it.location.x }
        val x = overrideXPositionToFit(context, entryX, bounds, text)

        label.drawText(
            context = context,
            text = text,
            textX = x,
            textY = bounds.top +
                    label.allLinesHeight.half +
                    label.padding.topDp.pixels -
                    context.markerHeight,
        ) { textContext, left, top, right, bottom ->
            markerTempBounds.set(left, top, right, bottom)
            drawMarkerBackground(textContext, markerTempBounds, bounds, entryX)
        }
    }

    private fun drawMarkerBackground(
        context: DrawContext,
        bounds: RectF,
        contentBounds: RectF,
        entryX: Float,
    ) {
        path.reset()
        shape.drawMarker(context, paint, path, bounds, contentBounds, entryX)
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
        guideline.setParentBounds(bounds)
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
        dataSetModel: DataSetModel,
        outInsets: Insets
    ) = with(context) {
        outInsets.top = markerHeight
    }
}
