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

package com.patrykandpatrick.vico.views.theme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.DefaultDimens.SLICE_ANGLED_SEGMENT_WIDTH
import com.patrykandpatrick.vico.core.DefaultDimens.SLICE_HORIZONTAL_SEGMENT_WIDTH
import com.patrykandpatrick.vico.core.DefaultDimens.SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.label.InsideSliceLabel
import com.patrykandpatrick.vico.core.chart.pie.label.OutsideSliceLabel
import com.patrykandpatrick.vico.core.chart.pie.label.SliceLabel
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.extension.defaultColors

internal class PieChartStyleHandler(
    private val context: Context,
    attrs: AttributeSet?,
) {

    private val defaultColors: DefaultColors by lazy {
        context.defaultColors
    }

    internal val sliceSpacing: Float

    internal val startAngle: Float

    internal val outerSize: Size.OuterSize

    internal val innerSize: Size.InnerSize

    internal val slices: List<Slice>

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)

        sliceSpacing = typedArray.getSliceSpacing()

        startAngle = typedArray.getFloat(
            R.styleable.PieChartView_startAngle,
            DefaultDimens.PIE_CHART_START_ANGLE,
        )

        outerSize = typedArray.getOuterSize()

        innerSize = typedArray.getInnerSize()

        val genericSliceLabel: SliceLabel? =
            if (typedArray.hasValue(R.styleable.PieChartView_pieSliceLabelStyle)) {
                typedArray.getSliceLabel(resourceId = R.styleable.PieChartView_pieSliceLabelStyle)
            } else {
                null
            }

        slices = typedArray.getSlices(genericSliceLabel)

        typedArray.recycle()
    }

    private fun TypedArray.getSliceSpacing(): Float =
        getRawDimension(
            context = context,
            index = R.styleable.PieChartView_sliceSpacing,
            defaultValue = 0f,
        )

    private fun TypedArray.getOuterSize(): Size.OuterSize {
        val outerSizeValue = getRawDimension(
            context = context,
            index = R.styleable.PieChartView_pieOuterSize,
            defaultValue = -1f,
        )

        return if (outerSizeValue <= 0) {
            Size.OuterSize.fill()
        } else {
            Size.OuterSize.fixed(maxDiameterDp = outerSizeValue)
        }
    }

    private fun TypedArray.getInnerSize(): Size.InnerSize =
        getRawDimension(
            context = context,
            index = R.styleable.PieChartView_pieInnerSize,
            defaultValue = 0f,
        ).let(Size.InnerSize::fixed)

    private fun TypedArray.getSlices(genericSliceLabel: SliceLabel?): List<Slice> {
        return listOf(
            R.styleable.PieChartView_pieSliceStyle1,
            R.styleable.PieChartView_pieSliceStyle2,
            R.styleable.PieChartView_pieSliceStyle3,
            R.styleable.PieChartView_pieSliceStyle4,
            R.styleable.PieChartView_pieSliceStyle5,
            R.styleable.PieChartView_pieSliceStyle6,
            R.styleable.PieChartView_pieSliceStyle7,
            R.styleable.PieChartView_pieSliceStyle8,
        ).mapNotNull { index ->
            if (hasValue(index)) {
                getNestedTypedArray(
                    context = context,
                    resourceId = index,
                    styleableResourceId = R.styleable.PieChartSliceStyle,
                ).getSlice(genericSliceLabel)
            } else {
                null
            }
        }.ifEmpty {
            listOf(
                defaultColors.entity1Color,
                defaultColors.entity2Color,
                defaultColors.entity3Color,
            ).map { color ->
                Slice(color = color.toInt())
            }
        }
    }

    private fun TypedArray.getSliceLabel(
        @StyleableRes resourceId: Int,
    ): SliceLabel {
        val typedArray = getNestedTypedArray(
            context = context,
            resourceId = resourceId,
            styleableResourceId = R.styleable.PieChartSliceLabelStyle,
        )

        val type = typedArray.getInt(
            R.styleable.PieChartSliceLabelStyle_pieSliceLabelType,
            0,
        )

        val textComponent =
            if (typedArray.hasValue(R.styleable.PieChartSliceLabelStyle_pieSliceLabelTextStyle)) {
                typedArray.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.PieChartSliceLabelStyle_pieSliceLabelTextStyle,
                    styleableResourceId = R.styleable.TextComponentStyle,
                ).getTextComponent(context)
            } else {
                textComponent()
            }

        return when (type) {
            SLICE_LABEL_TYPE_INSIDE -> InsideSliceLabel(textComponent = textComponent)
            SLICE_LABEL_TYPE_OUTSIDE -> OutsideSliceLabel(
                textComponent = textComponent,
                lineColor = typedArray.getColor(
                    R.styleable.PieChartSliceLabelStyle_pieSliceLabelLineColor,
                    Color.BLACK,
                ),
                lineWidthDp = typedArray.getRawDimension(
                    context = context,
                    index = R.styleable.PieChartSliceLabelStyle_pieSliceLabelLineWidth,
                    defaultValue = 1f,
                ),
                angledSegmentLengthDp = typedArray.getRawDimension(
                    context = context,
                    index = R.styleable.PieChartSliceLabelStyle_pieSliceLabelAngledSegmentLength,
                    defaultValue = SLICE_ANGLED_SEGMENT_WIDTH,
                ),
                horizontalSegmentWidthDp = typedArray.getRawDimension(
                    context = context,
                    index = R.styleable.PieChartSliceLabelStyle_pieSliceLabelHorizontalSegmentWidth,
                    defaultValue = SLICE_HORIZONTAL_SEGMENT_WIDTH,
                ),
                maxWidthToBoundsRatio = typedArray.getFraction(
                    R.styleable.PieChartSliceLabelStyle_pieSliceLabelMaxWidthToBoundsRatio,
                    SLICE_OUTSIDE_LABEL_MAX_WIDTH_TO_BOUNDS_RATIO,
                ),
            )

            else -> throw IllegalArgumentException("Unknown slice label type: $type")
        }
    }

    private fun TypedArray.getSlice(genericSliceLabel: SliceLabel?): Slice {
        val label = if (hasValue(R.styleable.PieChartSliceStyle_pieSliceLabelStyle)) {
            getSliceLabel(resourceId = R.styleable.PieChartSliceStyle_pieSliceLabelStyle)
        } else {
            genericSliceLabel
        }

        return Slice(
            color = getColor(
                R.styleable.PieChartSliceStyle_sliceColor,
                Color.LTGRAY,
            ),
            strokeWidthDp = getRawDimension(
                context = context,
                index = R.styleable.PieChartSliceStyle_sliceStrokeWidth,
                defaultValue = 0f,
            ),
            strokeColor = getColor(
                R.styleable.PieChartSliceStyle_sliceStrokeColor,
                Color.TRANSPARENT,
            ),
            offsetFromCenterDp = getRawDimension(
                context = context,
                index = R.styleable.PieChartSliceStyle_sliceOffsetFromCenter,
                defaultValue = 0f,
            ),
            label = label,
        )
    }

    private companion object {

        private const val SLICE_LABEL_TYPE_INSIDE = 0
        private const val SLICE_LABEL_TYPE_OUTSIDE = 1
    }
}
