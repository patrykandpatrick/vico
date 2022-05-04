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

package com.patrykandpatryk.vico.view.chart.line

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.view.R
import com.patrykandpatryk.vico.view.theme.getLineSpec
import com.patrykandpatryk.vico.view.theme.getNestedTypedArray
import com.patrykandpatryk.vico.view.theme.getRawDimension
import com.patrykandpatryk.vico.view.theme.use

/**
 * Creates a [LineChart.LineSpec] using the provided [themeAttrRes] and [styleResId].
 *
 * @param context the context used to retrieve the style information.
 * @param themeAttrRes a theme attributes resource identifier used to retrieve the [LineChart.LineSpec]’s style.
 * Can be [R.attr.line1spec], [R.attr.line2spec], [R.attr.line3spec], or [0].
 * @param styleResId used to retrieve the style information if the provided [themeAttrRes] cannot be resolved.
 * The [styleResId] must define the style of at least one of the following: [R.attr.line1spec], [R.attr.line2spec],
 * and [R.attr.line3spec].
 *
 * @see R.styleable.LineSpec
 */
public fun lineSpec(
    context: Context,
    @AttrRes themeAttrRes: Int = R.attr.line1spec,
    @StyleRes styleResId: Int = 0,
): LineChart.LineSpec {

    val tempArray = IntArray(1)

    tempArray[0] = themeAttrRes
    return context.obtainStyledAttributes(null, tempArray, 0, styleResId)
        .use { typedArray ->
            typedArray.getNestedTypedArray(
                context = context,
                resourceId = 0,
                styleableResourceId = R.styleable.LineSpec,
            ).getLineSpec(context)
        }
}

/**
 * Creates a [LineChart] using the provided [List] of theme attribute resource identifiers and the given [styleResId].
 *
 * @param context the context used to retrieve the style information.
 * @param themeAttrRes a [List] of theme attribute resource identifiers used to retrieve [LineChart.LineSpec] styles.
 * By default it is a list of [R.attr.line1spec], [R.attr.line2spec], and [R.attr.line3spec].
 * @param styleResId used to retrieve the style information if the provided [themeAttrRes] cannot be resolved.
 * The [styleResId] must define the style of at least one of the following: [R.attr.line1spec], [R.attr.line2spec],
 * and [R.attr.line3spec].
 *
 * @see lineSpec
 */
public fun lineChart(
    context: Context,
    @AttrRes themeAttrRes: List<Int> = listOf(R.attr.line1spec, R.attr.line2spec, R.attr.line3spec),
    @StyleRes styleResId: Int = 0,
): LineChart {

    val tempArray = IntArray(1)

    val lineSpecs = themeAttrRes.map { themeAttrResItem ->
        tempArray[0] = themeAttrResItem
        context.obtainStyledAttributes(null, tempArray, 0, styleResId)
            .use { typedArray ->
                typedArray.getNestedTypedArray(
                    context = context,
                    resourceId = 0,
                    styleableResourceId = R.styleable.LineSpec,
                ).getLineSpec(context)
            }
    }

    tempArray[0] = R.styleable.LineChartStyle_spacing
    val spacingDp = context.obtainStyledAttributes(null, tempArray)
        .use { typedArray ->
            typedArray.getRawDimension(
                context = context,
                index = R.styleable.LineChartStyle_spacing,
                defaultValue = DefaultDimens.POINT_SPACING,
            )
        }

    return LineChart(
        lines = lineSpecs,
        spacingDp = spacingDp,
    )
}
