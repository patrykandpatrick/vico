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

package com.patrykandpatrick.vico.compose.cartesian.layer

import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore

/**
 * Visualizes data on a Cartesian plane. [CartesianLayer]s are combined and drawn by
 * [CartesianChart]s.
 */
public interface CartesianLayer<M : CartesianLayerModel> : CartesianLayerMarginUpdater<M> {
  /** Links _x_ values to [CartesianMarker.Target]s. */
  public val markerTargets: Map<Double, List<CartesianMarker.Target>>

  /** Draws the [CartesianLayer]. */
  public fun draw(context: CartesianDrawingContext, model: M)

  /**
   * Draws the part of the [CartesianLayer] associated with [pass]. [CartesianLayer]s whose area
   * fills can’t be drawn separately from their strokes draw all of their content during
   * [DrawingPass.Strokes].
   */
  public fun draw(context: CartesianDrawingContext, model: M, pass: DrawingPass) {
    if (pass != DrawingPass.Fills) draw(context, model)
  }

  /** Denotes what part of a [CartesianLayer] is being drawn. */
  public enum class DrawingPass {
    /** Denotes that the entire [CartesianLayer] is being drawn. */
    All,
    /** Denotes that only the [CartesianLayer]’s area fills are being drawn. */
    Fills,
    /**
     * Denotes that everything except for the [CartesianLayer]’s area fills is being drawn—this
     * includes strokes, points, and data labels.
     */
    Strokes,
  }

  /** Updates [dimensions] to match this [CartesianLayer]’s dimensions. */
  public fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
    model: M,
  )

  /** Updates [chartRanges] in accordance with [model]. */
  public fun updateChartRanges(chartRanges: MutableCartesianChartRanges, model: M)

  /** Prepares the [CartesianLayer] for a difference animation. */
  public fun prepareForTransformation(
    model: M?,
    ranges: CartesianChartRanges,
    extraStore: MutableExtraStore,
  )

  /** Carries out the pending difference animation. */
  public suspend fun transform(extraStore: MutableExtraStore, fraction: Float)
}
