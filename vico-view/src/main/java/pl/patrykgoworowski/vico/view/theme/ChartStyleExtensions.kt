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
import android.graphics.Color
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders
import pl.patrykgoworowski.vico.core.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.vico.core.constants.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.vico.core.dataset.column.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.line.LineDataSet
import pl.patrykgoworowski.vico.view.R
import pl.patrykgoworowski.vico.view.component.shape.shader.verticalGradient
import pl.patrykgoworowski.vico.view.extension.colors

fun TypedArray.getColumnChart(
    context: Context,
): ColumnDataSet = ColumnDataSet(
    columns = listOf(
        getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ColumnChartStyle_column1Style,
            styleableResourceId = R.styleable.LineComponentStyle,
        ).getLineComponent(context = context),
        getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ColumnChartStyle_column2Style,
            styleableResourceId = R.styleable.LineComponentStyle,
        ).getLineComponent(context = context),
        getNestedTypedArray(
            context = context,
            resourceId = R.styleable.ColumnChartStyle_column3Style,
            styleableResourceId = R.styleable.LineComponentStyle,
        ).getLineComponent(context = context),
    ),
    spacingDp = getRawDimension(
        context = context,
        index = R.styleable.ColumnChartStyle_columnOuterSpacing,
        defaultValue = DEF_MERGED_BAR_SPACING
    ),
    innerSpacingDp = getRawDimension(
        context = context,
        index = R.styleable.ColumnChartStyle_columnInnerSpacing,
        defaultValue = DEF_MERGED_BAR_INNER_SPACING
    ),
)

fun TypedArray.getLineChart(
    context: Context,
): LineDataSet = LineDataSet(
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
    val gradientTopColor = getColor(index = R.styleable.LineChartStyle_gradientTopColor)
    val gradientBottomColor = getColor(index = R.styleable.LineChartStyle_gradientBottomColor)

    if (gradientTopColor != Color.TRANSPARENT || gradientBottomColor != Color.TRANSPARENT) {
        lineBackgroundShader = DynamicShaders.verticalGradient(
            gradientTopColor, gradientBottomColor
        )
    }
}
