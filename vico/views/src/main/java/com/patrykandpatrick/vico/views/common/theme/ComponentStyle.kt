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
import android.graphics.Color
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.DefaultAlpha
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.copyColor
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
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
    color = array.getColorExtended(R.styleable.LineComponentStyle_color, defaultColor),
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
    strokeColor =
      array.getColorExtended(R.styleable.LineComponentStyle_strokeColor, Color.TRANSPARENT),
    strokeThicknessDp =
      array.getRawDimension(context, R.styleable.LineComponentStyle_strokeThickness, 0f),
  )
}

internal fun TypedArray.getComponent(context: Context): Component? = use { array ->
  if (!hasValue(R.styleable.ComponentStyle_color)) {
    return@use null
  }

  val layeredComponent =
    when {
      hasValue(R.styleable.ComponentStyle_overlayingComponentStyle) ->
        getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ComponentStyle_overlayingComponentStyle,
            styleableResourceId = R.styleable.ComponentStyle,
          )
          .getComponent(context)
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
      color = array.getColorExtended(R.styleable.ComponentStyle_color),
      shape =
        getNestedTypedArray(context, R.styleable.ComponentStyle_shapeStyle, R.styleable.ShapeStyle)
          .getShape(context),
      strokeColor =
        array.getColorExtended(R.styleable.ComponentStyle_strokeColor, Color.TRANSPARENT),
      strokeThicknessDp =
        array.getRawDimension(context, R.styleable.ComponentStyle_strokeThickness, 0f),
    )

  if (layeredComponent != null) {
    LayeredComponent(
      rear = baseComponent,
      front = layeredComponent,
      padding =
        Dimensions(
          allDp =
            getRawDimension(
              context = context,
              index = R.styleable.ComponentStyle_layeredComponentPadding,
              defaultValue =
                getRawDimension(
                  context = context,
                  index = R.styleable.ComponentStyle_overlayingComponentPadding,
                  defaultValue = 0f,
                ),
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
      getColor(R.styleable.LineStyle_color, defaultColor),
    )

  val negativeLineColor =
    getColor(
      R.styleable.LineStyle_negativeColor,
      getColor(R.styleable.LineStyle_color, defaultColor),
    )

  val positiveGradientTopColor =
    getColor(
      R.styleable.LineStyle_positiveGradientTopColor,
      getColor(
        R.styleable.LineStyle_gradientTopColor,
        positiveLineColor.copyColor(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_START),
      ),
    )

  val positiveGradientBottomColor =
    getColor(
      R.styleable.LineStyle_positiveGradientBottomColor,
      getColor(
        R.styleable.LineStyle_gradientBottomColor,
        positiveLineColor.copyColor(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_END),
      ),
    )

  val negativeGradientTopColor =
    getColor(
      R.styleable.LineStyle_negativeGradientTopColor,
      getColor(
        R.styleable.LineStyle_gradientBottomColor,
        negativeLineColor.copyColor(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_END),
      ),
    )

  val negativeGradientBottomColor =
    getColor(
      R.styleable.LineStyle_negativeGradientBottomColor,
      getColor(
        R.styleable.LineStyle_gradientTopColor,
        negativeLineColor.copyColor(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_START),
      ),
    )

  return LineCartesianLayer.Line(
    fill =
      if (positiveLineColor != negativeLineColor) {
        LineCartesianLayer.LineFill.double(Fill(positiveLineColor), Fill(negativeLineColor))
      } else {
        LineCartesianLayer.LineFill.single(Fill(positiveLineColor))
      },
    thicknessDp =
      getRawDimension(context, R.styleable.LineStyle_thickness, Defaults.LINE_SPEC_THICKNESS_DP),
    areaFill =
      LineCartesianLayer.AreaFill.double(
        Fill(DynamicShader.verticalGradient(positiveGradientTopColor, positiveGradientBottomColor)),
        Fill(DynamicShader.verticalGradient(negativeGradientTopColor, negativeGradientBottomColor)),
      ),
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
      LineCartesianLayer.PointConnector.cubic(
        getFraction(R.styleable.LineStyle_curvature, Defaults.LINE_CURVATURE)
      ),
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
    dataLabelVerticalPosition =
      VerticalPosition.entries[getInteger(R.styleable.LineStyle_dataLabelVerticalPosition, 0)],
    dataLabelRotationDegrees = getFloat(R.styleable.LineStyle_dataLabelRotationDegrees, 0f),
  )
}
