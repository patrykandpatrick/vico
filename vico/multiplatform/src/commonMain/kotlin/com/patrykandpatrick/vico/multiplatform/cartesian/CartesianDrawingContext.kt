/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.common.DrawingContext
import kotlin.math.ceil

/** A [DrawingContext] extension with [CartesianChart]-specific data. */
public interface CartesianDrawingContext : DrawingContext, CartesianMeasuringContext {
  /** The bounds of the [CartesianLayer] area. */
  public val layerBounds: Rect

  /** Stores shared [CartesianLayer] dimensions. */
  public val layerDimensions: CartesianLayerDimensions

  /** The scroll value (in pixels). */
  public val scroll: Float

  /** The zoom factor. */
  public val zoom: Float
}

internal fun CartesianMeasuringContext.getMaxScrollDistance(
  chartWidth: Float,
  layerDimensions: CartesianLayerDimensions,
): Float =
  ceil(
    (layoutDirectionMultiplier * (layerDimensions.getContentWidth(this) - chartWidth)).run {
      if (isLtr) coerceAtLeast(0f) else coerceAtMost(0f)
    }
  )

internal fun CartesianDrawingContext.getMaxScrollDistance() =
  getMaxScrollDistance(layerBounds.width, layerDimensions)

internal fun CartesianDrawingContext(
  measuringContext: CartesianMeasuringContext,
  canvas: Canvas,
  layerDimensions: CartesianLayerDimensions,
  layerBounds: Rect,
  scroll: Float,
  zoom: Float,
): CartesianDrawingContext =
  object : CartesianDrawingContext, CartesianMeasuringContext by measuringContext {
    override var canvas = canvas

    override val layerBounds: Rect = layerBounds

    override val layerDimensions: CartesianLayerDimensions = layerDimensions

    override val scroll: Float = scroll

    override val zoom: Float = zoom

    override fun withCanvas(canvas: Canvas, block: () -> Unit) {
      val originalCanvas = this.canvas
      this.canvas = canvas
      block()
      this.canvas = originalCanvas
    }
  }
