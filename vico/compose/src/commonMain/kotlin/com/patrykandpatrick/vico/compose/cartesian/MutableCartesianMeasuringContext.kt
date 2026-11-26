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

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.AnimatedCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.common.MeasuringContext
import com.patrykandpatrick.vico.compose.common.data.CacheStore

internal class MutableCartesianMeasuringContext(
  override var canvasWidth: Float,
  override var fontFamilyResolver: FontFamily.Resolver,
  override var density: Density,
  override var layoutDirection: LayoutDirection,
  override var chartModel: CartesianChartModel,
  override var ranges: CartesianChartRanges,
  override var scrollEnabled: Boolean,
  override var zoomEnabled: Boolean,
  override var layerPadding: CartesianLayerPadding,
  override var markerX: Double?,
  override var markerSeriesIndex: Int?,
  override val cacheStore: CacheStore = CacheStore(),
  var drawingModels: Map<Any, Any> = emptyMap(),
) : MeasuringContext, CartesianMeasuringContext {
  @Suppress("UNCHECKED_CAST")
  override fun <D : Any> getDrawingModel(layer: AnimatedCartesianLayer<*, D>): D =
    drawingModels[layer.key] as D?
      ?: error("No drawing model for the layer with key ${layer.key}. Is it part of the chart?")
}
