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

  /**
   * Draws the parts of the [CartesianLayer] that belong to [phases]. Within one frame, each
   * [DrawingPhase] is passed exactly once, in declaration order—either together in a single call,
   * or one per call, so that the [CartesianChart] can draw other content in between.
   *
   * Implementations guard each phase independently:
   * ```
   * if (DrawingPhase.AreaFills in phases) { /* … */ }
   * if (DrawingPhase.Content in phases) { /* … */ }
   * ```
   *
   * [phases] can be ignored unless [canSeparateAreaFills] is overridden to return `true`, because a
   * [CartesianLayer] that never separates its area fills is always passed every phase at once.
   */
  public fun draw(context: CartesianDrawingContext, model: M, phases: Set<DrawingPhase>)

  /**
   * Whether this [CartesianLayer] can draw [DrawingPhase.AreaFills] separately from
   * [DrawingPhase.Content] for [model]. Return `false` when there’s nothing to separate—no area
   * fills, or a configuration that keeps them next to the content they belong to—or when separating
   * them would be incorrect, as during a difference animation, where the two phases would land in
   * different opacity groups.
   */
  public fun canSeparateAreaFills(context: CartesianDrawingContext, model: M): Boolean = false

  /** Denotes a part of a [CartesianLayer], for the purpose of drawing order. */
  public enum class DrawingPhase {
    /** Denotes the [CartesianLayer]’s area fills. */
    AreaFills,
    /**
     * Denotes everything except for the [CartesianLayer]’s area fills—strokes, points, and data
     * labels.
     */
    Content;

    /** Houses [DrawingPhase] constants. */
    public companion object {
      /** Every [DrawingPhase]. */
      public val All: Set<DrawingPhase> = entries.toSet()
    }
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
