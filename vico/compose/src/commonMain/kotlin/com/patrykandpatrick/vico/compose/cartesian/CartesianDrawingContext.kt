/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.AnimatedCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.common.DrawingContext
import com.patrykandpatrick.vico.compose.common.MutableDrawScope
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

internal fun CartesianDrawingContext.getVisibleXRange() =
  getVisibleXRange(layerDimensions, layerBounds, scroll)

internal fun CartesianDrawingContext(
  measuringContext: CartesianMeasuringContext,
  canvasSize: Size,
  canvas: Canvas,
  layerDimensions: CartesianLayerDimensions,
  layerBounds: Rect,
  scroll: Float,
  zoom: Float,
  mutableDrawScope: MutableDrawScope,
  ranges: CartesianChartRanges = measuringContext.ranges,
): CartesianDrawingContext =
  object : CartesianDrawingContext, CartesianMeasuringContext by measuringContext {
    override val canvasSize: Size = canvasSize

    override val ranges: CartesianChartRanges = ranges

    override var canvas = canvas

    override fun <D : Any> getDrawingModel(layer: AnimatedCartesianLayer<*, D>): D =
      measuringContext.getDrawingModel(layer)

    override val layerBounds: Rect = layerBounds

    override val layerDimensions: CartesianLayerDimensions = layerDimensions

    override val scroll: Float = scroll

    override val zoom: Float = zoom

    override val mutableDrawScope: MutableDrawScope = mutableDrawScope

    override fun withCanvas(canvas: Canvas, block: () -> Unit) {
      val originalCanvas = this.canvas
      this.canvas = canvas
      block()
      this.canvas = originalCanvas
    }
  }

internal fun CartesianDrawingContext.withRanges(
  ranges: CartesianChartRanges
): CartesianDrawingContext {
  val original = this
  return object : CartesianDrawingContext by original {
    override val ranges: CartesianChartRanges = ranges
  }
}
