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

package com.patrykandpatrick.vico.compose.cartesian.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.data.CartesianLayerDrawingModelInterpolator

/** Creates and remembers a [CandlestickCartesianLayer]. */
@Composable
public fun rememberCandlestickCartesianLayer(
  candles: CandlestickCartesianLayer.CandleProvider =
    CandlestickCartesianLayer.CandleProvider.absolute(),
  minCandleBodyHeight: Dp = Defaults.MIN_CANDLE_BODY_HEIGHT_DP.dp,
  candleSpacing: Dp = Defaults.CANDLE_SPACING_DP.dp,
  scaleCandleWicks: Boolean = false,
  axisValueOverrider: AxisValueOverrider = remember { AxisValueOverrider.auto() },
  verticalAxisPosition: Axis.Position.Vertical? = null,
  drawingModelInterpolator:
    CartesianLayerDrawingModelInterpolator<
      CandlestickCartesianLayerDrawingModel.CandleInfo,
      CandlestickCartesianLayerDrawingModel,
    > =
    CartesianLayerDrawingModelInterpolator.default(),
): CandlestickCartesianLayer =
  remember { CandlestickCartesianLayer(candles) }
    .apply {
      this.candles = candles
      minCandleBodyHeightDp = minCandleBodyHeight.value
      candleSpacingDp = candleSpacing.value
      this.scaleCandleWicks = scaleCandleWicks
      this.axisValueOverrider = axisValueOverrider
      this.verticalAxisPosition = verticalAxisPosition
      this.drawingModelInterpolator = drawingModelInterpolator
    }
