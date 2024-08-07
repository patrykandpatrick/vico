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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.ChartInsetter
import com.patrykandpatrick.vico.core.cartesian.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.common.Bounded
import com.patrykandpatrick.vico.core.common.MeasuringContext

/** Draws an axis. */
public interface Axis<P : Axis.Position> : Bounded, ChartInsetter<CartesianChartModel> {
  /** The position of the [Axis]. */
  public val position: P

  /** Draws content under the [CartesianLayer]s. */
  public fun drawUnderLayers(context: CartesianDrawingContext)

  /** Draws content over the [CartesianLayer]s. */
  public fun drawOverLayers(context: CartesianDrawingContext)

  /** The bounds ([RectF]) passed here define the area where the [Axis] shouldn’t draw anything. */
  public fun setRestrictedBounds(vararg bounds: RectF?)

  /** Updates the chart’s [MutableHorizontalDimensions] instance. */
  public fun updateHorizontalDimensions(
    context: CartesianMeasuringContext,
    horizontalDimensions: MutableHorizontalDimensions,
  )

  /** Specifies the position of an [Axis]. */
  public sealed interface Position {
    /** Specifies the position of a horizontal [Axis]. */
    public sealed interface Horizontal : Position {
      /** Denotes that a horizontal [Axis] is at the top of its [CartesianChart]. */
      public data object Top : Horizontal

      /** Denotes that a horizontal [Axis] is at the bottom of its [CartesianChart]. */
      public data object Bottom : Horizontal
    }

    /** Specifies the position of a vertical [Axis]. */
    public sealed interface Vertical : Position {
      /** Denotes that a vertical [Axis] is at the start of its [CartesianChart]. */
      public data object Start : Vertical

      /** Denotes that a vertical [Axis] is at the end of its [CartesianChart]. */
      public data object End : Vertical
    }
  }
}

internal fun Axis.Position.Vertical.isLeft(context: MeasuringContext) =
  when (this) {
    Axis.Position.Vertical.Start -> context.isLtr
    Axis.Position.Vertical.End -> !context.isLtr
  }
