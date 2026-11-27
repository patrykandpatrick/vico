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

import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController

internal fun testCartesianChart(
  vararg layers: CartesianLayer<*>,
  marker: CartesianMarker? = null,
  persistentMarkers: Map<out Number, CartesianMarker> = emptyMap(),
  xStep: Double? = null,
  fadingEdges: FadingEdges? = null,
): CartesianChart =
  CartesianChart(
    layers = layers.toList(),
    startAxis = null,
    topAxis = null,
    endAxis = null,
    bottomAxis = null,
    marker = marker,
    markerVisibilityListener = null,
    layerPadding = CartesianLayerPadding(),
    legend = null,
    fadingEdges = fadingEdges,
    decorations = emptyList(),
    persistentMarkers = persistentMarkers,
    xStep = xStep,
    markerController = CartesianMarkerController.showOnPress(),
  )
