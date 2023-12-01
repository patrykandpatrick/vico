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

package com.patrykandpatrick.vico.views.chart.column

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.chart.line.lineSpec
import com.patrykandpatrick.vico.views.theme.getLineComponent
import com.patrykandpatrick.vico.views.theme.getNestedTypedArray
import com.patrykandpatrick.vico.views.theme.getRawDimension
import com.patrykandpatrick.vico.views.theme.use

/**
 * Creates a [LineComponent] using the provided [attrRes] and [styleResId].
 *
 * @param context the context used to retrieve the style information.
 * @param attrRes a theme attribute resource identifier used to retrieve the [LineComponent]’s style.
 * This can be [R.attr.column1], [R.attr.column2], or [R.attr.column3].
 * @param styleResId used to retrieve the style information if the provided [attrRes] cannot be resolved.
 * The [styleResId] must define the style of at least one of the following: [R.attr.column1], [R.attr.column2],
 * and [R.attr.column3].
 *
 * @see R.styleable.LineSpec
 */
public fun columnLineComponent(
    context: Context,
    @AttrRes attrRes: Int = R.attr.column1,
    @StyleRes styleResId: Int = 0,
): LineComponent {
    val tempArray = IntArray(1)

    tempArray[0] = attrRes
    return context.obtainStyledAttributes(null, tempArray, 0, styleResId)
        .use { typedArray ->
            typedArray.getNestedTypedArray(
                context = context,
                resourceId = 0,
                styleableResourceId = R.styleable.LineComponent,
            ).getLineComponent(context, defaultThickness = DefaultDimens.COLUMN_WIDTH)
        }
}

/**
 * Creates a [ColumnChart] using the provided [List] of theme attribute resource identifiers and the given [styleResId].
 *
 * @param context the context used to retrieve the style information.
 * @param styleResId if not 0, used to retrieve the style information from the provided style resource.
 * The [styleResId] should define the style of all of the following: [R.attr.column1], [R.attr.column2],
 * and [R.attr.column3]. If [styleResId] is 0, the style attributes are retrieved from [Context]’s theme.
 *
 * @see lineSpec
 */
public fun columnChart(
    context: Context,
    @StyleRes styleResId: Int = 0,
    mergeMode: ColumnChart.MergeMode = ColumnChart.MergeMode.Grouped,
): ColumnChart {
    val tempArray = IntArray(1)

    val columns =
        listOf(R.attr.column1, R.attr.column2, R.attr.column3)
            .map { themeAttrResItem -> columnLineComponent(context, themeAttrResItem, styleResId) }

    tempArray[0] = R.styleable.ColumnChartStyle_columnOuterSpacing

    val spacingDp =
        context.obtainStyledAttributes(null, tempArray)
            .use { typedArray ->
                typedArray.getRawDimension(
                    context = context,
                    index = R.styleable.ColumnChartStyle_columnOuterSpacing,
                    defaultValue = DefaultDimens.COLUMN_OUTSIDE_SPACING,
                )
            }

    val innerSpacingDp =
        context.obtainStyledAttributes(null, tempArray)
            .use { typedArray ->
                typedArray.getRawDimension(
                    context = context,
                    index = R.styleable.ColumnChartStyle_columnInnerSpacing,
                    defaultValue = DefaultDimens.COLUMN_INSIDE_SPACING,
                )
            }

    return ColumnChart(
        columns = columns,
        spacingDp = spacingDp,
        innerSpacingDp = innerSpacingDp,
        mergeMode = mergeMode,
    )
}
