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
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer.Candle
import com.patrykandpatrick.vico.core.cartesian.layer.absolute
import com.patrykandpatrick.vico.core.cartesian.layer.absoluteRelative
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent

@Composable
private fun Candle.Companion.sharpFilledCandle(
  color: Color,
  thickness: Dp = Defaults.CANDLE_BODY_WIDTH_DP.dp,
) = Candle(rememberLineComponent(color, thickness))

@Composable
private fun Candle.Companion.sharpHollowCandle(
  color: Color,
  thickness: Dp = Defaults.CANDLE_BODY_WIDTH_DP.dp,
  strokeThickness: Dp = Defaults.HOLLOW_CANDLE_STROKE_THICKNESS_DP.dp,
) =
  Candle(
    rememberLineComponent(
      color = Color.Transparent,
      thickness = thickness,
      strokeColor = color,
      strokeThickness = strokeThickness,
    )
  )

private fun Candle.copyWithColor(color: Color) =
  Candle(body.copyWithColor(color), topWick.copyWithColor(color), bottomWick.copyWithColor(color))

private fun LineComponent.copyWithColor(color: Color) =
  copy(
    color = if (this.color == android.graphics.Color.TRANSPARENT) this.color else color.toArgb(),
    strokeColor =
      if (this.strokeColor == android.graphics.Color.TRANSPARENT) this.color else color.toArgb(),
  )

/**
 * An alias for [CandlestickCartesianLayer.CandleProvider.Companion.absolute] with default
 * arguments.
 */
@Composable
@Stable
public fun CandlestickCartesianLayer.CandleProvider.Companion.absolute(
  bullish: Candle = Candle.sharpFilledCandle(vicoTheme.candlestickCartesianLayerColors.bullish),
  neutral: Candle = bullish.copyWithColor(vicoTheme.candlestickCartesianLayerColors.neutral),
  bearish: Candle = bullish.copyWithColor(vicoTheme.candlestickCartesianLayerColors.bearish),
): CandlestickCartesianLayer.CandleProvider =
  CandlestickCartesianLayer.CandleProvider.absolute(bullish, neutral, bearish)

/**
 * An alias for [CandlestickCartesianLayer.CandleProvider.Companion.absoluteRelative] with default
 * arguments.
 */
@Composable
@Stable
public fun CandlestickCartesianLayer.CandleProvider.Companion.absoluteRelative(
  absolutelyBullishRelativelyBullish: Candle =
    Candle.sharpHollowCandle(vicoTheme.candlestickCartesianLayerColors.bullish),
  absolutelyBullishRelativelyNeutral: Candle =
    absolutelyBullishRelativelyBullish.copyWithColor(
      vicoTheme.candlestickCartesianLayerColors.neutral
    ),
  absolutelyBullishRelativelyBearish: Candle =
    absolutelyBullishRelativelyBullish.copyWithColor(
      vicoTheme.candlestickCartesianLayerColors.bearish
    ),
  absolutelyNeutralRelativelyBullish: Candle = absolutelyBullishRelativelyBullish,
  absolutelyNeutralRelativelyNeutral: Candle = absolutelyBullishRelativelyNeutral,
  absolutelyNeutralRelativelyBearish: Candle = absolutelyBullishRelativelyBearish,
  absolutelyBearishRelativelyBullish: Candle =
    Candle.sharpFilledCandle(vicoTheme.candlestickCartesianLayerColors.bullish),
  absolutelyBearishRelativelyNeutral: Candle =
    absolutelyBearishRelativelyBullish.copyWithColor(
      vicoTheme.candlestickCartesianLayerColors.neutral
    ),
  absolutelyBearishRelativelyBearish: Candle =
    absolutelyBearishRelativelyBullish.copyWithColor(
      vicoTheme.candlestickCartesianLayerColors.bearish
    ),
): CandlestickCartesianLayer.CandleProvider =
  CandlestickCartesianLayer.CandleProvider.absoluteRelative(
    absolutelyBullishRelativelyBullish,
    absolutelyBullishRelativelyNeutral,
    absolutelyBullishRelativelyBearish,
    absolutelyNeutralRelativelyBullish,
    absolutelyNeutralRelativelyNeutral,
    absolutelyNeutralRelativelyBearish,
    absolutelyBearishRelativelyBullish,
    absolutelyBearishRelativelyNeutral,
    absolutelyBearishRelativelyBearish,
  )
