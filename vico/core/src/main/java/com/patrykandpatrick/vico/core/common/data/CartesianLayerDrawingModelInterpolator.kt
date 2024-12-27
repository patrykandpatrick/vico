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

/** Interpolates two [CartesianLayerDrawingModel]s. */
public interface CartesianLayerDrawingModelInterpolator<
  T : CartesianLayerDrawingModel.Entry,
  R : CartesianLayerDrawingModel<T>,
> {
  /** Sets the initial and target [CartesianLayerDrawingModel]s. */
  public fun setModels(old: R?, new: R?)

  /**
   * Interpolates the two [CartesianLayerDrawingModel]s. [fraction] is the balance between the
   * initial and target [CartesianLayerDrawingModel]s, with 0 corresponding to the initial
   * [CartesianLayerDrawingModel], and 1 corresponding to the target [CartesianLayerDrawingModel].
   */
  public suspend fun transform(fraction: Float): R?

  /** Houses a [CartesianLayerDrawingModelInterpolator] factory function. */
  public companion object {
    /**
     * Creates an instance of the default [CartesianLayerDrawingModelInterpolator] implementation.
     */
    public fun <T : CartesianLayerDrawingModel.Entry, R : CartesianLayerDrawingModel<T>> default():
      CartesianLayerDrawingModelInterpolator<T, R> = DefaultCartesianLayerDrawingModelInterpolator()
  }
}
