/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import pl.patrykgoworowski.vico.core.Alpha
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders
import pl.patrykgoworowski.vico.core.chart.column.ColumnChart
import pl.patrykgoworowski.vico.core.chart.column.MergeMode
import pl.patrykgoworowski.vico.core.chart.line.LineChart
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.view.R
import pl.patrykgoworowski.vico.view.component.shape.shader.verticalGradient
import pl.patrykgoworowski.vico.view.extension.colors

internal fun TypedArray.getColumnChart(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.BaseChartView_columnChartStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.ColumnChartStyle,
): ColumnChart = getNestedTypedArray(context, resourceId, styleableResourceId).run {
    val defaultShape = Shapes.roundedCornersShape(allPercent = Dimens.COLUMN_ROUNDNESS_PERCENT)
    ColumnChart(
        columns = listOf(
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column1Style,
                styleableResourceId = R.styleable.LineComponentStyle,
            ).getLineComponent(
                context = context,
                defaultColor = context.colors.column1Color.toInt(),
                defaultThickness = Dimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column2Style,
                styleableResourceId = R.styleable.LineComponentStyle,
            ).getLineComponent(
                context = context,
                defaultColor = context.colors.column2Color.toInt(),
                defaultThickness = Dimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.ColumnChartStyle_column3Style,
                styleableResourceId = R.styleable.LineComponentStyle,
            ).getLineComponent(
                context = context,
                defaultColor = context.colors.column3Color.toInt(),
                defaultThickness = Dimens.COLUMN_WIDTH,
                defaultShape = defaultShape,
            ),
        ),
        spacingDp = getRawDimension(
            context = context,
            index = R.styleable.ColumnChartStyle_columnOuterSpacing,
            defaultValue = Dimens.COLUMN_OUTSIDE_SPACING
        ),
        innerSpacingDp = getRawDimension(
            context = context,
            index = R.styleable.ColumnChartStyle_columnInnerSpacing,
            defaultValue = Dimens.COLUMN_INSIDE_SPACING
        ),
        mergeMode = getInt(R.styleable.ColumnChartStyle_mergeMode, 0).let { int ->
            MergeMode.values()[int]
        }
    )
}

internal fun TypedArray.getLineChart(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.BaseChartView_lineChartStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.LineChartStyle,
): LineChart = getNestedTypedArray(context, resourceId, styleableResourceId).run {
    LineChart(
        lineColor = getColor(
            index = R.styleable.LineChartStyle_color,
            defaultColor = context.colors.lineColor.toInt(),
        ),
        point = getNestedTypedArray(
            context = context,
            resourceId = R.styleable.LineChartStyle_pointStyle,
            styleableResourceId = R.styleable.ComponentStyle,
        ).getComponent(context),
        pointSizeDp = getRawDimension(
            context = context,
            index = R.styleable.LineChartStyle_pointSize,
            defaultValue = Dimens.POINT_SIZE,
        ),
        spacingDp = getRawDimension(
            context = context,
            index = R.styleable.LineChartStyle_spacing,
            defaultValue = Dimens.POINT_SPACING,
        ),
        lineThicknessDp = getRawDimension(
            context = context,
            index = R.styleable.LineChartStyle_lineThickness,
            defaultValue = Dimens.LINE_THICKNESS,
        ),
    ).apply {
        cubicStrength = getFraction(
            index = R.styleable.LineChartStyle_cubicStrength,
            defaultValue = Dimens.CUBIC_STRENGTH
        )
        if (
            hasValue(R.styleable.LineChartStyle_gradientTopColor) ||
            hasValue(R.styleable.LineChartStyle_gradientBottomColor)
        ) {
            val gradientTopColor = getColor(R.styleable.LineChartStyle_gradientTopColor)
            val gradientBottomColor = getColor(R.styleable.LineChartStyle_gradientBottomColor)

            lineBackgroundShader = DynamicShaders.verticalGradient(
                gradientTopColor, gradientBottomColor
            )
        } else {
            lineBackgroundShader = DynamicShaders.verticalGradient(
                lineColor.copyColor(alpha = Alpha.LINE_BACKGROUND_SHADER_START),
                lineColor.copyColor(alpha = Alpha.LINE_BACKGROUND_SHADER_END),
            )
        }
    }
}
