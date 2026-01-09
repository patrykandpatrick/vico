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

package com.patrykandpatrick.vico.views.cartesian

import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.views.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.views.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.views.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.views.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.views.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.views.cartesian.layer.MutableCartesianLayerDimensions
import com.patrykandpatrick.vico.views.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.views.common.MutableSize
import com.patrykandpatrick.vico.views.common.Size
import com.patrykandpatrick.vico.views.common.data.CacheStore
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.data.MutableExtraStore

internal class FakeCartesianMeasuringContext : CartesianMeasuringContext {
  override val model: CartesianChartModel
    get() = throw NotImplementedError()

  override val ranges: CartesianChartRanges
    get() = throw NotImplementedError()

  override val scrollEnabled: Boolean = false
  override val zoomEnabled: Boolean = false
  override val layerPadding: CartesianLayerPadding = CartesianLayerPadding()
  override val markerX: Double? = null
  override val canvasSize: Size = MutableSize(100f, 100f)
  override val density: Float = 1f
  override val extraStore: ExtraStore
    get() = throw NotImplementedError()

  override val cacheStore: CacheStore
    get() = throw NotImplementedError()

  override val isLtr: Boolean = true

  override fun spToPx(sp: Float): Float = sp * density
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
    extraStore: MutableExtraStore,
  ) {}

  override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {}

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
    extraStore: MutableExtraStore,
  ) {}

  override suspend fun transform(extraStore: MutableExtraStore, fraction: Float) {}

  override fun equals(other: Any?) = other is FakeColumnCartesianLayer

  override fun hashCode() = 0
}
