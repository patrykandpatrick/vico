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
public abstract class DrawingModel<T : DrawingModel.DrawingInfo>(
  private val drawingInfo: List<Map<Double, T>>
) : List<Map<Double, T>> by drawingInfo {
  /**
   * Returns an intermediate [DrawingModel] between this one and [from]. The returned drawing model
   * includes the provided [DrawingInfo] list. [fraction] is the balance between [from] and this
   * [DrawingModel], with 0 corresponding to [from], and 1 corresponding to this [DrawingModel]. The
   * returned object should be an instance of the [DrawingModel] subclass to which this function
   * belongs.
   */
  public abstract fun transform(
    drawingInfo: List<Map<Double, T>>,
    from: DrawingModel<T>?,
    fraction: Float,
  ): DrawingModel<T>

  abstract override fun equals(other: Any?): Boolean

  abstract override fun hashCode(): Int

  /**
   * Houses positional information for a single [CartesianLayer] entity (e.g., a column or a point).
   */
  public interface DrawingInfo {
    /**
     * Returns an intermediate [DrawingInfo] implementation between this one and [from]. [fraction]
     * is the balance between [from] and this [DrawingInfo] implementation, with 0 corresponding to
     * [from], and 1 corresponding to this [DrawingInfo] implementation. The returned object should
     * be an instance of the [DrawingInfo] implementation to which this function belongs.
     */
    public fun transform(from: DrawingInfo?, fraction: Float): DrawingInfo

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
  }
}
