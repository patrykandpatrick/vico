/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.MutableCartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.core.common.Point
import com.patrykandpatrick.vico.core.common.data.CacheStore
import com.patrykandpatrick.vico.core.common.data.ExtraStore

@Composable
internal fun rememberCartesianMeasuringContext(
  canvasBounds: RectF,
  extraStore: ExtraStore,
  model: CartesianChartModel,
  ranges: CartesianChartRanges,
  scrollEnabled: Boolean,
  zoomEnabled: Boolean,
  layerPadding: CartesianLayerPadding,
  pointerPosition: Point?,
  isMarkerVisible: Boolean,
): CartesianMeasuringContext {
  val density = LocalDensity.current
  val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
  val cacheStore = remember { CacheStore() }
  return remember(
    canvasBounds,
    density,
    extraStore,
    isLtr,
    model,
    ranges,
    scrollEnabled,
    zoomEnabled,
    layerPadding,
    pointerPosition,
    cacheStore,
    isMarkerVisible,
  ) {
    MutableCartesianMeasuringContext(
      canvasBounds = canvasBounds,
      density = density.density,
      extraStore = extraStore,
      isLtr = isLtr,
      spToPx = density.run { { it.sp.toPx() } },
      model = model,
      ranges = ranges,
      scrollEnabled = scrollEnabled,
      zoomEnabled = zoomEnabled,
      layerPadding = layerPadding,
      pointerPosition = pointerPosition,
      cacheStore = cacheStore,
      isMarkerVisible = isMarkerVisible,
    )
  }
}
