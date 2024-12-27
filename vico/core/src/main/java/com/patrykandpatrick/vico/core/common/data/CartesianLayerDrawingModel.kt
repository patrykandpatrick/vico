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

package com.patrykandpatrick.vico.core.common.data

import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer

/** Houses drawing information for a [CartesianLayer]. */
public abstract class CartesianLayerDrawingModel<T : CartesianLayerDrawingModel.Entry>(
  private val entries: List<Map<Double, T>>
) : List<Map<Double, T>> by entries {
  /**
   * Returns an intermediate [CartesianLayerDrawingModel] between this one and [from]. The returned
   * drawing model includes the provided [Entry] list. [fraction] is the balance between [from] and
   * this [CartesianLayerDrawingModel], with 0 corresponding to [from], and 1 corresponding to this
   * [CartesianLayerDrawingModel]. The returned object should be an instance of the
   * [CartesianLayerDrawingModel] subclass to which this function belongs.
   */
  public abstract fun transform(
    entries: List<Map<Double, T>>,
    from: CartesianLayerDrawingModel<T>?,
    fraction: Float,
  ): CartesianLayerDrawingModel<T>

  abstract override fun equals(other: Any?): Boolean

  abstract override fun hashCode(): Int

  /**
   * Houses positional information for a single [CartesianLayer] entity (e.g., a column or a point).
   */
  public interface Entry {
    /**
     * Returns an intermediate [Entry] implementation between this one and [from]. [fraction] is the
     * balance between [from] and this [Entry] implementation, with 0 corresponding to [from], and 1
     * corresponding to this [Entry] implementation. The returned object should be an instance of
     * the [Entry] implementation to which this function belongs.
     */
    public fun transform(from: Entry?, fraction: Float): Entry

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
  }
}
