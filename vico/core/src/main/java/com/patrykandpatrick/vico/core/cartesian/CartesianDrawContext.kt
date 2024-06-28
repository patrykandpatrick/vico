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
import com.patrykandpatrick.vico.core.common.ceil
import kotlin.math.abs

/** A [DrawContext] extension with [CartesianChart]-specific data. */
public interface CartesianDrawContext : DrawContext, CartesianMeasureContext {
  /** The bounds in which the [CartesianChart] will be drawn. */
  public val chartBounds: RectF

  /** Holds information on the [CartesianChart]’s horizontal dimensions. */
  public val horizontalDimensions: HorizontalDimensions

  /** The point inside the chart’s coordinates where physical touch is occurring. */
  public val markerTouchPoint: Point?

  /** The scroll value (in pixels). */
  public val scroll: Float

  /** The scroll value (in pixels). */
  @Deprecated("Use `scroll`.", ReplaceWith("scroll"))
  public val horizontalScroll: Float
    get() = scroll

  /** The zoom factor. */
  public val zoom: Float
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun CartesianMeasureContext.getMaxScrollDistance(
  chartWidth: Float,
  horizontalDimensions: HorizontalDimensions,
): Float =
  (layoutDirectionMultiplier * (horizontalDimensions.getContentWidth(this) - chartWidth))
    .run { if (isLtr) coerceAtLeast(minimumValue = 0f) else coerceAtMost(maximumValue = 0f) }
    .ceil

internal fun CartesianDrawContext.getMaxScrollDistance() =
  getMaxScrollDistance(chartBounds.width(), horizontalDimensions)

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun CartesianDrawContext(
  canvas: Canvas,
  elevationOverlayColor: Int,
  measureContext: CartesianMeasureContext,
  markerTouchPoint: Point?,
  horizontalDimensions: HorizontalDimensions,
  chartBounds: RectF,
  scroll: Float,
  zoom: Float,
): CartesianDrawContext =
  object : CartesianDrawContext, CartesianMeasureContext by measureContext {
    override val chartBounds: RectF = chartBounds

    override var canvas: Canvas = canvas

    override val elevationOverlayColor: Long = elevationOverlayColor.toLong()

    override val markerTouchPoint: Point? = markerTouchPoint

    override val horizontalDimensions: HorizontalDimensions = horizontalDimensions

    override val scroll: Float = scroll

    override val zoom: Float = zoom

    override fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit) {
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
  previousTargetHashCode: Int?,
): Int? {
  if (markerTouchPoint == null || chart.markerTargets.isEmpty()) {
    if (previousTargetHashCode != null) visibilityListener?.onHidden(marker)
    return null
  }
  var targets = emptyList<CartesianMarker.Target>()
  var previousDistance = Float.POSITIVE_INFINITY
  for (xTargets in chart.markerTargets.values) {
    val (distance, canvasXTargets) =
      xTargets.groupBy { abs(markerTouchPoint.x - it.canvasX) }.minBy { it.key }
    if (distance > previousDistance) break
    targets = canvasXTargets
    previousDistance = distance
  }
  marker.draw(this, targets)
  val targetHashCode = targets.hashCode()
  if (previousTargetHashCode == null) {
    visibilityListener?.onShown(marker, targets)
  } else if (targetHashCode != previousTargetHashCode) {
    visibilityListener?.onUpdated(marker, targets)
  }
  return targetHashCode
}
