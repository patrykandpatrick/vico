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
import com.patrykandpatrick.vico.views.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.views.common.MutableMeasuringContext
import com.patrykandpatrick.vico.views.common.Size
import com.patrykandpatrick.vico.views.common.data.CacheStore
import com.patrykandpatrick.vico.views.common.data.ExtraStore

internal class MutableCartesianMeasuringContext(
  override val canvasSize: Size,
  override var density: Float,
  override var extraStore: ExtraStore,
  override var isLtr: Boolean,
  spToPx: (Float) -> Float,
  override var model: CartesianChartModel,
  override var ranges: CartesianChartRanges,
  override var scrollEnabled: Boolean,
  override var zoomEnabled: Boolean,
  override var layerPadding: CartesianLayerPadding,
  override var markerX: Double?,
  cacheStore: CacheStore = CacheStore(),
) :
  MutableMeasuringContext(canvasSize, density, extraStore, isLtr, spToPx, cacheStore),
  CartesianMeasuringContext
