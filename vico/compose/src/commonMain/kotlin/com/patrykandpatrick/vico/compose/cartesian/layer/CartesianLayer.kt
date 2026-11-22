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
import com.patrykandpatrick.vico.compose.cartesian.accessibility.CandlestickCartesianLayerAggregation
import com.patrykandpatrick.vico.compose.cartesian.accessibility.CartesianDescriptionProvider
import com.patrykandpatrick.vico.compose.cartesian.accessibility.CartesianLayerAggregation
import com.patrykandpatrick.vico.compose.cartesian.accessibility.ColumnCartesianLayerAggregation
import com.patrykandpatrick.vico.compose.cartesian.accessibility.LineCartesianLayerAggregation
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
   * Returns an aggregation of [model] for [xRange], or `null` if the layer has no data in [xRange]
   * or does not support aggregation. The default [CartesianDescriptionProvider] describes only
   * [LineCartesianLayerAggregation]s, [ColumnCartesianLayerAggregation]s, and
   * [CandlestickCartesianLayerAggregation]s, so other aggregation types require a custom
   * [CartesianDescriptionProvider].
   *
   * @param model the [CartesianLayerModel].
   * @param xRange the range of _x_ values to aggregate.
   */
  public fun getAggregation(
    model: M,
    xRange: ClosedFloatingPointRange<Double>,
  ): CartesianLayerAggregation? = null

  /**
   * Returns the number of series in [model]. The default implementation returns `0`, indicating
   * that the layer contributes no series to the chart summary or interval descriptions.
   */
  public fun getSeriesCount(model: M): Int = 0

  /**
   * Returns the distinct _x_ values present in [model] in ascending order. The default
   * implementation returns an empty list, indicating that the layer contributes no intervals to the
   * accessibility overlay.
   */
  public fun getXValues(model: M): List<Double> = emptyList()

  /** Draws the [CartesianLayer]. */
  public fun draw(context: CartesianDrawingContext, model: M)

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
