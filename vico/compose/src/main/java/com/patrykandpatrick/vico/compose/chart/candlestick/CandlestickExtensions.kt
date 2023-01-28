/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.chart.candlestick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.style.getDefaultColors
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.candlestick.CandlestickChart
import com.patrykandpatrick.vico.core.chart.candlestick.CandlestickChart.Candle
import com.patrykandpatrick.vico.core.component.shape.LineComponent

/**
 * TODO
 */
@Composable
public fun Candle.Companion.sharpFilledCandle(
    color: Color,
    thickness: Dp = DefaultDimens.REAL_BODY_WIDTH_DP.dp,
): Candle {

    val filledBody = lineComponent(color, thickness)

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

    val hollowBody = lineComponent(
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
public fun Candle.copyWithColor(color: Color): Candle = remember(color) {
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
        strokeColor = if (this.color == android.graphics.Color.TRANSPARENT) this.color else color.toArgb(),
    )

/**
 * TODO
 */
@Composable
public fun CandlestickChart.Config.Companion.standard(
    filledGreenCandle: Candle = Candle.sharpFilledCandle(color = Color(getDefaultColors().candlestickGreen)),
    crossGrayCandle: Candle = filledGreenCandle.copyWithColor(color = Color(getDefaultColors().candlestickGray)),
    filledRedCandle: Candle = filledGreenCandle.copyWithColor(color = Color(getDefaultColors().candlestickRed)),
): CandlestickChart.Config = remember(
    filledGreenCandle,
    crossGrayCandle,
    filledRedCandle,
) {
    CandlestickChart.Config(
        filledGreenCandle = filledGreenCandle,
        filledGrayCandle = crossGrayCandle,
        filledRedCandle = filledRedCandle,
        crossGreenCandle = filledGreenCandle,
        crossGrayCandle = crossGrayCandle,
        crossRedCandle = filledRedCandle,
        hollowGreenCandle = filledGreenCandle,
        hollowGrayCandle = crossGrayCandle,
        hollowRedCandle = filledRedCandle,
    )
}

/**
 * TODO
 */
@Composable
public fun CandlestickChart.Config.Companion.hollow(
    filledGreenCandle: Candle = Candle.sharpFilledCandle(color = Color(getDefaultColors().candlestickGreen)),
    filledGrayCandle: Candle = filledGreenCandle.copyWithColor(color = Color(getDefaultColors().candlestickGray)),
    filledRedCandle: Candle = filledGreenCandle.copyWithColor(color = Color(getDefaultColors().candlestickRed)),
    crossGreenCandle: Candle = filledGreenCandle,
    crossGrayCandle: Candle = filledGrayCandle,
    crossRedCandle: Candle = filledRedCandle,
    hollowGreenCandle: Candle = Candle.sharpHollowCandle(color = Color(getDefaultColors().candlestickGreen)),
    hollowGrayCandle: Candle = hollowGreenCandle.copyWithColor(color = Color(getDefaultColors().candlestickGray)),
    hollowRedCandle: Candle = hollowGreenCandle.copyWithColor(color = Color(getDefaultColors().candlestickRed)),
): CandlestickChart.Config = CandlestickChart.Config(
    filledGreenCandle = filledGreenCandle,
    filledGrayCandle = filledGrayCandle,
    filledRedCandle = filledRedCandle,
    crossGreenCandle = crossGreenCandle,
    crossGrayCandle = crossGrayCandle,
    crossRedCandle = crossRedCandle,
    hollowGreenCandle = hollowGreenCandle,
    hollowGrayCandle = hollowGrayCandle,
    hollowRedCandle = hollowRedCandle,
)
