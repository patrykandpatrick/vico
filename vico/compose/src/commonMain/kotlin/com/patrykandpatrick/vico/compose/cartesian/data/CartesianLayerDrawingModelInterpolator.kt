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

package com.patrykandpatrick.vico.compose.cartesian.data

import com.patrykandpatrick.vico.compose.cartesian.layer.AnimatedCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer

/**
 * Creates [Transition]s between drawing models of type [D] for [AnimatedCartesianLayer]s. This is
 * stateless: the chart host owns the animation and asks for one [Transition] per data update.
 * Implementations must be pure functions of their arguments and of the values they capture, and
 * they’re called off the main thread.
 *
 * To animate a series out after it’s removed from a built-in layer’s model, include it in the
 * frames, keyed by its series key (e.g., at its former position in `seriesKeys`, with its entries
 * shrinking). The host draws it with the data and style it had, so the new layer’s providers aren’t
 * asked about it. A series that’s in neither drawing model isn’t drawn.
 *
 * In a composable function, a lambda converted to [CartesianLayerDrawingModelInterpolator] is
 * memoized while the values it captures are unchanged (compared via `equals` for stable types and
 * by identity otherwise), so it needs no explicit `remember`.
 */
public fun interface CartesianLayerDrawingModelInterpolator<D : Any> {
  /**
   * Returns a [Transition] from [old] to [new]. [old] is the drawing model on screen when the data
   * update occurs—possibly an intermediate one, if an animation is in progress—or `null` if the
   * [AnimatedCartesianLayer] is appearing (for the initial animation or because the layer was just
   * added).
   */
  public fun createTransition(old: D?, new: D): Transition<D>

  /** Interpolates between two drawing models. */
  public fun interface Transition<D : Any> {
    /**
     * Returns the drawing model for [fraction], where 0 corresponds to the initial drawing model,
     * and 1 corresponds to the target drawing model. This is called off the main thread.
     */
    public fun interpolate(fraction: Float): D
  }

  /** Houses [CartesianLayerDrawingModelInterpolator] factory functions. */
  public companion object {
    /**
     * Creates a [CartesianLayerDrawingModelInterpolator] for [LineCartesianLayer]s. If [sweep] is
     * `true`, the [LineCartesianLayer]’s first appearance is animated as a clip sweeping in from
     * the start edge at full opacity and _y_, rather than the default grow-from-baseline and
     * fade-in.
     */
    public fun line(
      sweep: Boolean = false
    ): CartesianLayerDrawingModelInterpolator<LineCartesianLayerDrawingModel> =
      LineCartesianLayerDrawingModelInterpolator(sweep)

    /** Creates a [CartesianLayerDrawingModelInterpolator] for [ColumnCartesianLayer]s. */
    public fun column(): CartesianLayerDrawingModelInterpolator<ColumnCartesianLayerDrawingModel> =
      ColumnCartesianLayerDrawingModelInterpolator

    /** Creates a [CartesianLayerDrawingModelInterpolator] for [CandlestickCartesianLayer]s. */
    public fun candlestick():
      CartesianLayerDrawingModelInterpolator<CandlestickCartesianLayerDrawingModel> =
      CandlestickCartesianLayerDrawingModelInterpolator
  }
}
