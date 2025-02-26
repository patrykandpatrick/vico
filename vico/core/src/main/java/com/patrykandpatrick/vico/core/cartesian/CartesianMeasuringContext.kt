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

package com.patrykandpatrick.vico.core.cartesian

import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.core.common.MeasuringContext
import com.patrykandpatrick.vico.core.common.Point

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
