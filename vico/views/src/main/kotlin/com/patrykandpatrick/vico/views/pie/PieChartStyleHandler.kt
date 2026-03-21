/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.pie

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.common.Defaults
import com.patrykandpatrick.vico.views.common.component.TextComponent
import com.patrykandpatrick.vico.views.common.defaultColors
import com.patrykandpatrick.vico.views.common.theme.getFraction
import com.patrykandpatrick.vico.views.common.theme.getNestedTypedArray
import com.patrykandpatrick.vico.views.common.theme.getRawDimension
import com.patrykandpatrick.vico.views.common.theme.getTextComponent
import com.patrykandpatrick.vico.views.common.theme.use

internal class PieChartStyleHandler(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
  internal val sliceSpacing: Float
  internal val startAngle: Float
  internal val outerSize: PieSize.Outer
  internal val innerSize: PieSize.Inner
  internal val slices: List<PieChart.Slice>

  init {
    val typedArray =
      context.obtainStyledAttributes(attrs, R.styleable.PieChartView, defStyleAttr, 0)
    sliceSpacing = typedArray.getRawDimension(context, R.styleable.PieChartView_sliceSpacing, 0f)
    startAngle =
      typedArray.getFloat(R.styleable.PieChartView_startAngle, Defaults.PIE_CHART_START_ANGLE)
    outerSize = typedArray.getOuterSize(context)
    innerSize = typedArray.getInnerSize(context)
    val genericSliceLabel =
      if (typedArray.hasValue(R.styleable.PieChartView_pieSliceLabelStyle)) {
        typedArray.getSliceLabel(context, R.styleable.PieChartView_pieSliceLabelStyle)
      } else {
        null
      }
    slices = typedArray.getSlices(context, genericSliceLabel)
    typedArray.recycle()
  }

  private fun TypedArray.getOuterSize(context: Context): PieSize.Outer {
    val outerSizeValue = getRawDimension(context, R.styleable.PieChartView_pieOuterSize, -1f)
    return if (outerSizeValue <= 0) PieSize.Outer.Fill else PieSize.Outer.fixed(outerSizeValue)
  }

  private fun TypedArray.getInnerSize(context: Context): PieSize.Inner =
    PieSize.Inner.fixed(getRawDimension(context, R.styleable.PieChartView_pieInnerSize, 0f))

  private fun TypedArray.getSlices(
    context: Context,
    genericSliceLabel: PieChart.SliceLabel?,
  ): List<PieChart.Slice> =
    listOf(
        R.styleable.PieChartView_pieSliceStyle1,
        R.styleable.PieChartView_pieSliceStyle2,
        R.styleable.PieChartView_pieSliceStyle3,
        R.styleable.PieChartView_pieSliceStyle4,
        R.styleable.PieChartView_pieSliceStyle5,
        R.styleable.PieChartView_pieSliceStyle6,
        R.styleable.PieChartView_pieSliceStyle7,
        R.styleable.PieChartView_pieSliceStyle8,
      )
      .mapNotNull { index ->
        if (hasValue(index)) {
          getNestedTypedArray(context, index, R.styleable.PieChartSliceStyle)
            .getSlice(context, genericSliceLabel)
        } else {
          null
        }
      }
      .ifEmpty {
        context.defaultColors.cartesianLayerColors.take(3).map { color ->
          PieChart.Slice(color = color.toInt())
        }
      }

  private fun TypedArray.getSliceLabel(
    context: Context,
    @StyleableRes resourceId: Int,
  ): PieChart.SliceLabel =
    getNestedTypedArray(context, resourceId, R.styleable.PieChartSliceLabelStyle).use { typedArray
      ->
      val type = typedArray.getInt(R.styleable.PieChartSliceLabelStyle_pieSliceLabelType, 0)
      val textComponent =
        if (typedArray.hasValue(R.styleable.PieChartSliceLabelStyle_pieSliceLabelTextStyle)) {
          typedArray
            .getNestedTypedArray(
              context,
              R.styleable.PieChartSliceLabelStyle_pieSliceLabelTextStyle,
              R.styleable.TextComponentStyle,
            )
            .getTextComponent(context)
        } else {
          TextComponent()
        }
      when (type) {
        SLICE_LABEL_TYPE_INSIDE -> PieChart.SliceLabel.Inside(textComponent)
        SLICE_LABEL_TYPE_OUTSIDE ->
          PieChart.SliceLabel.Outside(
            textComponent = textComponent,
            lineColor =
              typedArray.getColor(
                R.styleable.PieChartSliceLabelStyle_pieSliceLabelLineColor,
                Color.BLACK,
              ),
            initialLineWidthDp =
              typedArray.getRawDimension(
                context,
                R.styleable.PieChartSliceLabelStyle_pieSliceLabelLineWidth,
                1f,
              ),
            angledSegmentLengthDp =
              typedArray.getRawDimension(
                context,
                R.styleable.PieChartSliceLabelStyle_pieSliceLabelAngledSegmentLength,
                Defaults.SLICE_ANGLED_SEGMENT_LENGTH,
              ),
            horizontalSegmentLengthDp =
              typedArray.getRawDimension(
                context,
                R.styleable.PieChartSliceLabelStyle_pieSliceLabelHorizontalSegmentLength,
                Defaults.SLICE_HORIZONTAL_SEGMENT_LENGTH,
              ),
            maxWidthToBoundsRatio =
              typedArray.getFraction(
                R.styleable.PieChartSliceLabelStyle_pieSliceLabelMaxWidthToBoundsRatio,
                Defaults.SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
              ),
          )
        else -> throw IllegalArgumentException("Unknown slice-label type: $type")
      }
    }

  private fun TypedArray.getSlice(
    context: Context,
    genericSliceLabel: PieChart.SliceLabel?,
  ): PieChart.Slice {
    val label =
      if (hasValue(R.styleable.PieChartSliceStyle_pieSliceLabelStyle)) {
        getSliceLabel(context, R.styleable.PieChartSliceStyle_pieSliceLabelStyle)
      } else {
        genericSliceLabel
      }
    return PieChart.Slice(
      color = getColor(R.styleable.PieChartSliceStyle_sliceColor, Color.LTGRAY),
      strokeWidthDp = getRawDimension(context, R.styleable.PieChartSliceStyle_sliceStrokeWidth, 0f),
      strokeColor = getColor(R.styleable.PieChartSliceStyle_sliceStrokeColor, Color.TRANSPARENT),
      offsetFromCenterDp =
        getRawDimension(context, R.styleable.PieChartSliceStyle_sliceOffsetFromCenter, 0f),
      label = label,
    )
  }

  private companion object {
    const val SLICE_LABEL_TYPE_INSIDE: Int = 0
    const val SLICE_LABEL_TYPE_OUTSIDE: Int = 1
  }
}
