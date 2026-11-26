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

package com.patrykandpatrick.vico.compose.cartesian.data

import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayer

/**
 * Houses drawing information for a [CartesianLayer]: one map from _x_-values to [Entry] instances
 * per series, with the series identified by [seriesKeys].
 */
public abstract class CartesianLayerDrawingModel<T : CartesianLayerDrawingModel.Entry>(
  private val entries: List<Map<Double, T>>,
  public val seriesKeys: List<Any>,
) : List<Map<Double, T>> by entries {
  init {
    require(entries.size == seriesKeys.size) {
      "`entries` and `seriesKeys` must have the same size."
    }
    require(seriesKeys.toSet().size == seriesKeys.size) { "Series keys must be unique." }
  }

  abstract override fun equals(other: Any?): Boolean

  abstract override fun hashCode(): Int

  /**
   * Houses positional information for a single [CartesianLayer] entity (e.g., a column or a point).
   */
  public interface Entry {
    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
  }
}
