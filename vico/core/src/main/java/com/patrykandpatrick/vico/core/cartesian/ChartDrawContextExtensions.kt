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

package com.patrykandpatrick.vico.core.cartesian

import android.graphics.Canvas
import android.graphics.RectF
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.DrawContext
import com.patrykandpatrick.vico.core.common.Point
import kotlin.math.abs

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun CartesianDrawContext(
    canvas: Canvas,
    elevationOverlayColor: Int,
    measureContext: CartesianMeasureContext,
    markerTouchPoint: Point?,
    horizontalDimensions: HorizontalDimensions,
    chartBounds: RectF,
    horizontalScroll: Float,
    zoom: Float,
): CartesianDrawContext =
    object : CartesianDrawContext, CartesianMeasureContext by measureContext {
        override val chartBounds: RectF = chartBounds

        override var canvas: Canvas = canvas

        override val elevationOverlayColor: Long = elevationOverlayColor.toLong()

        override val markerTouchPoint: Point? = markerTouchPoint

        override val zoom: Float = zoom

        override val horizontalDimensions: HorizontalDimensions = horizontalDimensions

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

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun CartesianDrawContext.drawMarker(
    marker: CartesianMarker,
    markerTouchPoint: Point?,
    chart: CartesianChart,
    visibilityListener: CartesianMarkerVisibilityListener?,
    previousX: Float?,
): Float? {
    if (markerTouchPoint == null || chart.markerTargets.isEmpty()) {
        if (previousX != null) visibilityListener?.onHidden(marker)
        return null
    }
    var targets = chart.markerTargets.values.first()
    var previousDistance = abs(markerTouchPoint.x - targets.first().canvasX)
    for (i in 1..<chart.markerTargets.size) {
        val potentialTargets = chart.markerTargets.values.elementAt(i)
        val distance = abs(markerTouchPoint.x - potentialTargets.first().canvasX)
        if (distance > previousDistance) break
        targets = potentialTargets
        previousDistance = distance
    }
    marker.draw(this, targets)
    val x = targets.first().x
    if (previousX == null) {
        visibilityListener?.onShown(marker, targets)
    } else if (x != previousX) {
        visibilityListener?.onMoved(marker, targets)
    }
    return x
}
