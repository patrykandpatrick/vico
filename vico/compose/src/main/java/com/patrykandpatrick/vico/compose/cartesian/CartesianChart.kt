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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianLayerPadding
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.FadingEdges
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.Legend
import com.patrykandpatrick.vico.core.common.ValueWrapper
import com.patrykandpatrick.vico.core.common.data.ExtraStore

/**
 * Creates and remembers a [CartesianChart].
 *
 * @see rememberCandlestickCartesianLayer
 * @see rememberColumnCartesianLayer
 * @see rememberLineCartesianLayer
 */
@Composable
public fun rememberCartesianChart(
  vararg layers: CartesianLayer<*>,
  startAxis: Axis<Axis.Position.Vertical.Start>? = null,
  topAxis: Axis<Axis.Position.Horizontal.Top>? = null,
  endAxis: Axis<Axis.Position.Vertical.End>? = null,
  bottomAxis: Axis<Axis.Position.Horizontal.Bottom>? = null,
  marker: CartesianMarker? = null,
  markerVisibilityListener: CartesianMarkerVisibilityListener? = null,
  layerPadding: ((ExtraStore) -> CartesianLayerPadding) = { cartesianLayerPadding() },
  legend: Legend<CartesianMeasuringContext, CartesianDrawingContext>? = null,
  fadingEdges: FadingEdges? = null,
  decorations: List<Decoration> = emptyList(),
  persistentMarkers: (CartesianChart.PersistentMarkerScope.(ExtraStore) -> Unit)? = null,
  getXStep: ((CartesianChartModel) -> Double) = { it.getXDeltaGcd() },
): CartesianChart {
  val wrapper = remember { ValueWrapper<CartesianChart?>(null) }
  return remember(
    *layers,
    startAxis,
    topAxis,
    endAxis,
    bottomAxis,
    marker,
    markerVisibilityListener,
    layerPadding,
    legend,
    fadingEdges,
    decorations,
    persistentMarkers,
    getXStep,
  ) {
    val cartesianChart =
      wrapper.value?.copy(
        *layers,
        startAxis = startAxis,
        topAxis = topAxis,
        endAxis = endAxis,
        bottomAxis = bottomAxis,
        marker = marker,
        markerVisibilityListener = markerVisibilityListener,
        layerPadding = layerPadding,
        legend = legend,
        fadingEdges = fadingEdges,
        decorations = decorations,
        persistentMarkers = persistentMarkers,
        getXStep = getXStep,
      )
        ?: CartesianChart(
          *layers,
          startAxis = startAxis,
          topAxis = topAxis,
          endAxis = endAxis,
          bottomAxis = bottomAxis,
          marker = marker,
          markerVisibilityListener = markerVisibilityListener,
          layerPadding = layerPadding,
          legend = legend,
          fadingEdges = fadingEdges,
          decorations = decorations,
          persistentMarkers = persistentMarkers,
          getXStep = getXStep,
        )
    wrapper.value = cartesianChart
    cartesianChart
  }
}
