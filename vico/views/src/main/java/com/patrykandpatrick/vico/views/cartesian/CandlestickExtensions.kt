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

private fun getAbsolutelyBullishRelativelyBullish(colors: DefaultColors) =
    Candle.sharpHollowCandle(colors.candlestickGreen.toInt())

private fun getAbsolutelyBearishRelativelyBullish(colors: DefaultColors) =
    Candle.sharpFilledCandle(colors.candlestickGreen.toInt())

internal class CandlestickStandardConfigBuilder(private val context: Context) : CandlestickStandardConfigBuilderScope {
    override var absolutelyBullish: Candle? = null
    override var absolutelyNeutral: Candle? = null
    override var absolutelyBearish: Candle? = null

    internal fun build(): CandlestickCartesianLayer.Config {
        val defaultColors = context.defaultColors
        val absolutelyBullish =
            absolutelyBullish ?: Candle.sharpFilledCandle(color = defaultColors.candlestickGreen.toInt())
        val absolutelyNeutral =
            absolutelyNeutral ?: absolutelyBullish.copyWithColor(defaultColors.candlestickGray.toInt())
        val absolutelyBearish =
            absolutelyBearish ?: absolutelyBullish.copyWithColor(defaultColors.candlestickRed.toInt())

        return CandlestickCartesianLayer.Config(
            absolutelyBullishRelativelyBullish = absolutelyBullish,
            absolutelyBullishRelativelyNeutral = absolutelyBullish,
            absolutelyBullishRelativelyBearish = absolutelyBullish,
            absolutelyNeutralRelativelyBullish = absolutelyNeutral,
            absolutelyNeutralRelativelyNeutral = absolutelyNeutral,
            absolutelyNeutralRelativelyBearish = absolutelyNeutral,
            absolutelyBearishRelativelyBullish = absolutelyBearish,
            absolutelyBearishRelativelyNeutral = absolutelyBearish,
            absolutelyBearishRelativelyBearish = absolutelyBearish,
        )
    }
}

internal interface CandlestickStandardConfigBuilderScope {
    var absolutelyBullish: Candle?
    var absolutelyNeutral: Candle?
    var absolutelyBearish: Candle?
}

internal class CandlestickHollowConfigBuilder(private val context: Context) : CandlestickHollowConfigBuilderScope {
    override var absolutelyBullishRelativelyBullish: Candle? = null
    override var absolutelyBullishRelativelyNeutral: Candle? = null
    override var absolutelyBullishRelativelyBearish: Candle? = null
    override var absolutelyNeutralRelativelyBullish: Candle? = null
    override var absolutelyNeutralRelativelyNeutral: Candle? = null
    override var absolutelyNeutralRelativelyBearish: Candle? = null
    override var absolutelyBearishRelativelyBullish: Candle? = null
    override var absolutelyBearishRelativelyNeutral: Candle? = null
    override var absolutelyBearishRelativelyBearish: Candle? = null

    fun build(): CandlestickCartesianLayer.Config {
        val defaultColors = context.defaultColors
        val absolutelyBullishRelativelyBullish: Candle =
            absolutelyBullishRelativelyBullish ?: getAbsolutelyBullishRelativelyBullish(defaultColors)
        val absolutelyBullishRelativelyNeutral: Candle =
            absolutelyBearishRelativelyNeutral
                ?: absolutelyBullishRelativelyBullish.copyWithColor(defaultColors.candlestickGray.toInt())
        val absolutelyBullishRelativelyBearish: Candle =
            absolutelyBullishRelativelyBearish
                ?: absolutelyBullishRelativelyBullish.copyWithColor(defaultColors.candlestickRed.toInt())
        val absolutelyNeutralRelativelyBullish: Candle =
            absolutelyNeutralRelativelyBullish ?: absolutelyBullishRelativelyBullish
        val absolutelyNeutralRelativelyNeutral: Candle =
            absolutelyNeutralRelativelyNeutral ?: absolutelyBullishRelativelyNeutral
        val absolutelyNeutralRelativelyBearish: Candle =
            absolutelyNeutralRelativelyBearish ?: absolutelyBullishRelativelyBearish
        val absolutelyBearishRelativelyBullish: Candle =
            absolutelyBearishRelativelyBullish ?: getAbsolutelyBearishRelativelyBullish(defaultColors)
        val absolutelyBearishRelativelyNeutral: Candle =
            absolutelyBearishRelativelyNeutral
                ?: absolutelyBearishRelativelyBullish.copyWithColor(defaultColors.candlestickGray.toInt())
        val absolutelyBearishRelativelyBearish: Candle =
            absolutelyBearishRelativelyBearish
                ?: absolutelyBearishRelativelyBullish.copyWithColor(defaultColors.candlestickRed.toInt())

        return CandlestickCartesianLayer.Config(
            absolutelyBullishRelativelyBullish = absolutelyBullishRelativelyBullish,
            absolutelyBullishRelativelyNeutral = absolutelyBullishRelativelyNeutral,
            absolutelyBullishRelativelyBearish = absolutelyBullishRelativelyBearish,
            absolutelyNeutralRelativelyBullish = absolutelyNeutralRelativelyBullish,
            absolutelyNeutralRelativelyNeutral = absolutelyNeutralRelativelyNeutral,
            absolutelyNeutralRelativelyBearish = absolutelyNeutralRelativelyBearish,
            absolutelyBearishRelativelyBullish = absolutelyBearishRelativelyBullish,
            absolutelyBearishRelativelyNeutral = absolutelyBearishRelativelyNeutral,
            absolutelyBearishRelativelyBearish = absolutelyBearishRelativelyBearish,
        )
    }
}

internal interface CandlestickHollowConfigBuilderScope {
    var absolutelyBullishRelativelyBullish: Candle?
    var absolutelyBullishRelativelyNeutral: Candle?
    var absolutelyBullishRelativelyBearish: Candle?
    var absolutelyNeutralRelativelyBullish: Candle?
    var absolutelyNeutralRelativelyNeutral: Candle?
    var absolutelyNeutralRelativelyBearish: Candle?
    var absolutelyBearishRelativelyBullish: Candle?
    var absolutelyBearishRelativelyNeutral: Candle?
    var absolutelyBearishRelativelyBearish: Candle?
}

internal fun CandlestickCartesianLayer.Config.Companion.standardBuilder(
    context: Context,
    block: CandlestickStandardConfigBuilderScope.() -> Unit = {},
) = CandlestickStandardConfigBuilder(context).apply(block).build()

internal fun CandlestickCartesianLayer.Config.Companion.hollowBuilder(
    context: Context,
    block: CandlestickHollowConfigBuilderScope.() -> Unit = {},
) = CandlestickHollowConfigBuilder(context).apply(block).build()
