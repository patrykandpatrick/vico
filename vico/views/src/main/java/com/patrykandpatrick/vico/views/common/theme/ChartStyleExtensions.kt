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
import com.patrykandpatrick.vico.core.cartesian.layer.asWick
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.extension.getRepeating
import com.patrykandpatrick.vico.core.common.position.VerticalPosition
import com.patrykandpatrick.vico.core.common.shape.Shapes
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.common.extension.defaultColors

internal fun TypedArray.getColumnCartesianLayer(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.CartesianChartView_columnLayerStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.ColumnCartesianLayerStyle,
): ColumnCartesianLayer =
    getNestedTypedArray(context, resourceId, styleableResourceId).run {
        val defaultShape = Shapes.roundedCornerShape(allPercent = Defaults.COLUMN_ROUNDNESS_PERCENT)
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
    val candles =
        getNestedTypedArray(
            context,
            R.styleable.CartesianChartView_candlestickLayerStyle,
            R.styleable.CandlestickLayerStyle,
        ).use { typedArray ->
            val hasHollowCandleStyle = typedArray.hasHollowCandleStyle()
            val green = context.defaultColors.candlestickGreen.toInt()
            val gray = context.defaultColors.candlestickGray.toInt()
            val red = context.defaultColors.candlestickRed.toInt()

            if (hasHollowCandleStyle) {
                CandlestickCartesianLayer.Candles(
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyBullishCandleStyle,
                        green,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyNeutralCandleStyle,
                        gray,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyBearishCandleStyle,
                        red,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyBullishCandleStyle,
                        green,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyNeutralCandleStyle,
                        gray,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyBearishCandleStyle,
                        red,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyBullishCandleStyle,
                        green,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyNeutralCandleStyle,
                        gray,
                    ),
                    typedArray.getCandle(
                        context,
                        R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyBearishCandleStyle,
                        red,
                    ),
                )
            } else {
                val bullish =
                    typedArray.getCandle(context, R.styleable.CandlestickLayerStyle_bullishCandleStyle, green)
                val neutral =
                    typedArray.getCandle(context, R.styleable.CandlestickLayerStyle_neutralCandleStyle, gray)
                val bearish =
                    typedArray.getCandle(context, R.styleable.CandlestickLayerStyle_bearishCandleStyle, red)
                CandlestickCartesianLayer.Candles(
                    bullish,
                    bullish,
                    bullish,
                    neutral,
                    neutral,
                    neutral,
                    bearish,
                    bearish,
                    bearish,
                )
            }
        }
    return CandlestickCartesianLayer(
        candles = candles,
        minCandleBodyHeightDp =
            getRawDimension(
                context,
                R.styleable.CandlestickLayerStyle_minCandleBodyHeight,
                Defaults.MIN_CANDLE_BODY_HEIGHT_DP,
            ),
        candleSpacingDp =
            getRawDimension(context, R.styleable.CandlestickLayerStyle_candleSpacing, Defaults.CANDLE_SPACING_DP),
    )
}

private fun TypedArray.hasHollowCandleStyle(): Boolean =
    hasValue(R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyBullishCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyNeutralCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyBullishRelativelyBearishCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyBullishCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyNeutralCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyNeutralRelativelyBearishCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyBullishCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyNeutralCandleStyle) ||
        hasValue(R.styleable.CandlestickLayerStyle_absolutelyBearishRelativelyBearishCandleStyle)

private fun TypedArray.getCandle(
    context: Context,
    resourceId: Int,
    defaultColor: Int,
): CandlestickCartesianLayer.Candle =
    getNestedTypedArray(context, resourceId, R.styleable.CandleStyle).use { typedArray ->
        val topWick =
            if (typedArray.hasValue(R.styleable.CandleStyle_topWickStyle)) {
                typedArray.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.CandleStyle_topWickStyle,
                    styleableResourceId = R.styleable.LineComponent,
                ).getLineComponent(
                    context = context,
                    defaultColor = defaultColor,
                    defaultThickness = Defaults.WICK_DEFAULT_WIDTH_DP,
                )
            } else {
                null
            }

        val bottomWick =
            if (typedArray.hasValue(R.styleable.CandleStyle_bottomWickStyle)) {
                typedArray.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.CandleStyle_bottomWickStyle,
                    styleableResourceId = R.styleable.LineComponent,
                ).getLineComponent(
                    context = context,
                    defaultColor = defaultColor,
                    defaultThickness = Defaults.WICK_DEFAULT_WIDTH_DP,
                )
            } else {
                null
            }

        val body =
            typedArray.getNestedTypedArray(
                context = context,
                resourceId = R.styleable.CandleStyle_bodyStyle,
                styleableResourceId = R.styleable.LineComponent,
            ).getLineComponent(
                context = context,
                defaultColor = defaultColor,
                defaultThickness = Defaults.CANDLE_BODY_WIDTH_DP,
            )

        CandlestickCartesianLayer.Candle(
            body = body,
            topWick = topWick ?: body.asWick(),
            bottomWick = bottomWick ?: topWick ?: body.asWick(),
        )
    }
