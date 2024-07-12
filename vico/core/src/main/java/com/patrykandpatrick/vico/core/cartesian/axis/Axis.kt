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

package com.patrykandpatrick.vico.core.cartesian.axis

import android.graphics.RectF
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.ChartInsetter
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.Bounded

/**
 * Defines the minimal set of properties and functions required by other parts of the library to
 * draw an axis.
 */
public interface Axis<Position : AxisPosition> : Bounded, ChartInsetter<CartesianChartModel> {
  /** Defines the position of the axis relative to the [CartesianChart]. */
  public val position: Position

  /** Draws content under the [CartesianLayer]s. */
  public fun drawUnderLayers(context: CartesianDrawContext)

  /** Draws content over the [CartesianLayer]s. */
  public fun drawOverLayers(context: CartesianDrawContext)

  /** The bounds ([RectF]) passed here define the area where the [Axis] shouldn’t draw anything. */
  public fun setRestrictedBounds(vararg bounds: RectF?)

  /** Updates the chart’s [MutableHorizontalDimensions] instance. */
  public fun updateHorizontalDimensions(
    context: CartesianMeasureContext,
    horizontalDimensions: MutableHorizontalDimensions,
  )
}
