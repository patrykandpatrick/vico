/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.chart.column.ColumnChart
import com.patrykandpatryk.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.view.R
import com.patrykandpatryk.vico.view.extension.defaultColors

internal fun TypedArray.getColumnChart(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.BaseChartView_columnChartStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.ColumnChartStyle,
    mergeMode: MergeMode,
): ColumnChart = getNestedTypedArray(context, resourceId, styleableResourceId).run {
    val defaultShape = Shapes.roundedCornerShape(allPercent = DefaultDimens.COLUMN_ROUNDNESS_PERCENT)
    ColumnChart(
        columns = listOf(
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column1Style,
                styleableResourceId = R.styleable.LineComponentStyle,
            ).getLineComponent(
                context = context,
                defaultColor = context.defaultColors.column1Color.toInt(),
                defaultThickness = DefaultDimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column2Style,
                styleableResourceId = R.styleable.LineComponentStyle,
            ).getLineComponent(
                context = context,
                defaultColor = context.defaultColors.column2Color.toInt(),
                defaultThickness = DefaultDimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column3Style,
                styleableResourceId = R.styleable.LineComponentStyle,
            ).getLineComponent(
                context = context,
                defaultColor = context.defaultColors.column3Color.toInt(),
                defaultThickness = DefaultDimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
        ),
        spacingDp = getRawDimension(
            context = context,
            index = R.styleable.ColumnChartStyle_columnOuterSpacing,
            defaultValue = DefaultDimens.COLUMN_OUTSIDE_SPACING
        ),
        innerSpacingDp = getRawDimension(
            context = context,
            index = R.styleable.ColumnChartStyle_columnInnerSpacing,
            defaultValue = DefaultDimens.COLUMN_INSIDE_SPACING
        ),
        mergeMode = mergeMode
    )
}

internal fun TypedArray.getLineChart(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.BaseChartView_lineChartStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.LineChartStyle,
): LineChart = getNestedTypedArray(context, resourceId, styleableResourceId).run {

    LineChart(
        lines = listOf(
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineChartStyle_line1spec,
                styleableResourceId = R.styleable.LineSpec,
            ).getLineSpec(context),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineChartStyle_line2spec,
                styleableResourceId = R.styleable.LineSpec,
            ).getLineSpec(context),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineChartStyle_line3spec,
                styleableResourceId = R.styleable.LineSpec,
            ).getLineSpec(context),
        ),
        spacingDp = getRawDimension(
            context = context,
            index = R.styleable.LineChartStyle_spacing,
            defaultValue = DefaultDimens.POINT_SPACING,
        ),
    )
}
