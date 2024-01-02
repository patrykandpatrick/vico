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

package com.patrykandpatrick.vico.compose.chart.layer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.style.getDefaultColors
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.chart.layer.CandlestickCartesianLayer.Candle
import com.patrykandpatrick.vico.core.component.shape.LineComponent

/**
 * TODO
 */
@Composable
public fun Candle.Companion.sharpFilledCandle(
    color: Color,
    thickness: Dp = DefaultDimens.REAL_BODY_WIDTH_DP.dp,
): Candle {
    val filledBody = rememberLineComponent(color, thickness)

    return remember(filledBody) {
        Candle(realBody = filledBody)
    }
}

/**
 * TODO
 */
@Composable
public fun Candle.Companion.sharpHollowCandle(
    color: Color,
    thickness: Dp = DefaultDimens.REAL_BODY_WIDTH_DP.dp,
    strokeWidth: Dp = DefaultDimens.HOLLOW_CANDLE_STROKE_WIDTH_DP.dp,
): Candle {
    val hollowBody =
        rememberLineComponent(
            color = Color.Transparent,
            thickness = thickness,
            strokeWidth = strokeWidth,
            strokeColor = color,
        )

    return remember(hollowBody) {
        Candle(realBody = hollowBody)
    }
}

/**
 * TODO
 */
@Composable
public fun Candle.copyWithColor(color: Color): Candle =
    remember(color) {
        Candle(
            realBody = realBody.copyWithColor(color),
            upperWick = upperWick.copyWithColor(color),
            lowerWick = lowerWick.copyWithColor(color),
        )
    }

/**
 * TODO
 */
public fun LineComponent.copyWithColor(color: Color): LineComponent =
    copy(
        color = if (this.color == android.graphics.Color.TRANSPARENT) this.color else color.toArgb(),
        strokeColor = if (this.strokeColor == android.graphics.Color.TRANSPARENT) this.color else color.toArgb(),
    )

/**
 * TODO
 */
@Composable
public fun CandlestickCartesianLayer.Config.Companion.rememberStandard(
    absolutelyIncreasing: Candle = Candle.sharpFilledCandle(color = Color(getDefaultColors().candlestickGreen)),
    absolutelyZero: Candle = absolutelyIncreasing.copyWithColor(color = Color(getDefaultColors().candlestickGray)),
    absolutelyDecreasing: Candle = absolutelyIncreasing.copyWithColor(color = Color(getDefaultColors().candlestickRed)),
): CandlestickCartesianLayer.Config =
    remember(
        absolutelyIncreasing,
        absolutelyZero,
        absolutelyDecreasing,
    ) {
        CandlestickCartesianLayer.Config(
            absolutelyIncreasingRelativelyIncreasing = absolutelyIncreasing,
            absolutelyIncreasingRelativelyZero = absolutelyIncreasing,
            absolutelyIncreasingRelativelyDecreasing = absolutelyIncreasing,
            absolutelyZeroRelativelyIncreasing = absolutelyZero,
            absolutelyZeroRelativelyZero = absolutelyZero,
            absolutelyZeroRelativelyDecreasing = absolutelyZero,
            absolutelyDecreasingRelativelyIncreasing = absolutelyDecreasing,
            absolutelyDecreasingRelativelyZero = absolutelyDecreasing,
            absolutelyDecreasingRelativelyDecreasing = absolutelyDecreasing,
        )
    }

/**
 * TODO
 */
@Composable
@Stable
public fun CandlestickCartesianLayer.Config.Companion.rememberHollow(
    absolutelyIncreasingRelativelyIncreasing: Candle = getAbsolutelyIncreasingRelativelyIncreasing(),
    absolutelyIncreasingRelativelyZero: Candle =
        absolutelyIncreasingRelativelyIncreasing.copyWithColor(
            color = Color(getDefaultColors().candlestickGray),
        ),
    absolutelyIncreasingRelativelyDecreasing: Candle =
        absolutelyIncreasingRelativelyIncreasing.copyWithColor(
            color = Color(getDefaultColors().candlestickRed),
        ),
    absolutelyZeroRelativelyIncreasing: Candle = absolutelyIncreasingRelativelyIncreasing,
    absolutelyZeroRelativelyZero: Candle = absolutelyIncreasingRelativelyZero,
    absolutelyZeroRelativelyDecreasing: Candle = absolutelyIncreasingRelativelyDecreasing,
    absolutelyDecreasingRelativelyIncreasing: Candle = getAbsolutelyDecreasingRelativelyIncreasing(),
    absolutelyDecreasingRelativelyZero: Candle =
        absolutelyDecreasingRelativelyIncreasing.copyWithColor(
            color = Color(getDefaultColors().candlestickGray),
        ),
    absolutelyDecreasingRelativelyDecreasing: Candle =
        absolutelyDecreasingRelativelyIncreasing.copyWithColor(
            color = Color(getDefaultColors().candlestickRed),
        ),
): CandlestickCartesianLayer.Config =
    remember(
        absolutelyIncreasingRelativelyIncreasing,
        absolutelyIncreasingRelativelyZero,
        absolutelyIncreasingRelativelyDecreasing,
        absolutelyZeroRelativelyIncreasing,
        absolutelyZeroRelativelyZero,
        absolutelyZeroRelativelyDecreasing,
        absolutelyDecreasingRelativelyIncreasing,
        absolutelyDecreasingRelativelyZero,
        absolutelyDecreasingRelativelyDecreasing,
    ) {
        CandlestickCartesianLayer.Config(
            absolutelyIncreasingRelativelyIncreasing = absolutelyDecreasingRelativelyIncreasing,
            absolutelyIncreasingRelativelyZero = absolutelyDecreasingRelativelyZero,
            absolutelyIncreasingRelativelyDecreasing = absolutelyDecreasingRelativelyDecreasing,
            absolutelyZeroRelativelyIncreasing = absolutelyZeroRelativelyIncreasing,
            absolutelyZeroRelativelyZero = absolutelyZeroRelativelyZero,
            absolutelyZeroRelativelyDecreasing = absolutelyZeroRelativelyDecreasing,
            absolutelyDecreasingRelativelyIncreasing = absolutelyIncreasingRelativelyIncreasing,
            absolutelyDecreasingRelativelyZero = absolutelyIncreasingRelativelyZero,
            absolutelyDecreasingRelativelyDecreasing = absolutelyIncreasingRelativelyDecreasing,
        )
    }

@Composable
private fun getAbsolutelyIncreasingRelativelyIncreasing(): Candle =
    Candle.sharpHollowCandle(color = Color(getDefaultColors().candlestickGreen))

@Composable
private fun getAbsolutelyDecreasingRelativelyIncreasing(): Candle =
    Candle.sharpFilledCandle(color = Color(getDefaultColors().candlestickGreen))
