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

import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.MutableCartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.core.common.Point

@Composable
internal fun rememberCartesianMeasuringContext(
  canvasBounds: RectF,
  model: CartesianChartModel,
  ranges: CartesianChartRanges,
  scrollEnabled: Boolean,
  zoomEnabled: Boolean,
  layerPadding: CartesianLayerPadding,
  spToPx: (Float) -> Float,
  pointerPosition: Point?,
): CartesianMeasuringContext {
  val density = LocalDensity.current.density
  val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
  return remember(
    canvasBounds,
    model,
    ranges,
    scrollEnabled,
    zoomEnabled,
    layerPadding,
    spToPx,
    pointerPosition,
  ) {
    MutableCartesianMeasuringContext(
      canvasBounds,
      density,
      isLtr,
      model,
      ranges,
      scrollEnabled,
      zoomEnabled,
      layerPadding,
      pointerPosition,
      spToPx,
    )
  }
}
