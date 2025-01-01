/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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
import android.graphics.Color
import android.graphics.Paint
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.common.defaultColors

internal fun TypedArray.getLineComponent(
  context: Context,
  defaultColor: Int = context.defaultColors.lineColor.toInt(),
  defaultThickness: Float = Defaults.AXIS_LINE_WIDTH,
  defaultShape: Shape = Shape.Rectangle,
): LineComponent = use { array ->
  LineComponent(
    fill = Fill(array.getColorExtended(R.styleable.LineComponentStyle_android_color, defaultColor)),
    thicknessDp =
      array.getRawDimension(context, R.styleable.LineComponentStyle_thickness, defaultThickness),
    shape =
      if (hasValue(R.styleable.LineComponentStyle_shapeStyle)) {
        getNestedTypedArray(
            context,
            R.styleable.LineComponentStyle_shapeStyle,
            R.styleable.ShapeStyle,
          )
          .getShape(context)
      } else {
        defaultShape
      },
    strokeFill =
      Fill(array.getColorExtended(R.styleable.LineComponentStyle_strokeColor, Color.TRANSPARENT)),
    strokeThicknessDp =
      array.getRawDimension(context, R.styleable.LineComponentStyle_strokeThickness, 0f),
  )
}

internal fun TypedArray.getComponent(context: Context): Component? = use { array ->
  if (!hasValue(R.styleable.ComponentStyle_android_color)) {
    return@use null
  }

  val layeredComponent =
    when {
      hasValue(R.styleable.ComponentStyle_layeredComponentStyle) ->
        getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ComponentStyle_layeredComponentStyle,
            styleableResourceId = R.styleable.ComponentStyle,
          )
          .getComponent(context)
      else -> null
    }

  val baseComponent =
    ShapeComponent(
      fill = Fill(array.getColorExtended(R.styleable.ComponentStyle_android_color)),
      shape =
        getNestedTypedArray(context, R.styleable.ComponentStyle_shapeStyle, R.styleable.ShapeStyle)
          .getShape(context),
      strokeFill =
        Fill(array.getColorExtended(R.styleable.ComponentStyle_strokeColor, Color.TRANSPARENT)),
      strokeThicknessDp =
        array.getRawDimension(context, R.styleable.ComponentStyle_strokeThickness, 0f),
    )

  if (layeredComponent != null) {
    LayeredComponent(
      back = baseComponent,
      front = layeredComponent,
      padding =
        Insets(
          allDp =
            getRawDimension(
              context = context,
              index = R.styleable.ComponentStyle_layeredComponentPadding,
              defaultValue = 0f,
            )
        ),
    )
  } else {
    baseComponent
  }
}

internal fun TypedArray.getLine(context: Context, defaultColor: Int): LineCartesianLayer.Line {
  val positiveLineColor =
    getColor(
      R.styleable.LineStyle_positiveColor,
      getColor(R.styleable.LineStyle_android_color, defaultColor),
    )

  val negativeLineColor =
    getColor(
      R.styleable.LineStyle_negativeColor,
      getColor(R.styleable.LineStyle_android_color, defaultColor),
    )

  val dashLength = getRawDimension(context, R.styleable.LineStyle_dashLength, 0f)
  val dashGap = getRawDimension(context, R.styleable.LineStyle_gapLength, 0f)
  val thicknessDp =
    getRawDimension(context, R.styleable.LineStyle_thickness, Defaults.LINE_SPEC_THICKNESS_DP)
  val cap = Paint.Cap.entries[getInteger(R.styleable.LineStyle_android_strokeLineCap, 1)]

  return LineCartesianLayer.Line(
    fill =
      if (positiveLineColor != negativeLineColor) {
        LineCartesianLayer.LineFill.double(Fill(positiveLineColor), Fill(negativeLineColor))
      } else {
        LineCartesianLayer.LineFill.single(Fill(positiveLineColor))
      },
    pointProvider =
      getNestedTypedArray(context, R.styleable.LineStyle_pointStyle, R.styleable.ComponentStyle)
        .getComponent(context)
        ?.let { component ->
          LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
              component,
              getRawDimension(context, R.styleable.LineStyle_pointSize, Defaults.POINT_SIZE),
            )
          )
        },
    pointConnector =
      getFraction(R.styleable.LineStyle_curvature, 0f).let { curvature ->
        if (curvature == 0f) {
          LineCartesianLayer.PointConnector.Sharp
        } else {
          LineCartesianLayer.PointConnector.cubic(curvature)
        }
      },
    dataLabel =
      if (getBoolean(R.styleable.LineStyle_showDataLabels, false)) {
        getNestedTypedArray(
            context,
            R.styleable.LineStyle_dataLabelStyle,
            R.styleable.TextComponentStyle,
          )
          .getTextComponent(context)
      } else {
        null
      },
    dataLabelPosition =
      Position.Vertical.entries[getInteger(R.styleable.LineStyle_dataLabelPosition, 0)],
    dataLabelRotationDegrees = getFloat(R.styleable.LineStyle_dataLabelRotationDegrees, 0f),
    stroke =
      if (dashLength > 0f && dashGap > 0f) {
        LineCartesianLayer.LineStroke.Dashed(thicknessDp, cap, dashLength, dashGap)
      } else {
        LineCartesianLayer.LineStroke.Continuous(thicknessDp, cap)
      },
  )
}
