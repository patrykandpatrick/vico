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

internal class LineCartesianLayerDrawingModelInterpolator :
  BaseCartesianLayerDrawingModelInterpolator<
    LineCartesianLayerDrawingModel.Entry,
    LineCartesianLayerDrawingModel,
  >() {
  override fun transform(
    old: LineCartesianLayerDrawingModel?,
    new: LineCartesianLayerDrawingModel,
    entries: List<Map<Double, LineCartesianLayerDrawingModel.Entry>>,
    fraction: Float,
  ): LineCartesianLayerDrawingModel =
    LineCartesianLayerDrawingModel(
      entries = entries,
      seriesKeys = new.seriesKeys,
      opacity = old?.opacity.orZero.lerp(new.opacity, fraction),
    )

  override fun transformEntry(
    oldModel: LineCartesianLayerDrawingModel?,
    old: LineCartesianLayerDrawingModel.Entry?,
    new: LineCartesianLayerDrawingModel.Entry,
    fraction: Float,
  ): LineCartesianLayerDrawingModel.Entry =
    LineCartesianLayerDrawingModel.Entry(old?.y.orZero.lerp(new.y, fraction))
}
