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
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.DrawingContext
import com.patrykandpatrick.vico.core.common.Point
import kotlin.math.ceil

/** A [DrawingContext] extension with [CartesianChart]-specific data. */
public interface CartesianDrawingContext : DrawingContext, CartesianMeasuringContext {
  /** The bounds of the [CartesianLayer] area. */
  public val layerBounds: RectF

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
public fun CartesianMeasuringContext.getMaxScrollDistance(
  chartWidth: Float,
  horizontalDimensions: HorizontalDimensions,
): Float =
  ceil(
    (layoutDirectionMultiplier * (horizontalDimensions.getContentWidth(this) - chartWidth)).run {
      if (isLtr) coerceAtLeast(0f) else coerceAtMost(0f)
    }
  )

internal fun CartesianDrawingContext.getMaxScrollDistance() =
  getMaxScrollDistance(layerBounds.width(), horizontalDimensions)

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun CartesianDrawingContext(
  measuringContext: CartesianMeasuringContext,
  canvas: Canvas,
  markerTouchPoint: Point?,
  horizontalDimensions: HorizontalDimensions,
  layerBounds: RectF,
  scroll: Float,
  zoom: Float,
): CartesianDrawingContext =
  object : CartesianDrawingContext, CartesianMeasuringContext by measuringContext {
    override val layerBounds: RectF = layerBounds

    override var canvas: Canvas = canvas

    override val markerTouchPoint: Point? = markerTouchPoint

    override val horizontalDimensions: HorizontalDimensions = horizontalDimensions

    override val scroll: Float = scroll

    override val zoom: Float = zoom

    override fun withOtherCanvas(canvas: Canvas, block: () -> Unit) {
      val originalCanvas = this.canvas
      this.canvas = canvas
      block()
      this.canvas = originalCanvas
    }
  }
