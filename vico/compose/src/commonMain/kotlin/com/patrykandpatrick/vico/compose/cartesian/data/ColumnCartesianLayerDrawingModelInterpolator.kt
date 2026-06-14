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

import com.patrykandpatrick.vico.compose.common.lerp
import com.patrykandpatrick.vico.compose.common.orZero

internal class ColumnCartesianLayerDrawingModelInterpolator :
  BaseCartesianLayerDrawingModelInterpolator<
    ColumnCartesianLayerDrawingModel.Entry,
    ColumnCartesianLayerDrawingModel,
  >() {
  override fun transform(
    old: ColumnCartesianLayerDrawingModel?,
    new: ColumnCartesianLayerDrawingModel,
    entries: List<Map<Double, ColumnCartesianLayerDrawingModel.Entry>>,
    fraction: Float,
  ): ColumnCartesianLayerDrawingModel =
    ColumnCartesianLayerDrawingModel(
      entries = entries,
      seriesKeys = new.seriesKeys,
      opacity = old?.opacity.orZero.lerp(new.opacity, fraction),
    )

  override fun transformEntry(
    oldModel: ColumnCartesianLayerDrawingModel?,
    old: ColumnCartesianLayerDrawingModel.Entry?,
    new: ColumnCartesianLayerDrawingModel.Entry,
    fraction: Float,
  ): ColumnCartesianLayerDrawingModel.Entry =
    ColumnCartesianLayerDrawingModel.Entry(old?.height.orZero.lerp(new.height, fraction))
}
