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

import android.animation.TimeInterpolator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AccelerateInterpolator
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.FadingEdges
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Defaults.FADING_EDGE_VISIBILITY_THRESHOLD_DP
import com.patrykandpatrick.vico.core.common.hasFlag
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.views.R

internal class ThemeHandler(private val context: Context, attrs: AttributeSet?) {
  var scrollEnabled: Boolean = false
    private set

  var isChartZoomEnabled: Boolean = false
    private set

  var chart: CartesianChart? = null
    private set

  init {
    context.obtainStyledAttributes(attrs, R.styleable.CartesianChartView).use { typedArray ->
      scrollEnabled =
        typedArray.getBoolean(
          R.styleable.CartesianChartView_scrollEnabled,
          typedArray.getBoolean(
            R.styleable.CartesianChartView_chartHorizontalScrollingEnabled,
            true,
          ),
        )
      isChartZoomEnabled =
        typedArray.getBoolean(
          R.styleable.CartesianChartView_zoomEnabled,
          typedArray.getBoolean(R.styleable.CartesianChartView_chartZoomEnabled, true),
        )

      context.obtainStyledAttributes(attrs, R.styleable.CartesianChartView).use {
        chart = getChart(it, typedArray)
      }
    }
  }

  private fun <P : Axis.Position> TypedArray.getAxis(position: P): Axis<P>? {
    val visibilityAttributeIndex: Int
    var styleAttributeIndex: Int
    when (position) {
      Axis.Position.Vertical.Start -> {
        visibilityAttributeIndex = R.styleable.CartesianChartView_showStartAxis
        styleAttributeIndex = R.styleable.CartesianChartView_startAxisStyle
      }
      Axis.Position.Horizontal.Top -> {
        visibilityAttributeIndex = R.styleable.CartesianChartView_showTopAxis
        styleAttributeIndex = R.styleable.CartesianChartView_topAxisStyle
      }
      Axis.Position.Vertical.End -> {
        visibilityAttributeIndex = R.styleable.CartesianChartView_showEndAxis
        styleAttributeIndex = R.styleable.CartesianChartView_endAxisStyle
      }
      Axis.Position.Horizontal.Bottom -> {
        visibilityAttributeIndex = R.styleable.CartesianChartView_showBottomAxis
        styleAttributeIndex = R.styleable.CartesianChartView_bottomAxisStyle
      }
      else -> throw IllegalArgumentException("Unexpected `Axis.Position` subclass.")
    }
    if (!getBoolean(visibilityAttributeIndex, false)) return null
    if (!hasValue(styleAttributeIndex)) {
      styleAttributeIndex = R.styleable.CartesianChartView_axisStyle
    }
    return getNestedTypedArray(context, styleAttributeIndex, R.styleable.AxisStyle).use { axisStyle
      ->
      val line =
        if (axisStyle.getBoolean(R.styleable.AxisStyle_showLine, true)) {
          axisStyle
            .getNestedTypedArray(
              context,
              R.styleable.AxisStyle_lineStyle,
              R.styleable.LineComponentStyle,
            )
            .getLineComponent(context)
        } else {
          null
        }
      val label =
        axisStyle
          .getNestedTypedArray(
            context,
            R.styleable.AxisStyle_labelStyle,
            R.styleable.TextComponentStyle,
          )
          .getTextComponent(context)
      val labelRotationDegrees = axisStyle.getFloat(R.styleable.AxisStyle_labelRotationDegrees, 0f)
      val tick =
        if (axisStyle.getBoolean(R.styleable.AxisStyle_showTicks, true)) {
          axisStyle
            .getNestedTypedArray(
              context,
              R.styleable.AxisStyle_tickStyle,
              R.styleable.LineComponentStyle,
            )
            .getLineComponent(context)
        } else {
          null
        }
      val tickLengthDp =
        axisStyle.getRawDimension(
          context,
          R.styleable.AxisStyle_tickLength,
          Defaults.AXIS_TICK_LENGTH,
        )
      val guideline =
        if (axisStyle.getBoolean(R.styleable.AxisStyle_showGuidelines, true)) {
          axisStyle
            .getNestedTypedArray(
              context,
              R.styleable.AxisStyle_guidelineStyle,
              R.styleable.LineComponentStyle,
            )
            .getLineComponent(context = context, defaultShape = DashedShape())
        } else {
          null
        }
      val titleComponent =
        if (axisStyle.getBoolean(R.styleable.AxisStyle_showTitle, false)) {
          axisStyle
            .getNestedTypedArray(
              context,
              R.styleable.AxisStyle_titleStyle,
              R.styleable.TextComponentStyle,
            )
            .getTextComponent(context)
        } else {
          null
        }
      val title = axisStyle.getString(R.styleable.AxisStyle_title)
      @Suppress("UNCHECKED_CAST")
      when (position) {
        is Axis.Position.Horizontal ->
          HorizontalAxis(
            position,
            line,
            label,
            labelRotationDegrees,
            tick,
            tickLengthDp,
            guideline,
            getHorizontalAxisItemPlacer(),
            titleComponent,
            title,
          )
        is Axis.Position.Vertical ->
          VerticalAxis(
            position = position,
            line = line,
            label = label,
            labelRotationDegrees = labelRotationDegrees,
            horizontalLabelPosition =
              VerticalAxis.HorizontalLabelPosition.entries[
                  axisStyle.getInteger(
                    R.styleable.AxisStyle_verticalAxisHorizontalLabelPosition,
                    0,
                  )],
            verticalLabelPosition =
              VerticalAxis.VerticalLabelPosition.entries[
                  axisStyle.getInteger(R.styleable.AxisStyle_verticalAxisVerticalLabelPosition, 0)],
            tick = tick,
            tickLengthDp = tickLengthDp,
            guideline = guideline,
            itemPlacer = axisStyle.getVerticalAxisItemPlacer(),
            titleComponent = titleComponent,
            title = title,
          )
        else -> throw IllegalArgumentException("Unexpected `Axis.Position` subclass.")
      }
        as Axis<P>
    }
  }

  private fun TypedArray.getHorizontalLayout(): HorizontalLayout =
    when (getInt(R.styleable.CartesianChartView_horizontalLayout, 0)) {
      0 -> HorizontalLayout.Segmented
      else ->
        HorizontalLayout.FullWidth(
          getRawDimension(context, R.styleable.CartesianChartView_scalableStartContentPadding, 0f),
          getRawDimension(context, R.styleable.CartesianChartView_scalableEndContentPadding, 0f),
          getRawDimension(
            context,
            R.styleable.CartesianChartView_unscalableStartContentPadding,
            0f,
          ),
          getRawDimension(context, R.styleable.CartesianChartView_unscalableEndContentPadding, 0f),
        )
    }

  private fun getChart(
    cartesianChartViewTypedArray: TypedArray,
    baseTypedArray: TypedArray,
  ): CartesianChart {
    val layerFlags = cartesianChartViewTypedArray.getInt(R.styleable.CartesianChartView_layers, 0)

    val columnLayer =
      if (layerFlags.hasFlag(COLUMN_LAYER)) baseTypedArray.getColumnCartesianLayer(context)
      else null
    val lineLayer =
      if (layerFlags.hasFlag(LINE_LAYER)) baseTypedArray.getLineCartesianLayer(context) else null
    val candlestickLayer =
      if (layerFlags.hasFlag(CANDLESTICK_LAYER))
        baseTypedArray.getCandlestickCartesianLayer(context)
      else null

    return CartesianChart(
      layers =
        buildList {
            if (columnLayer != null) add(columnLayer)
            if (lineLayer != null) add(lineLayer)
            if (candlestickLayer != null) add(candlestickLayer)
          }
          .toTypedArray(),
      startAxis = baseTypedArray.getAxis(Axis.Position.Vertical.Start),
      topAxis = baseTypedArray.getAxis(Axis.Position.Horizontal.Top),
      endAxis = baseTypedArray.getAxis(Axis.Position.Vertical.End),
      bottomAxis = baseTypedArray.getAxis(Axis.Position.Horizontal.Bottom),
      fadingEdges = baseTypedArray.getFadingEdges(),
      horizontalLayout = baseTypedArray.getHorizontalLayout(),
    )
  }

  private fun TypedArray.getFadingEdges(): FadingEdges? {
    val edgesLength = getRawDimension(context, R.styleable.CartesianChartView_fadingEdgeWidth, 0f)
    val startLength =
      getRawDimension(context, R.styleable.CartesianChartView_startFadingEdgeWidth, edgesLength)
    val endLength =
      getRawDimension(context, R.styleable.CartesianChartView_endFadingEdgeWidth, edgesLength)
    val threshold =
      getRawDimension(
        context,
        R.styleable.CartesianChartView_fadingEdgeVisibilityThreshold,
        FADING_EDGE_VISIBILITY_THRESHOLD_DP,
      )

    return if (startLength > 0f || endLength > 0f) {
      val interpolatorClassName =
        getString(R.styleable.CartesianChartView_fadingEdgeVisibilityInterpolator)

      val interpolator =
        if (interpolatorClassName != null) {
          try {
            context.classLoader
              .loadClass(interpolatorClassName)
              .getDeclaredConstructor()
              .newInstance() as? TimeInterpolator
          } catch (e: Exception) {
            Log.e(
              "ChartView",
              "Caught exception when trying to instantiate $interpolatorClassName " +
                "as fade interpolator.",
            )
            null
          }
        } else {
          null
        }

      FadingEdges(
        startEdgeWidthDp = startLength,
        endEdgeWidthDp = endLength,
        visibilityThresholdDp = threshold,
        visibilityInterpolator = interpolator ?: AccelerateInterpolator(),
      )
    } else {
      null
    }
  }

  private fun TypedArray.getHorizontalAxisItemPlacer(): HorizontalAxis.ItemPlacer =
    HorizontalAxis.ItemPlacer.default(
      getInteger(R.styleable.AxisStyle_horizontalAxisLabelSpacing, 1),
      getInteger(R.styleable.AxisStyle_horizontalAxisLabelOffset, 0),
      getBoolean(R.styleable.AxisStyle_shiftExtremeHorizontalAxisTicks, true),
      getBoolean(R.styleable.AxisStyle_addExtremeHorizontalAxisLabelPadding, false),
    )

  private fun TypedArray.getVerticalAxisItemPlacer(): VerticalAxis.ItemPlacer {
    val shiftTopLines = getBoolean(R.styleable.AxisStyle_shiftTopVerticalAxisLines, true)
    return if (
      hasValue(R.styleable.AxisStyle_verticalAxisItemCount) ||
        hasValue(R.styleable.AxisStyle_maxVerticalAxisItemCount)
    ) {
      val itemCount =
        getInteger(
          R.styleable.AxisStyle_verticalAxisItemCount,
          getInteger(R.styleable.AxisStyle_maxVerticalAxisItemCount, -1),
        )
      VerticalAxis.ItemPlacer.count({ itemCount }, shiftTopLines)
    } else {
      VerticalAxis.ItemPlacer.step(shiftTopLines = shiftTopLines)
    }
  }

  private companion object {
    const val COLUMN_LAYER = 1
    const val LINE_LAYER = 2
    const val CANDLESTICK_LAYER = 4
  }
}
