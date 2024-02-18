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

package com.patrykandpatrick.vico.core.cartesian.draw

import android.graphics.Canvas
import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.extension.getClosestMarkerEntryModel

/**
 * The anonymous implementation of [CartesianChartDrawContext].
 *
 * @param canvas the canvas on which the [CartesianChart] is to be drawn.
 * @param elevationOverlayColor the color of elevation overlays, applied to [ShapeComponent]s that cast shadows.
 * @param measureContext holds data used for component measurements.
 * @param markerTouchPoint the point inside the chart’s bounds where physical touch is occurring.
 * @param horizontalDimensions holds information on the [CartesianChart]’s horizontal dimensions.
 * @param chartBounds the bounds in which the [CartesianChart] will be drawn.
 * @param horizontalScroll the horizontal scroll.
 * @param zoom the zoom factor.
 *
 * @see [ShapeComponent.setShadow]
 */
public fun cartesianChartDrawContext(
    canvas: Canvas,
    elevationOverlayColor: Int,
    measureContext: CartesianMeasureContext,
    markerTouchPoint: Point?,
    horizontalDimensions: HorizontalDimensions,
    chartBounds: RectF,
    horizontalScroll: Float,
    zoom: Float,
): CartesianChartDrawContext =
    object : CartesianChartDrawContext, CartesianMeasureContext by measureContext {
        override val chartBounds: RectF = chartBounds

        override var canvas: Canvas = canvas

        override val elevationOverlayColor: Long = elevationOverlayColor.toLong()

        override val markerTouchPoint: Point? = markerTouchPoint

        override val zoom: Float = zoom

        override val horizontalDimensions: HorizontalDimensions = horizontalDimensions.scaled(zoom)

        override val horizontalScroll: Float = horizontalScroll

        override fun withOtherCanvas(
            canvas: Canvas,
            block: (DrawContext) -> Unit,
        ) {
            val originalCanvas = this.canvas
            this.canvas = canvas
            block(this)
            this.canvas = originalCanvas
        }
    }

/**
 * Draws the provided [marker] on top of the chart at the given [markerTouchPoint] and notifies the
 * [markerVisibilityChangeListener] about the [marker]’s visibility changes.
 */
public fun CartesianChartDrawContext.drawMarker(
    marker: CartesianMarker,
    markerTouchPoint: Point?,
    chart: CartesianChart,
    markerVisibilityChangeListener: CartesianMarkerVisibilityChangeListener?,
    wasMarkerVisible: Boolean,
    setWasMarkerVisible: (Boolean) -> Unit,
    lastMarkerEntryModels: List<CartesianMarker.EntryModel>,
    onMarkerEntryModelsChange: (List<CartesianMarker.EntryModel>) -> Unit,
) {
    markerTouchPoint
        ?.let(chart.entryLocationMap::getClosestMarkerEntryModel)
        ?.let { markerEntryModels ->
            marker.draw(
                context = this,
                bounds = chart.bounds,
                markedEntries = markerEntryModels,
                chartValues = chartValues,
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

private fun List<CartesianMarker.EntryModel>.xPosition(): Float? = firstOrNull()?.entry?.x

private fun List<CartesianMarker.EntryModel>.hasMoved(other: List<CartesianMarker.EntryModel>): Boolean =
    xPosition() != other.xPosition()
