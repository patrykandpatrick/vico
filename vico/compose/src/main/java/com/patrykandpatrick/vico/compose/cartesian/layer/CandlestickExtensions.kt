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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.style.getDefaultColors
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer.Candle
import com.patrykandpatrick.vico.core.cartesian.layer.copyAsWick
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent

@Composable
private fun Candle.Companion.sharpFilledCandle(
    color: Color,
    thickness: Dp = Defaults.CANDLE_BODY_WIDTH_DP.dp,
) = rememberCandle(rememberLineComponent(color, thickness))

@Composable
private fun Candle.Companion.sharpHollowCandle(
    color: Color,
    thickness: Dp = Defaults.CANDLE_BODY_WIDTH_DP.dp,
    strokeWidth: Dp = Defaults.HOLLOW_CANDLE_STROKE_WIDTH_DP.dp,
) = rememberCandle(
    rememberLineComponent(
        color = Color.Transparent,
        thickness = thickness,
        strokeWidth = strokeWidth,
        strokeColor = color,
    ),
)

@Composable
private fun Candle.copyWithColor(color: Color) =
    rememberCandle(body.copyWithColor(color), topWick.copyWithColor(color), bottomWick.copyWithColor(color))

private fun LineComponent.copyWithColor(color: Color) =
    copy(
        color = if (this.color == android.graphics.Color.TRANSPARENT) this.color else color.toArgb(),
        strokeColor = if (this.strokeColor == android.graphics.Color.TRANSPARENT) this.color else color.toArgb(),
    )

/** Creates and remembers a [CandlestickCartesianLayer.Candle]. */
@Composable
public fun rememberCandle(
    body: LineComponent,
    topWick: LineComponent = remember(body) { body.copyAsWick() },
    bottomWick: LineComponent = topWick,
): Candle = remember(body, topWick, bottomWick) { Candle(body, topWick, bottomWick) }

/**
 * TODO
 */
@Composable
public fun rememberFilledCandles(
    bullish: Candle = Candle.sharpFilledCandle(Color(getDefaultColors().candlestickGreen)),
    neutral: Candle = bullish.copyWithColor(Color(getDefaultColors().candlestickGray)),
    bearish: Candle = bullish.copyWithColor(Color(getDefaultColors().candlestickRed)),
): CandlestickCartesianLayer.Candles =
    remember(
        bullish,
        neutral,
        bearish,
    ) {
        CandlestickCartesianLayer.Candles(
            absolutelyBullishRelativelyBullish = bullish,
            absolutelyBullishRelativelyNeutral = bullish,
            absolutelyBullishRelativelyBearish = bullish,
            absolutelyNeutralRelativelyBullish = neutral,
            absolutelyNeutralRelativelyNeutral = neutral,
            absolutelyNeutralRelativelyBearish = neutral,
            absolutelyBearishRelativelyBullish = bearish,
            absolutelyBearishRelativelyNeutral = bearish,
            absolutelyBearishRelativelyBearish = bearish,
        )
    }

/**
 * TODO
 */
@Composable
@Stable
public fun rememberHollowCandles(
    absolutelyBullishRelativelyBullish: Candle = Candle.sharpHollowCandle(Color(getDefaultColors().candlestickGreen)),
    absolutelyBullishRelativelyNeutral: Candle =
        absolutelyBullishRelativelyBullish.copyWithColor(Color(getDefaultColors().candlestickGray)),
    absolutelyBullishRelativelyBearish: Candle =
        absolutelyBullishRelativelyBullish.copyWithColor(Color(getDefaultColors().candlestickRed)),
    absolutelyNeutralRelativelyBullish: Candle = absolutelyBullishRelativelyBullish,
    absolutelyNeutralRelativelyNeutral: Candle = absolutelyBullishRelativelyNeutral,
    absolutelyNeutralRelativelyBearish: Candle = absolutelyBullishRelativelyBearish,
    absolutelyBearishRelativelyBullish: Candle = Candle.sharpFilledCandle(Color(getDefaultColors().candlestickGreen)),
    absolutelyBearishRelativelyNeutral: Candle =
        absolutelyBearishRelativelyBullish.copyWithColor(Color(getDefaultColors().candlestickGray)),
    absolutelyBearishRelativelyBearish: Candle =
        absolutelyBearishRelativelyBullish.copyWithColor(Color(getDefaultColors().candlestickRed)),
): CandlestickCartesianLayer.Candles =
    remember(
        absolutelyBullishRelativelyBullish,
        absolutelyBullishRelativelyNeutral,
        absolutelyBullishRelativelyBearish,
        absolutelyNeutralRelativelyBullish,
        absolutelyNeutralRelativelyNeutral,
        absolutelyNeutralRelativelyBearish,
        absolutelyBearishRelativelyBullish,
        absolutelyBearishRelativelyNeutral,
        absolutelyBearishRelativelyBearish,
    ) {
        CandlestickCartesianLayer.Candles(
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
    }
