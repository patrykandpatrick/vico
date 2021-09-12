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

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.axis.model.DataSetModel
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShader
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dimensions.Dimensions
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.extension.averageOf
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.extension.orZero
import pl.patrykgoworowski.vico.core.marker.DefaultMarkerLabelFormatter
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.marker.MarkerLabelFormatter
import pl.patrykgoworowski.vico.core.path.corner.MarkerCorneredShape

public open class MarkerComponent(
    private val label: TextComponent,
    private val indicator: Component,
    private val guideline: LineComponent,
    shape: MarkerCorneredShape,
    markerBackgroundColor: Int,
    dynamicShader: DynamicShader? = null
) : Marker, ShapeComponent<MarkerCorneredShape>(shape, markerBackgroundColor, dynamicShader) {

    private val markerTempBounds = RectF()

    private val markerHeight: Float
        get() = label.getHeight() + shape.tickSize.orZero

    public var indicatorSize: Float = 0f
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter

    override fun draw(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ) {
        setParentBounds(bounds)
        applyShader(bounds)
        drawGuideline(canvas, bounds, markedEntries)

        markedEntries.forEachIndexed { _, model ->
            onApplyEntryColor?.invoke(model.color)
            indicator.draw(
                canvas,
                model.location.x - indicatorSize.half,
                model.location.y - indicatorSize.half,
                model.location.x + indicatorSize.half,
                model.location.y + indicatorSize.half,
            )
        }
        drawLabel(canvas, bounds, markedEntries)
    }

    private fun drawLabel(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ) {
        val text = labelFormatter.getLabel(markedEntries)
        val entryX = markedEntries.averageOf { it.location.x }
        val x = overrideXPositionToFit(entryX, bounds, text)

        label.drawText(
            canvas = canvas,
            text = text,
            textX = x,
            textY = bounds.top + label.allLinesHeight.half + label.padding.top - markerHeight,
        ) { textCanvas, left, top, right, bottom ->
            markerTempBounds.set(left, top, right, bottom)
            drawMarkerBackground(textCanvas, markerTempBounds, bounds, entryX)
        }
    }

    private fun drawMarkerBackground(
        canvas: Canvas,
        bounds: RectF,
        contentBounds: RectF,
        entryX: Float,
    ) {
        path.reset()
        shape.drawMarker(canvas, paint, path, bounds, contentBounds, entryX)
    }

    private fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        text: CharSequence,
    ): Float {
        val halfOfTextWidth = label.getWidth(text).half
        return when {
            xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
            xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
            else -> xPosition
        }
    }

    private fun drawGuideline(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ) {
        guideline.setParentBounds(bounds)
        markedEntries
            .map { it.location.x }
            .toSet()
            .forEach { x ->
                guideline.drawVertical(
                    canvas,
                    bounds.top,
                    bounds.bottom,
                    x,
                )
            }
    }

    override fun getVerticalInsets(
        outDimensions: MutableDimensions,
        dataSetModel: DataSetModel
    ): Dimensions =
        outDimensions.apply {
            top = markerHeight
        }

    override fun getHorizontalInsets(
        outDimensions: MutableDimensions,
        availableHeight: Float,
        dataSetModel: DataSetModel
    ): Dimensions = outDimensions
}
