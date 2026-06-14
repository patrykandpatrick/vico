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

internal class CandlestickCartesianLayerDrawingModelInterpolator :
  BaseCartesianLayerDrawingModelInterpolator<
    CandlestickCartesianLayerDrawingModel.Entry,
    CandlestickCartesianLayerDrawingModel,
  >() {
  override fun transform(
    old: CandlestickCartesianLayerDrawingModel?,
    new: CandlestickCartesianLayerDrawingModel,
    entries: List<Map<Double, CandlestickCartesianLayerDrawingModel.Entry>>,
    fraction: Float,
  ): CandlestickCartesianLayerDrawingModel =
    CandlestickCartesianLayerDrawingModel(
      entries = entries.first(),
      modelKey = new.modelKey,
      opacity = old?.opacity.orZero.lerp(new.opacity, fraction),
    )

  override fun transformEntry(
    oldModel: CandlestickCartesianLayerDrawingModel?,
    old: CandlestickCartesianLayerDrawingModel.Entry?,
    new: CandlestickCartesianLayerDrawingModel.Entry,
    fraction: Float,
  ): CandlestickCartesianLayerDrawingModel.Entry =
    CandlestickCartesianLayerDrawingModel.Entry(
      bodyBottomY = old?.bodyBottomY.orZero.lerp(new.bodyBottomY, fraction),
      bodyTopY = old?.bodyTopY.orZero.lerp(new.bodyTopY, fraction),
      bottomWickY = old?.bottomWickY.orZero.lerp(new.bottomWickY, fraction),
      topWickY = old?.topWickY.orZero.lerp(new.topWickY, fraction),
    )
}
