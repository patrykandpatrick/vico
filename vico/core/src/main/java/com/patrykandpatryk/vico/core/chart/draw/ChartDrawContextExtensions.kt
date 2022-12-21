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

package com.patrykandpatryk.vico.core.chart.draw

import android.graphics.Canvas
import android.graphics.RectF
import com.patrykandpatryk.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatryk.vico.core.chart.Chart
import com.patrykandpatryk.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.context.DrawContext
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.extension.getClosestMarkerEntryModel
import com.patrykandpatryk.vico.core.marker.Marker
import com.patrykandpatryk.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatryk.vico.core.model.Point

/**
 * The anonymous implementation of [ChartDrawContext].
 *
 * @param canvas the canvas on which the [Chart] is to be drawn.
 * @param elevationOverlayColor the color of elevation overlays, applied to [ShapeComponent]s that cast shadows.
 * @param measureContext holds data used for component measurements.
 * @param markerTouchPoint the point inside the chart’s bounds where physical touch is occurring.
 * @param segmentProperties holds information about the width of each individual chart segment.
 * @param chartBounds the bounds in which the [Chart] will be drawn.
 * @param horizontalScroll the horizontal scroll.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the entry count and
 * intrinsic segment width are such that, at a scale factor of 1, an empty space would be visible near the end edge of
 * the chart.
 *
 * @see [ShapeComponent.setShadow]
 */
@LongParameterListDrawFunction
public fun chartDrawContext(
    canvas: Canvas,
    elevationOverlayColor: Int,
    measureContext: MeasureContext,
    markerTouchPoint: Point?,
    segmentProperties: SegmentProperties,
    chartBounds: RectF,
    horizontalScroll: Float,
    autoScaleUp: AutoScaleUp,
): ChartDrawContext = object : ChartDrawContext, MeasureContext by measureContext {

    override val chartBounds: RectF = chartBounds

    override var canvas: Canvas = canvas

    override val elevationOverlayColor: Long = elevationOverlayColor.toLong()

    override val markerTouchPoint: Point? = markerTouchPoint

    override val chartScale: Float = calculateDrawScale()

    override val segmentProperties: SegmentProperties = segmentProperties.scaled(chartScale)

    override val horizontalScroll: Float = horizontalScroll

    override fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit) {
        val originalCanvas = this.canvas
        this.canvas = canvas
        block(this)
        this.canvas = originalCanvas
    }

    private fun calculateDrawScale(): Float {
        val drawnEntryWidth = segmentProperties.segmentWidth * chartValuesManager.getChartValues().getDrawnEntryCount()
        val upscalingPossibleButDisallowed = drawnEntryWidth < chartBounds.width() && autoScaleUp == AutoScaleUp.None
        val scrollEnabledAndUpscalingImpossible = isHorizontalScrollEnabled && drawnEntryWidth >= chartBounds.width()
        return if (upscalingPossibleButDisallowed || scrollEnabledAndUpscalingImpossible) {
            measureContext.chartScale
        } else {
            chartBounds.width() / drawnEntryWidth
        }
    }
}

internal inline val ChartDrawContext.segmentWidth: Int
    get() = segmentProperties.segmentWidth.pixels.toInt()

/**
 * Draws the provided [marker] on top of the chart at the given [markerTouchPoint] and notifies the
 * [markerVisibilityChangeListener] about the [marker]’s visibility changes.
 */
@LongParameterListDrawFunction
public fun <Model : ChartEntryModel> ChartDrawContext.drawMarker(
    marker: Marker,
    markerTouchPoint: Point?,
    chart: Chart<Model>,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener?,
    wasMarkerVisible: Boolean,
    setWasMarkerVisible: (Boolean) -> Unit,
) {
    markerTouchPoint
        ?.let(chart.entryLocationMap::getClosestMarkerEntryModel)
        ?.let { markerEntryModels ->
            marker.draw(
                context = this,
                bounds = chart.bounds,
                markedEntries = markerEntryModels,
            )
            if (wasMarkerVisible.not()) {
                markerVisibilityChangeListener?.onMarkerShown(
                    marker = marker,
                    markerEntryModels = markerEntryModels,
                )
                setWasMarkerVisible(true)
            }
        } ?: marker
        .takeIf { wasMarkerVisible }
        ?.also {
            markerVisibilityChangeListener?.onMarkerHidden(marker = marker)
            setWasMarkerVisible(false)
        }
}
