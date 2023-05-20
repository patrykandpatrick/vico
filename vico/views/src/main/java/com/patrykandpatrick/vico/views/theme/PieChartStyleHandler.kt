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
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
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

        startAngle = typedArray.getFloat(R.styleable.PieChartView_startAngle, 0f)

        outerSize = typedArray.getOuterSize()

        innerSize = typedArray.getInnerSize()

        slices = typedArray.getSlices()

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

    private fun TypedArray.getSlices(): List<Slice> {
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
                ).getSlice(index)
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

    private fun TypedArray.getSlice(index: Int): Slice {
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
        )
    }
}
