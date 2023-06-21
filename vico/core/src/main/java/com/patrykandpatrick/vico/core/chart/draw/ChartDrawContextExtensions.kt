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

package com.patrykandpatrick.vico.core.chart.draw

import android.graphics.Canvas
import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.dimensions.padding
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.getClosestMarkerEntryModel
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.model.Point

/**
 * The anonymous implementation of [ChartDrawContext].
 *
 * @param canvas the canvas on which the [Chart] is to be drawn.
 * @param elevationOverlayColor the color of elevation overlays, applied to [ShapeComponent]s that cast shadows.
 * @param measureContext holds data used for component measurements.
 * @param markerTouchPoint the point inside the chart’s bounds where physical touch is occurring.
 * @param horizontalDimensions holds information on the [Chart]’s horizontal dimensions.
 * @param chartBounds the bounds in which the [Chart] will be drawn.
 * @param horizontalScroll the horizontal scroll.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the dimensions are such
 * that, at a scale factor of 1, an empty space would be visible near the end edge of the chart.
 *
 * @see [ShapeComponent.setShadow]
 */
public fun chartDrawContext(
    canvas: Canvas,
    elevationOverlayColor: Int,
    measureContext: MeasureContext,
    markerTouchPoint: Point?,
    horizontalDimensions: HorizontalDimensions,
    chartBounds: RectF,
    horizontalScroll: Float,
    autoScaleUp: AutoScaleUp,
): ChartDrawContext = object : ChartDrawContext, MeasureContext by measureContext {

    override val chartBounds: RectF = chartBounds

    override var canvas: Canvas = canvas

    override val elevationOverlayColor: Long = elevationOverlayColor.toLong()

    override val markerTouchPoint: Point? = markerTouchPoint

    override val chartScale: Float = calculateDrawScale()

    override val horizontalDimensions: HorizontalDimensions = horizontalDimensions.scaled(chartScale)

    override val horizontalScroll: Float = horizontalScroll

    override fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit) {
        val originalCanvas = this.canvas
        this.canvas = canvas
        block(this)
        this.canvas = originalCanvas
    }

    private fun calculateDrawScale(): Float {
        val contentWidth = horizontalLayout.getContentWidth(
            horizontalDimensions.xSpacing,
            chartValuesManager.getChartValues().getMaxMajorEntryCount(),
        ) + horizontalDimensions.padding
        val upscalingPossibleButDisallowed = contentWidth < chartBounds.width() && autoScaleUp == AutoScaleUp.None
        val scrollEnabledAndUpscalingImpossible = isHorizontalScrollEnabled && contentWidth >= chartBounds.width()
        return if (upscalingPossibleButDisallowed || scrollEnabledAndUpscalingImpossible) {
            measureContext.chartScale
        } else {
            chartBounds.width() / contentWidth
        }
    }
}

/**
 * Draws the provided [marker] on top of the chart at the given [markerTouchPoint] and notifies the
 * [markerVisibilityChangeListener] about the [marker]’s visibility changes.
 */
public fun <Model : ChartEntryModel> ChartDrawContext.drawMarker(
    marker: Marker,
    markerTouchPoint: Point?,
    chart: Chart<Model>,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener?,
    wasMarkerVisible: Boolean,
    setWasMarkerVisible: (Boolean) -> Unit,
    lastMarkerEntryModels: List<Marker.EntryModel>,
    onMarkerEntryModelsChange: (List<Marker.EntryModel>) -> Unit,
) {
    markerTouchPoint
        ?.let(chart.entryLocationMap::getClosestMarkerEntryModel)
        ?.let { markerEntryModels ->
            chartValuesManager.getChartValues()
            marker.draw(
                context = this,
                bounds = chart.bounds,
                markedEntries = markerEntryModels,
                chartValuesProvider = chartValuesManager,
            )
            if (wasMarkerVisible.not()) {
                markerVisibilityChangeListener?.onMarkerShown(
                    marker = marker,
                    markerEntryModels = markerEntryModels,
                )
                setWasMarkerVisible(true)
            }
            val didMarkerMove = lastMarkerEntryModels.hasMoved(markerEntryModels)
            if (wasMarkerVisible && didMarkerMove) {
                onMarkerEntryModelsChange(markerEntryModels)
                if (lastMarkerEntryModels.isNotEmpty()) {
                    markerVisibilityChangeListener?.onMarkerMoved(
                        marker = marker,
                        markerEntryModels = markerEntryModels,
                    )
                }
            }
        } ?: marker
        .takeIf { wasMarkerVisible }
        ?.also {
            markerVisibilityChangeListener?.onMarkerHidden(marker = marker)
            setWasMarkerVisible(false)
        }
}

private fun List<Marker.EntryModel>.xPosition(): Float? = firstOrNull()?.entry?.x
private fun List<Marker.EntryModel>.hasMoved(other: List<Marker.EntryModel>): Boolean =
    xPosition() != other.xPosition()
