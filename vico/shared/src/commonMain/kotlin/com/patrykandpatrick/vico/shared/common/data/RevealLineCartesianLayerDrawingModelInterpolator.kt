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

package com.patrykandpatrick.vico.shared.common.data

import com.patrykandpatrick.vico.shared.cartesian.data.LineCartesianLayerDrawingModel

internal class RevealLineCartesianLayerDrawingModelInterpolator :
  CartesianLayerDrawingModelInterpolator<
    LineCartesianLayerDrawingModel.Entry,
    LineCartesianLayerDrawingModel,
  > {
  private val delegate =
    DefaultCartesianLayerDrawingModelInterpolator<
      LineCartesianLayerDrawingModel.Entry,
      LineCartesianLayerDrawingModel,
    >()

  override fun setModels(
    old: LineCartesianLayerDrawingModel?,
    new: LineCartesianLayerDrawingModel?,
  ) {
    delegate.setModels(
      old = if (old == null && new != null) new.withRevealFraction(0f) else old,
      new = new,
    )
  }

  override suspend fun transform(fraction: Float): LineCartesianLayerDrawingModel? =
    delegate.transform(fraction)

  override fun equals(other: Any?): Boolean =
    this === other || other is RevealLineCartesianLayerDrawingModelInterpolator

  override fun hashCode(): Int = this::class.hashCode()
}
