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

import androidx.annotation.RestrictTo
import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.AnimatedCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.common.MeasuringContext

/** A [MeasuringContext] extension with [CartesianChart]-specific data. */
public interface CartesianMeasuringContext : MeasuringContext {
  /**
   * Aggregates the data of the [CartesianChart]’s [CartesianLayer]s. (A [CartesianLayer]’s own data
   * is [CartesianLayer.model].)
   */
  public val chartModel: CartesianChartModel

  /** Stores the [CartesianChart]’s _x_ and _y_ ranges. */
  public val ranges: CartesianChartRanges

  /** Whether scroll is enabled. */
  public val scrollEnabled: Boolean

  /** Whether zoom is enabled. */
  public val zoomEnabled: Boolean

  /** Stores the [CartesianLayer] padding values. */
  public val layerPadding: CartesianLayerPadding

  /** The marker’s _x_-value. */
  public val markerX: Double?

  public val markerSeriesIndex: Int?
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP) get

  /**
   * Returns the drawing model for [layer]. While drawing, this is the current frame of the
   * difference animation (which, while series are exiting, may include series absent from [layer]’s
   * model). Otherwise, it’s the drawing model for [layer]’s data.
   */
  public fun <D : Any> getDrawingModel(layer: AnimatedCartesianLayer<*, D>): D
}

internal fun CartesianMeasuringContext.getFullXRange(layerDimensions: CartesianLayerDimensions) =
  layerDimensions.run {
    val start = ranges.minX - startPadding / xSpacing * ranges.xStep
    val end = ranges.maxX + endPadding / xSpacing * ranges.xStep
    start..end
  }

internal fun CartesianMeasuringContext.getVisibleXRange(
  layerDimensions: CartesianLayerDimensions,
  layerBounds: Rect,
  scroll: Float,
): ClosedFloatingPointRange<Double> {
  val fullRange = getFullXRange(layerDimensions)
  val start =
    fullRange.start + layoutDirectionMultiplier * scroll / layerDimensions.xSpacing * ranges.xStep
  val end = start + layerBounds.width / layerDimensions.xSpacing * ranges.xStep
  return start..end
}
