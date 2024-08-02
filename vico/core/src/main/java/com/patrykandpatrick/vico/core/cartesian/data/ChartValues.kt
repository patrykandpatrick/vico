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

package com.patrykandpatrick.vico.core.cartesian.data

import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.Axis

/** Houses a [CartesianChart]’s [CartesianChartModel] and _x_ and _y_ ranges. */
public interface ChartValues {
  /** The minimum _x_ value. */
  public val minX: Double

  /** The maximum _x_ value. */
  public val maxX: Double

  /** The difference between the _x_ values of neighboring major entries. */
  public val xStep: Double

  /** The [CartesianChart]’s [CartesianChartModel]. */
  public val model: CartesianChartModel

  /**
   * Returns the [YRange] associated with the given [Axis.Position.Vertical] subclass. If
   * [axisPosition] is `null` or has no associated [YRange], the global [YRange] is returned.
   */
  public fun getYRange(axisPosition: Axis.Position.Vertical?): YRange

  /** The difference between [maxX] and [minX]. */
  public val xLength: Double
    get() = maxX - minX

  /** Holds information on a _y_ range. */
  public interface YRange {
    /** The minimum _y_ value. */
    public val minY: Double

    /** The maximum _y_ value. */
    public val maxY: Double

    /** The difference between [maxY] and [minY]. */
    public val length: Double
  }

  /** An empty [ChartValues] implementation. */
  public object Empty : ChartValues {
    private const val ERROR_MESSAGE = "`ChartValues.Empty` shouldn’t be used."

    override val minX: Double
      get() {
        error(ERROR_MESSAGE)
      }

    override val maxX: Double
      get() {
        error(ERROR_MESSAGE)
      }

    override val xStep: Double
      get() {
        error(ERROR_MESSAGE)
      }

    override val model: CartesianChartModel = CartesianChartModel.empty

    override fun getYRange(axisPosition: Axis.Position.Vertical?): YRange {
      error(ERROR_MESSAGE)
    }
  }
}
