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

package com.patrykandpatrick.vico.multiplatform.cartesian

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalLayoutDirection
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.multiplatform.common.MeasuringContext
import com.patrykandpatrick.vico.multiplatform.common.Point
import com.patrykandpatrick.vico.multiplatform.common.data.CacheStore
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore

/** A [MeasuringContext] extension with [CartesianChart]-specific data. */
public interface CartesianMeasuringContext : MeasuringContext {
  /** Stores the [CartesianChart]’s data. */
  public val model: CartesianChartModel

  /** Stores the [CartesianChart]’s _x_ and _y_ ranges. */
  public val ranges: CartesianChartRanges

  /** Whether scroll is enabled. */
  public val scrollEnabled: Boolean

  /** Whether zoom is enabled. */
  public val zoomEnabled: Boolean

  /** Stores the [CartesianLayer] padding values. */
  public val layerPadding: CartesianLayerPadding

  /** The pointer position. */
  public val pointerPosition: Point?
}

internal fun CartesianMeasuringContext.getFullXRange(layerDimensions: CartesianLayerDimensions) =
  layerDimensions.run {
    val start = ranges.minX - startPadding / xSpacing * ranges.xStep
    val end = ranges.maxX + endPadding / xSpacing * ranges.xStep
    start..end
  }

@Composable
internal fun rememberCartesianMeasuringContext(
  extraStore: ExtraStore,
  model: CartesianChartModel,
  ranges: CartesianChartRanges,
  scrollEnabled: Boolean,
  zoomEnabled: Boolean,
  layerPadding: CartesianLayerPadding,
  pointerPosition: Point?,
): MutableCartesianMeasuringContext {
  val fontFamilyResolver = LocalFontFamilyResolver.current
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  val cacheStore = remember { CacheStore() }
  return remember(
    fontFamilyResolver,
    density,
    extraStore,
    layoutDirection,
    model,
    ranges,
    scrollEnabled,
    zoomEnabled,
    layerPadding,
    pointerPosition,
    cacheStore,
  ) {
    MutableCartesianMeasuringContext(
      canvasSize = Size.Zero,
      fontFamilyResolver = fontFamilyResolver,
      density = density,
      extraStore = extraStore,
      layoutDirection = layoutDirection,
      model = model,
      ranges = ranges,
      scrollEnabled = scrollEnabled,
      zoomEnabled = zoomEnabled,
      layerPadding = layerPadding,
      pointerPosition = pointerPosition,
      cacheStore = cacheStore,
    )
  }
}
