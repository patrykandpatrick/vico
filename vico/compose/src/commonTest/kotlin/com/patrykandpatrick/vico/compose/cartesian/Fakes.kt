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

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker

internal class FakeCartesianMeasuringContext : CartesianMeasuringContext {
  override val model: CartesianChartModel
    get() = throw NotImplementedError()

  override val ranges: CartesianChartRanges
    get() = throw NotImplementedError()

  override val scrollEnabled: Boolean = false
  override val zoomEnabled: Boolean = false
  override val layerPadding: CartesianLayerPadding = CartesianLayerPadding()
  override val markerX: Double? = null
  override val canvasSize: Size = Size(100f, 100f)
  override val density: Float = 1f
  override val isLtr: Boolean = true
  override val layoutDirection: LayoutDirection = LayoutDirection.Ltr
  override val spToPx: Density
    get() = throw NotImplementedError()

  override val fontFamilyResolver: androidx.compose.ui.text.font.FontFamily.Resolver
    get() = throw NotImplementedError()
}

internal fun FakeMutableCartesianLayerDimensions() = MutableCartesianLayerDimensions()

internal class FakeLineCartesianLayer : CartesianLayer<LineCartesianLayerModel> {
  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = emptyMap()

  override fun draw(context: CartesianDrawingContext, model: LineCartesianLayerModel) {}

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
    model: LineCartesianLayerModel,
  ) {}

  override fun updateChartRanges(
    chartRanges: MutableCartesianChartRanges,
    model: LineCartesianLayerModel,
  ) {}

  override fun prepareForTransformation(
    model: LineCartesianLayerModel?,
    ranges: CartesianChartRanges,
    extraStore: com.patrykandpatrick.vico.compose.common.data.ExtraStore,
  ) {}

  override suspend fun transform(
    extraStore: com.patrykandpatrick.vico.compose.common.data.ExtraStore,
    fraction: Float,
  ) {}

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
    model: LineCartesianLayerModel,
  ) {}

  override fun equals(other: Any?) = other is FakeLineCartesianLayer

  override fun hashCode() = 0
}

internal class FakeColumnCartesianLayer : CartesianLayer<ColumnCartesianLayerModel> {
  override val markerTargets: Map<Double, List<CartesianMarker.Target>> = emptyMap()

  override fun draw(context: CartesianDrawingContext, model: ColumnCartesianLayerModel) {}

  override fun updateDimensions(
    context: CartesianMeasuringContext,
    dimensions: MutableCartesianLayerDimensions,
    model: ColumnCartesianLayerModel,
  ) {}

  override fun updateChartRanges(
    chartRanges: MutableCartesianChartRanges,
    model: ColumnCartesianLayerModel,
  ) {}

  override fun prepareForTransformation(
    model: ColumnCartesianLayerModel?,
    ranges: CartesianChartRanges,
    extraStore: com.patrykandpatrick.vico.compose.common.data.ExtraStore,
  ) {}

  override suspend fun transform(
    extraStore: com.patrykandpatrick.vico.compose.common.data.ExtraStore,
    fraction: Float,
  ) {}

  override fun updateLayerMargins(
    context: CartesianMeasuringContext,
    layerMargins: CartesianLayerMargins,
    layerDimensions: CartesianLayerDimensions,
    model: ColumnCartesianLayerModel,
  ) {}

  override fun equals(other: Any?) = other is FakeColumnCartesianLayer

  override fun hashCode() = 0
}
