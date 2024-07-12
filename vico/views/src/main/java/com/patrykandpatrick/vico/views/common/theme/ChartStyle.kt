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
      when (getInteger(R.styleable.ColumnCartesianLayerStyle_mergeMode, 0)) {
        0 ->
          ColumnCartesianLayer.MergeMode.Grouped(
            getRawDimension(
              context,
              R.styleable.ColumnCartesianLayerStyle_groupedColumnSpacing,
              Defaults.GROUPED_COLUMN_SPACING,
            )
          )
        1 -> ColumnCartesianLayer.MergeMode.Stacked
        else -> throw IllegalArgumentException("Unexpected `mergeMode` value.")
      }
    ColumnCartesianLayer(
      columnProvider =
        ColumnCartesianLayer.ColumnProvider.series(
          getNestedTypedArray(
              context = context,
              resourceId = R.styleable.ColumnCartesianLayerStyle_column1Style,
              styleableResourceId = R.styleable.LineComponentStyle,
            )
            .getLineComponent(
              context = context,
              defaultColor = context.defaultColors.cartesianLayerColors[0].toInt(),
              defaultThickness = Defaults.COLUMN_WIDTH,
              defaultShape = defaultShape,
            ),
          getNestedTypedArray(
              context = context,
              resourceId = R.styleable.ColumnCartesianLayerStyle_column2Style,
              styleableResourceId = R.styleable.LineComponentStyle,
            )
            .getLineComponent(
              context = context,
              defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(1).toInt(),
              defaultThickness = Defaults.COLUMN_WIDTH,
              defaultShape = defaultShape,
            ),
          getNestedTypedArray(
              context = context,
              resourceId = R.styleable.ColumnCartesianLayerStyle_column3Style,
              styleableResourceId = R.styleable.LineComponentStyle,
            )
            .getLineComponent(
              context = context,
              defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(2).toInt(),
              defaultThickness = Defaults.COLUMN_WIDTH,
              defaultShape = defaultShape,
            ),
        ),
      columnCollectionSpacingDp =
        getRawDimension(
          context = context,
          index = R.styleable.ColumnCartesianLayerStyle_columnCollectionSpacing,
          defaultValue = Defaults.COLUMN_COLLECTION_SPACING,
        ),
      mergeMode = { mergeMode },
      dataLabel =
        if (getBoolean(R.styleable.ColumnCartesianLayerStyle_showDataLabels, false)) {
          getNestedTypedArray(
              context = context,
              resourceId = R.styleable.ColumnCartesianLayerStyle_dataLabelStyle,
              styleableResourceId = R.styleable.TextComponentStyle,
            )
            .getTextComponent(context = context)
        } else {
          null
        },
      dataLabelVerticalPosition =
        VerticalPosition.entries[
            getInteger(R.styleable.ColumnCartesianLayerStyle_dataLabelVerticalPosition, 0)],
      dataLabelRotationDegrees =
        getFloat(R.styleable.ColumnCartesianLayerStyle_dataLabelRotationDegrees, 0f),
    )
  }

internal fun TypedArray.getLineCartesianLayer(
  context: Context,
  @StyleableRes resourceId: Int = R.styleable.CartesianChartView_lineLayerStyle,
  @StyleableRes styleableResourceId: IntArray = R.styleable.LineCartesianLayerStyle,
): LineCartesianLayer =
  getNestedTypedArray(context, resourceId, styleableResourceId).run {
    LineCartesianLayer(
      lineProvider =
        LineCartesianLayer.LineProvider.series(
          getNestedTypedArray(
              context = context,
              resourceId = R.styleable.LineCartesianLayerStyle_line1Style,
              styleableResourceId = R.styleable.LineStyle,
            )
            .getLine(
              context = context,
              defaultColor = context.defaultColors.cartesianLayerColors[0].toInt(),
            ),
          getNestedTypedArray(
              context = context,
              resourceId = R.styleable.LineCartesianLayerStyle_line2Style,
              styleableResourceId = R.styleable.LineStyle,
            )
            .getLine(
              context = context,
              defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(1).toInt(),
            ),
          getNestedTypedArray(
              context = context,
              resourceId = R.styleable.LineCartesianLayerStyle_line3Style,
              styleableResourceId = R.styleable.LineStyle,
            )
            .getLine(
              context = context,
              defaultColor = context.defaultColors.cartesianLayerColors.getRepeating(2).toInt(),
            ),
        ),
      pointSpacingDp =
        getRawDimension(
          context = context,
          index = R.styleable.LineCartesianLayerStyle_pointSpacing,
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
      R.styleable.CandlestickCartesianLayerStyle,
    )
    .use { typedArray ->
      val candleProvider =
        when (typedArray.getInteger(R.styleable.CandlestickCartesianLayerStyle_candleStyle, 0)) {
          0 -> {
            val bullish =
              typedArray.getCandle(
                context,
                R.styleable.CandlestickCartesianLayerStyle_bullishCandleStyle,
              ) ?: CandlestickCartesianLayer.Candle.sharpFilledCandle(bullishColor)
            CandlestickCartesianLayer.CandleProvider.absolute(
              bullish = bullish,
              neutral =
                typedArray.getCandle(
                  context,
                  R.styleable.CandlestickCartesianLayerStyle_neutralCandleStyle,
                ) ?: bullish.copyWithColor(neutralColor),
              bearish =
                typedArray.getCandle(
                  context,
                  R.styleable.CandlestickCartesianLayerStyle_bearishCandleStyle,
                ) ?: bullish.copyWithColor(bearishColor),
            )
          }
          1 -> {
            val absolutelyBullishRelativelyBullish =
              typedArray.getCandle(
                context,
                R.styleable
                  .CandlestickCartesianLayerStyle_absolutelyBullishRelativelyBullishCandleStyle,
              ) ?: CandlestickCartesianLayer.Candle.sharpHollowCandle(bullishColor)
            val absolutelyBullishRelativelyNeutral =
              typedArray.getCandle(
                context,
                R.styleable
                  .CandlestickCartesianLayerStyle_absolutelyBullishRelativelyNeutralCandleStyle,
              ) ?: absolutelyBullishRelativelyBullish.copyWithColor(neutralColor)
            val absolutelyBullishRelativelyBearish =
              typedArray.getCandle(
                context,
                R.styleable
                  .CandlestickCartesianLayerStyle_absolutelyBullishRelativelyBearishCandleStyle,
              ) ?: absolutelyBullishRelativelyBullish.copyWithColor(bearishColor)
            val absolutelyBearishRelativelyBullish =
              typedArray.getCandle(
                context,
                R.styleable
                  .CandlestickCartesianLayerStyle_absolutelyBearishRelativelyBullishCandleStyle,
              ) ?: CandlestickCartesianLayer.Candle.sharpFilledCandle(bullishColor)
            CandlestickCartesianLayer.CandleProvider.absoluteRelative(
              absolutelyBullishRelativelyBullish = absolutelyBullishRelativelyBullish,
              absolutelyBullishRelativelyNeutral = absolutelyBullishRelativelyNeutral,
              absolutelyBullishRelativelyBearish = absolutelyBullishRelativelyBearish,
              absolutelyNeutralRelativelyBullish =
                typedArray.getCandle(
                  context,
                  R.styleable
                    .CandlestickCartesianLayerStyle_absolutelyNeutralRelativelyBullishCandleStyle,
                ) ?: absolutelyBullishRelativelyBullish,
              absolutelyNeutralRelativelyNeutral =
                typedArray.getCandle(
                  context,
                  R.styleable
                    .CandlestickCartesianLayerStyle_absolutelyNeutralRelativelyNeutralCandleStyle,
                ) ?: absolutelyBullishRelativelyNeutral,
              absolutelyNeutralRelativelyBearish =
                typedArray.getCandle(
                  context,
                  R.styleable
                    .CandlestickCartesianLayerStyle_absolutelyNeutralRelativelyBearishCandleStyle,
                ) ?: absolutelyBullishRelativelyBearish,
              absolutelyBearishRelativelyBullish = absolutelyBearishRelativelyBullish,
              absolutelyBearishRelativelyNeutral =
                typedArray.getCandle(
                  context,
                  R.styleable
                    .CandlestickCartesianLayerStyle_absolutelyBearishRelativelyNeutralCandleStyle,
                ) ?: absolutelyBearishRelativelyBullish.copyWithColor(neutralColor),
              absolutelyBearishRelativelyBearish =
                typedArray.getCandle(
                  context,
                  R.styleable
                    .CandlestickCartesianLayerStyle_absolutelyBearishRelativelyBearishCandleStyle,
                ) ?: absolutelyBearishRelativelyBullish.copyWithColor(bearishColor),
            )
          }
          else -> throw IllegalArgumentException("Unexpected `candleStyle` value.")
        }
      CandlestickCartesianLayer(
        candles = candleProvider,
        minCandleBodyHeightDp =
          typedArray.getRawDimension(
            context,
            R.styleable.CandlestickCartesianLayerStyle_minCandleBodyHeight,
            Defaults.MIN_CANDLE_BODY_HEIGHT_DP,
          ),
        candleSpacingDp =
          typedArray.getRawDimension(
            context,
            R.styleable.CandlestickCartesianLayerStyle_candleSpacing,
            Defaults.CANDLE_SPACING_DP,
          ),
        scaleCandleWicks =
          typedArray.getBoolean(R.styleable.CandlestickCartesianLayerStyle_scaleCandleWicks, false),
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
          .getNestedTypedArray(
            context,
            R.styleable.CandleStyle_bodyStyle,
            R.styleable.LineComponentStyle,
          )
          .getLineComponent(context = context, defaultThickness = Defaults.CANDLE_BODY_WIDTH_DP)
      val topWick =
        if (typedArray.hasValue(R.styleable.CandleStyle_topWickStyle)) {
          typedArray
            .getNestedTypedArray(
              context,
              R.styleable.CandleStyle_topWickStyle,
              R.styleable.LineComponentStyle,
            )
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
              R.styleable.LineComponentStyle,
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
