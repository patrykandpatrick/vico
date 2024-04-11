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

package com.patrykandpatrick.vico.views.common.theme

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.absolute
import com.patrykandpatrick.vico.core.cartesian.layer.absoluteRelative
import com.patrykandpatrick.vico.core.cartesian.layer.asWick
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.getRepeating
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.cartesian.copyWithColor
import com.patrykandpatrick.vico.views.cartesian.sharpFilledCandle
import com.patrykandpatrick.vico.views.cartesian.sharpHollowCandle
import com.patrykandpatrick.vico.views.common.defaultColors

internal fun TypedArray.getColumnCartesianLayer(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.CartesianChartView_columnLayerStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.ColumnCartesianLayerStyle,
): ColumnCartesianLayer =
    getNestedTypedArray(context, resourceId, styleableResourceId).run {
        val defaultShape = Shape.rounded(allPercent = Defaults.COLUMN_ROUNDNESS_PERCENT)
        val mergeMode =
            getInteger(R.styleable.ColumnCartesianLayerStyle_mergeMode, 0)
                .let(ColumnCartesianLayer.MergeMode.entries::get)
        ColumnCartesianLayer(
            columnProvider =
                ColumnCartesianLayer.ColumnProvider.series(
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_column1,
                        styleableResourceId = R.styleable.LineComponent,
                    ).getLineComponent(
                        context = context,
                        defaultColor = context.defaultColors.cartesianLayerColors[0].toInt(),
                        defaultThickness = Defaults.COLUMN_WIDTH,
                        defaultShape = defaultShape,
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_column2,
                        styleableResourceId = R.styleable.LineComponent,
                    ).getLineComponent(
                        context = context,
                        defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(1).toInt(),
                        defaultThickness = Defaults.COLUMN_WIDTH,
                        defaultShape = defaultShape,
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_column3,
                        styleableResourceId = R.styleable.LineComponent,
                    ).getLineComponent(
                        context = context,
                        defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(2).toInt(),
                        defaultThickness = Defaults.COLUMN_WIDTH,
                        defaultShape = defaultShape,
                    ),
                ),
            spacingDp =
                getRawDimension(
                    context = context,
                    index = R.styleable.ColumnCartesianLayerStyle_columnOuterSpacing,
                    defaultValue = Defaults.COLUMN_OUTSIDE_SPACING,
                ),
            innerSpacingDp =
                getRawDimension(
                    context = context,
                    index = R.styleable.ColumnCartesianLayerStyle_columnInnerSpacing,
                    defaultValue = Defaults.COLUMN_INSIDE_SPACING,
                ),
            mergeMode = { mergeMode },
            dataLabel =
                if (getBoolean(R.styleable.ColumnCartesianLayerStyle_showDataLabels, false)) {
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_dataLabelStyle,
                        styleableResourceId = R.styleable.TextComponentStyle,
                    ).getTextComponent(context = context)
                } else {
                    null
                },
            dataLabelVerticalPosition =
                getInteger(R.styleable.ColumnCartesianLayerStyle_dataLabelVerticalPosition, 0)
                    .let { value ->
                        val values = VerticalPosition.entries
                        values[value % values.size]
                    },
            dataLabelRotationDegrees =
                getFloat(
                    R.styleable.ColumnCartesianLayerStyle_dataLabelRotationDegrees,
                    0f,
                ),
        )
    }

internal fun TypedArray.getLineCartesianLayer(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.CartesianChartView_lineLayerStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.LineCartesianLayerStyle,
): LineCartesianLayer =
    getNestedTypedArray(context, resourceId, styleableResourceId).run {
        LineCartesianLayer(
            lines =
                listOf(
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.LineCartesianLayerStyle_line1Spec,
                        styleableResourceId = R.styleable.LineSpec,
                    ).getLineSpec(
                        context = context,
                        defaultColor = context.defaultColors.cartesianLayerColors[0].toInt(),
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.LineCartesianLayerStyle_line2Spec,
                        styleableResourceId = R.styleable.LineSpec,
                    ).getLineSpec(
                        context = context,
                        defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(1).toInt(),
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.LineCartesianLayerStyle_line3Spec,
                        styleableResourceId = R.styleable.LineSpec,
                    ).getLineSpec(
                        context = context,
                        defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(2).toInt(),
                    ),
                ),
            spacingDp =
                getRawDimension(
                    context = context,
                    index = R.styleable.LineCartesianLayerStyle_spacing,
                    defaultValue = Defaults.POINT_SPACING,
                ),
        )
    }

internal fun TypedArray.getCandlestickCartesianLayer(context: Context): CandlestickCartesianLayer {
    val bullishColor = context.defaultColors.bullishCandleColor.toInt()
    val neutralColor = context.defaultColors.neutralCandleColor.toInt()
    val bearishColor = context.defaultColors.bearishCandleColor.toInt()
    return getNestedTypedArray(
        context,
        R.styleable.CartesianChartView_candlestickLayerStyle,
        R.styleable.CandlestickLayerStyle,
    ).use { typedArray ->
        val candleProvider =
            when (typedArray.getInteger(R.styleable.CandlestickLayerStyle_candleStyle, 0)) {
                0 -> {
                    val bullish =
                        typedArray.getCandle(context, R.styleable.CandlestickLayerStyle_bullishCandleStyle)
                            ?: CandlestickCartesianLayer.Candle.sharpFilledCandle(bullishColor)
                    CandlestickCartesianLayer.CandleProvider.absolute(
                        bullish = bullish,
                        neutral =
                            typedArray.getCandle(context, R.styleable.CandlestickLayerStyle_neutralCandleStyle)
                                ?: bullish.copyWithColor(neutralColor),
                        bearish =
                            typedArray.getCandle(context, R.styleable.CandlestickLayerStyle_bearishCandleStyle)
                                ?: bullish.copyWithColor(bearishColor),
                    )
                }
                1 -> {
                    val absolutelyBullishRelativelyBullish =
                        typedArray.getCandle(
                            context,
                            R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyBullishCandleStyle,
                        ) ?: CandlestickCartesianLayer.Candle.sharpHollowCandle(bullishColor)
                    val absolutelyBullishRelativelyNeutral =
                        typedArray.getCandle(
                            context,
                            R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyNeutralCandleStyle,
                        ) ?: absolutelyBullishRelativelyBullish.copyWithColor(neutralColor)
                    val absolutelyBullishRelativelyBearish =
                        typedArray.getCandle(
                            context,
                            R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyBearishCandleStyle,
                        ) ?: absolutelyBullishRelativelyBullish.copyWithColor(bearishColor)
                    val absolutelyBearishRelativelyBullish =
                        typedArray.getCandle(
                            context,
                            R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyBullishCandleStyle,
                        ) ?: CandlestickCartesianLayer.Candle.sharpFilledCandle(bullishColor)
                    CandlestickCartesianLayer.CandleProvider.absoluteRelative(
                        absolutelyBullishRelativelyBullish = absolutelyBullishRelativelyBullish,
                        absolutelyBullishRelativelyNeutral = absolutelyBullishRelativelyNeutral,
                        absolutelyBullishRelativelyBearish = absolutelyBullishRelativelyBearish,
                        absolutelyNeutralRelativelyBullish =
                            typedArray.getCandle(
                                context,
                                R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyBullishCandleStyle,
                            ) ?: absolutelyBullishRelativelyBullish,
                        absolutelyNeutralRelativelyNeutral =
                            typedArray.getCandle(
                                context,
                                R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyNeutralCandleStyle,
                            ) ?: absolutelyBullishRelativelyNeutral,
                        absolutelyNeutralRelativelyBearish =
                            typedArray.getCandle(
                                context,
                                R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyBearishCandleStyle,
                            ) ?: absolutelyBullishRelativelyBearish,
                        absolutelyBearishRelativelyBullish = absolutelyBearishRelativelyBullish,
                        absolutelyBearishRelativelyNeutral =
                            typedArray.getCandle(
                                context,
                                R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyNeutralCandleStyle,
                            ) ?: absolutelyBearishRelativelyBullish.copyWithColor(neutralColor),
                        absolutelyBearishRelativelyBearish =
                            typedArray.getCandle(
                                context,
                                R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyBearishCandleStyle,
                            ) ?: absolutelyBearishRelativelyBullish.copyWithColor(bearishColor),
                    )
                }
                else -> error("Unexpected `candleStyle` value.")
            }
        CandlestickCartesianLayer(
            candles = candleProvider,
            minCandleBodyHeightDp =
                typedArray.getRawDimension(
                    context,
                    R.styleable.CandlestickLayerStyle_minCandleBodyHeight,
                    Defaults.MIN_CANDLE_BODY_HEIGHT_DP,
                ),
            candleSpacingDp =
                typedArray.getRawDimension(
                    context,
                    R.styleable.CandlestickLayerStyle_candleSpacing,
                    Defaults.CANDLE_SPACING_DP,
                ),
            scaleCandleWicks = typedArray.getBoolean(R.styleable.CandlestickLayerStyle_scaleCandleWicks, false),
        )
    }
}

private fun TypedArray.getCandle(
    context: Context,
    resourceId: Int,
): CandlestickCartesianLayer.Candle? =
    if (hasValue(resourceId)) {
        getNestedTypedArray(context, resourceId, R.styleable.CandleStyle).use { typedArray ->
            val body =
                typedArray
                    .getNestedTypedArray(context, R.styleable.CandleStyle_bodyStyle, R.styleable.LineComponent)
                    .getLineComponent(context = context, defaultThickness = Defaults.CANDLE_BODY_WIDTH_DP)
            val topWick =
                if (typedArray.hasValue(R.styleable.CandleStyle_topWickStyle)) {
                    typedArray
                        .getNestedTypedArray(context, R.styleable.CandleStyle_topWickStyle, R.styleable.LineComponent)
                        .getLineComponent(context)
                } else {
                    body.asWick()
                }
            val bottomWick =
                if (typedArray.hasValue(R.styleable.CandleStyle_bottomWickStyle)) {
                    typedArray
                        .getNestedTypedArray(
                            context,
                            R.styleable.CandleStyle_bottomWickStyle,
                            R.styleable.LineComponent,
                        )
                        .getLineComponent(context)
                } else {
                    topWick
                }
            CandlestickCartesianLayer.Candle(body, topWick, bottomWick)
        }
    } else {
        null
    }
