/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.column.ColumnChart.MergeMode
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.extension.defaultColors

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
                resourceId = R.styleable.ColumnChartStyle_column1,
                styleableResourceId = R.styleable.LineComponent,
            ).getLineComponent(
                context = context,
                defaultColor = context.defaultColors.entity1Color.toInt(),
                defaultThickness = DefaultDimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column2,
                styleableResourceId = R.styleable.LineComponent,
            ).getLineComponent(
                context = context,
                defaultColor = context.defaultColors.entity2Color.toInt(),
                defaultThickness = DefaultDimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column3,
                styleableResourceId = R.styleable.LineComponent,
            ).getLineComponent(
                context = context,
                defaultColor = context.defaultColors.entity3Color.toInt(),
                defaultThickness = DefaultDimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
        ),
        spacingDp = getRawDimension(
            context = context,
            index = R.styleable.ColumnChartStyle_columnOuterSpacing,
            defaultValue = DefaultDimens.COLUMN_OUTSIDE_SPACING,
        ),
        innerSpacingDp = getRawDimension(
            context = context,
            index = R.styleable.ColumnChartStyle_columnInnerSpacing,
            defaultValue = DefaultDimens.COLUMN_INSIDE_SPACING,
        ),
        mergeMode = mergeMode,
        dataLabel = if (getBoolean(R.styleable.ColumnChartStyle_showDataLabels, false)) {
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_dataLabelStyle,
                styleableResourceId = R.styleable.TextComponentStyle,
            ).getTextComponent(context = context)
        } else null,
        dataLabelVerticalPosition = getInteger(R.styleable.ColumnChartStyle_dataLabelVerticalPosition, 0).let { value ->
            val values = VerticalPosition.values()
            values[value % values.size]
        },
        dataLabelRotationDegrees = getFloat(
            R.styleable.ColumnChartStyle_dataLabelRotationDegrees,
            0f,
        ),
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
                resourceId = R.styleable.LineChartStyle_line1Spec,
                styleableResourceId = R.styleable.LineSpec,
            ).getLineSpec(
                context = context,
                defaultColor = context.defaultColors.entity1Color.toInt(),
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineChartStyle_line2Spec,
                styleableResourceId = R.styleable.LineSpec,
            ).getLineSpec(
                context = context,
                defaultColor = context.defaultColors.entity2Color.toInt(),
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.LineChartStyle_line3Spec,
                styleableResourceId = R.styleable.LineSpec,
            ).getLineSpec(
                context = context,
                defaultColor = context.defaultColors.entity3Color.toInt(),
            ),
        ),
        spacingDp = getRawDimension(
            context = context,
            index = R.styleable.LineChartStyle_spacing,
            defaultValue = DefaultDimens.POINT_SPACING,
        ),
        pointPosition = getInteger(R.styleable.LineChartStyle_pointPosition, 1).let { value ->
            val values = LineChart.PointPosition.values()
            values[value % values.size]
        },
    )
}
