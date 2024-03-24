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

package com.patrykandpatrick.vico.views.cartesian

import android.content.Context
import android.graphics.Color
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer.Candle
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.views.common.extension.defaultColors

private fun Candle.Companion.sharpFilledCandle(
    color: Int,
    thicknessDp: Float = Defaults.CANDLE_BODY_WIDTH_DP,
): Candle {
    val filledBody = LineComponent(color, thicknessDp)
    return Candle(body = filledBody)
}

private fun Candle.Companion.sharpHollowCandle(
    color: Int,
    thicknessDp: Float = Defaults.CANDLE_BODY_WIDTH_DP,
    strokeWidthDp: Float = Defaults.HOLLOW_CANDLE_STROKE_WIDTH_DP,
): Candle {
    val hollowBody =
        LineComponent(
            color = Color.TRANSPARENT,
            thicknessDp = thicknessDp,
            strokeWidthDp = strokeWidthDp,
            strokeColor = color,
        )

    return Candle(body = hollowBody)
}

private fun Candle.copyWithColor(color: Int) =
    Candle(
        body = body.copyWithColor(color),
        topWick = topWick.copyWithColor(color),
        bottomWick = bottomWick.copyWithColor(color),
    )

private fun LineComponent.copyWithColor(color: Int) =
    copy(
        color = if (this.color == Color.TRANSPARENT) this.color else color,
        strokeColor = if (this.strokeColor == Color.TRANSPARENT) this.color else color,
    )

internal fun CandlestickCartesianLayer.Candles.Companion.filled(
    context: Context,
    bullish: Candle = Candle.sharpFilledCandle(context.defaultColors.candlestickGreen.toInt()),
    neutral: Candle = bullish.copyWithColor(context.defaultColors.candlestickGray.toInt()),
    bearish: Candle = bullish.copyWithColor(context.defaultColors.candlestickRed.toInt()),
) = CandlestickCartesianLayer.Candles(
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

internal fun CandlestickCartesianLayer.Candles.Companion.hollow(
    context: Context,
    absolutelyBullishRelativelyBullish: Candle =
        Candle.sharpHollowCandle(context.defaultColors.candlestickGreen.toInt()),
    absolutelyBullishRelativelyNeutral: Candle =
        absolutelyBullishRelativelyBullish.copyWithColor(context.defaultColors.candlestickGray.toInt()),
    absolutelyBullishRelativelyBearish: Candle =
        absolutelyBullishRelativelyBullish.copyWithColor(context.defaultColors.candlestickRed.toInt()),
    absolutelyNeutralRelativelyBullish: Candle = absolutelyBullishRelativelyBullish,
    absolutelyNeutralRelativelyNeutral: Candle = absolutelyBullishRelativelyNeutral,
    absolutelyNeutralRelativelyBearish: Candle = absolutelyBullishRelativelyBearish,
    absolutelyBearishRelativelyBullish: Candle =
        Candle.sharpFilledCandle(context.defaultColors.candlestickGreen.toInt()),
    absolutelyBearishRelativelyNeutral: Candle =
        absolutelyBearishRelativelyBullish.copyWithColor(context.defaultColors.candlestickGray.toInt()),
    absolutelyBearishRelativelyBearish: Candle =
        absolutelyBearishRelativelyBullish.copyWithColor(context.defaultColors.candlestickRed.toInt()),
) = CandlestickCartesianLayer.Candles(
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
