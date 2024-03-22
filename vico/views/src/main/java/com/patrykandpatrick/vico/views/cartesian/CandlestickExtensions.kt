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
import com.patrykandpatrick.vico.core.common.DefaultColors
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.views.common.extension.defaultColors

/**
 * TODO
 */
public fun Candle.Companion.sharpFilledCandle(
    color: Int,
    thicknessDp: Float = Defaults.REAL_BODY_WIDTH_DP,
): Candle {
    val filledBody = LineComponent(color, thicknessDp)
    return Candle(realBody = filledBody)
}

/**
 * TODO
 */
public fun Candle.Companion.sharpHollowCandle(
    color: Int,
    thicknessDp: Float = Defaults.REAL_BODY_WIDTH_DP,
    strokeWidthDp: Float = Defaults.HOLLOW_CANDLE_STROKE_WIDTH_DP,
): Candle {
    val hollowBody =
        LineComponent(
            color = Color.TRANSPARENT,
            thicknessDp = thicknessDp,
            strokeWidthDp = strokeWidthDp,
            strokeColor = color,
        )

    return Candle(realBody = hollowBody)
}

/**
 * TODO
 */
public fun Candle.copyWithColor(color: Int): Candle =
    Candle(
        realBody = realBody.copyWithColor(color),
        upperWick = upperWick.copyWithColor(color),
        lowerWick = lowerWick.copyWithColor(color),
    )

/**
 * TODO
 */
public fun LineComponent.copyWithColor(color: Int): LineComponent =
    copy(
        color = if (this.color == Color.TRANSPARENT) this.color else color,
        strokeColor = if (this.strokeColor == Color.TRANSPARENT) this.color else color,
    )

public class CandlestickStandardConfigBuilder internal constructor(private val context: Context) :
    CandlestickStandardConfigBuilderScope {
        override var absolutelyIncreasing: Candle? = null
        override var absolutelyZero: Candle? = null
        override var absolutelyDecreasing: Candle? = null

        internal fun build(): CandlestickCartesianLayer.Config {
            val defaultColors = context.defaultColors
            val absolutelyIncreasing =
                absolutelyIncreasing ?: Candle.sharpFilledCandle(color = defaultColors.candlestickGreen.toInt())
            val absolutelyZero =
                absolutelyZero ?: absolutelyIncreasing.copyWithColor(defaultColors.candlestickGray.toInt())
            val absolutelyDecreasing =
                absolutelyDecreasing ?: absolutelyIncreasing.copyWithColor(defaultColors.candlestickRed.toInt())

            return CandlestickCartesianLayer.Config(
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
    }

public interface CandlestickStandardConfigBuilderScope {
    public var absolutelyIncreasing: Candle?
    public var absolutelyZero: Candle?
    public var absolutelyDecreasing: Candle?
}

public class CandlestickHollowConfigBuilder internal constructor(private val context: Context) :
    CandlestickHollowConfigBuilderScope {
        public override var absolutelyIncreasingRelativelyIncreasing: Candle? = null
        public override var absolutelyIncreasingRelativelyZero: Candle? = null
        public override var absolutelyIncreasingRelativelyDecreasing: Candle? = null
        public override var absolutelyZeroRelativelyIncreasing: Candle? = null
        public override var absolutelyZeroRelativelyZero: Candle? = null
        public override var absolutelyZeroRelativelyDecreasing: Candle? = null
        public override var absolutelyDecreasingRelativelyIncreasing: Candle? = null
        public override var absolutelyDecreasingRelativelyZero: Candle? = null
        public override var absolutelyDecreasingRelativelyDecreasing: Candle? = null

        public fun build(): CandlestickCartesianLayer.Config {
            val defaultColors = context.defaultColors
            val absolutelyIncreasingRelativelyIncreasing: Candle =
                absolutelyIncreasingRelativelyIncreasing ?: getAbsolutelyIncreasingRelativelyIncreasing(defaultColors)
            val absolutelyIncreasingRelativelyZero: Candle =
                absolutelyDecreasingRelativelyZero
                    ?: absolutelyIncreasingRelativelyIncreasing.copyWithColor(defaultColors.candlestickGray.toInt())
            val absolutelyIncreasingRelativelyDecreasing: Candle =
                absolutelyIncreasingRelativelyDecreasing
                    ?: absolutelyIncreasingRelativelyIncreasing.copyWithColor(defaultColors.candlestickRed.toInt())
            val absolutelyZeroRelativelyIncreasing: Candle =
                absolutelyZeroRelativelyIncreasing ?: absolutelyIncreasingRelativelyIncreasing
            val absolutelyZeroRelativelyZero: Candle =
                absolutelyZeroRelativelyZero ?: absolutelyIncreasingRelativelyZero
            val absolutelyZeroRelativelyDecreasing: Candle =
                absolutelyZeroRelativelyDecreasing ?: absolutelyIncreasingRelativelyDecreasing
            val absolutelyDecreasingRelativelyIncreasing: Candle =
                absolutelyDecreasingRelativelyIncreasing ?: getAbsolutelyDecreasingRelativelyIncreasing(defaultColors)
            val absolutelyDecreasingRelativelyZero: Candle =
                absolutelyDecreasingRelativelyZero
                    ?: absolutelyDecreasingRelativelyIncreasing.copyWithColor(defaultColors.candlestickGray.toInt())
            val absolutelyDecreasingRelativelyDecreasing: Candle =
                absolutelyDecreasingRelativelyDecreasing
                    ?: absolutelyDecreasingRelativelyIncreasing.copyWithColor(defaultColors.candlestickRed.toInt())

            return CandlestickCartesianLayer.Config(
                absolutelyIncreasingRelativelyIncreasing = absolutelyIncreasingRelativelyIncreasing,
                absolutelyIncreasingRelativelyZero = absolutelyIncreasingRelativelyZero,
                absolutelyIncreasingRelativelyDecreasing = absolutelyIncreasingRelativelyDecreasing,
                absolutelyZeroRelativelyIncreasing = absolutelyZeroRelativelyIncreasing,
                absolutelyZeroRelativelyZero = absolutelyZeroRelativelyZero,
                absolutelyZeroRelativelyDecreasing = absolutelyZeroRelativelyDecreasing,
                absolutelyDecreasingRelativelyIncreasing = absolutelyDecreasingRelativelyIncreasing,
                absolutelyDecreasingRelativelyZero = absolutelyDecreasingRelativelyZero,
                absolutelyDecreasingRelativelyDecreasing = absolutelyDecreasingRelativelyDecreasing,
            )
        }
    }

public interface CandlestickHollowConfigBuilderScope {
    public var absolutelyIncreasingRelativelyIncreasing: Candle?
    public var absolutelyIncreasingRelativelyZero: Candle?
    public var absolutelyIncreasingRelativelyDecreasing: Candle?
    public var absolutelyZeroRelativelyIncreasing: Candle?
    public var absolutelyZeroRelativelyZero: Candle?
    public var absolutelyZeroRelativelyDecreasing: Candle?
    public var absolutelyDecreasingRelativelyIncreasing: Candle?
    public var absolutelyDecreasingRelativelyZero: Candle?
    public var absolutelyDecreasingRelativelyDecreasing: Candle?
}

public fun CandlestickCartesianLayer.Config.Companion.standardBuilder(
    context: Context,
    block: CandlestickStandardConfigBuilderScope.() -> Unit = {},
): CandlestickCartesianLayer.Config = CandlestickStandardConfigBuilder(context).apply(block).build()

public fun CandlestickCartesianLayer.Config.Companion.hollowBuilder(
    context: Context,
    block: CandlestickHollowConfigBuilderScope.() -> Unit = {},
): CandlestickCartesianLayer.Config = CandlestickHollowConfigBuilder(context).apply(block).build()

private fun getAbsolutelyIncreasingRelativelyIncreasing(colors: DefaultColors): Candle =
    Candle.sharpHollowCandle(color = colors.candlestickGreen.toInt())

private fun getAbsolutelyDecreasingRelativelyIncreasing(colors: DefaultColors): Candle =
    Candle.sharpFilledCandle(color = colors.candlestickGreen.toInt())
