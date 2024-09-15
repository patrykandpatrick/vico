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

package com.patrykandpatrick.vico.core.cartesian.layer

import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianLayerInsetter
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore

/**
 * Visualizes data on a Cartesian plane. [CartesianLayer]s are combined and drawn by
 * [CartesianChart]s.
 */
public interface CartesianLayer<M : CartesianLayerModel> : CartesianLayerInsetter<M> {
  /** Links _x_ values to [CartesianMarker.Target]s. */
  public val markerTargets: Map<Double, List<CartesianMarker.Target>>

  /** Draws the [CartesianLayer]. */
  public fun draw(context: CartesianDrawingContext, model: M)

  /** Updates [horizontalDimensions] to match this [CartesianLayer]’s dimensions. */
  public fun updateHorizontalDimensions(
    context: CartesianMeasuringContext,
    horizontalDimensions: MutableHorizontalDimensions,
    model: M,
  )

  /** Updates [ranges] in accordance with [model]. */
  public fun updateRanges(ranges: MutableCartesianChartRanges, model: M)

  /** Prepares the [CartesianLayer] for a difference animation. */
  public fun prepareForTransformation(
    model: M?,
    ranges: CartesianChartRanges,
    extraStore: MutableExtraStore,
  )

  /** Carries out the pending difference animation. */
  public suspend fun transform(extraStore: MutableExtraStore, fraction: Float)
}
