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
import androidx.annotation.StyleableRes
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.chart.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.extension.defaultColors

internal fun TypedArray.getColumnCartesianLayer(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.CartesianChartView_columnLayerStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.ColumnCartesianLayerStyle,
): ColumnCartesianLayer =
    getNestedTypedArray(context, resourceId, styleableResourceId).run {
        val defaultShape = Shapes.roundedCornerShape(allPercent = DefaultDimens.COLUMN_ROUNDNESS_PERCENT)
        ColumnCartesianLayer(
            columns =
                listOf(
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_column1,
                        styleableResourceId = R.styleable.LineComponent,
                    ).getLineComponent(
                        context = context,
                        defaultColor = context.defaultColors.entity1Color.toInt(),
                        defaultThickness = DefaultDimens.COLUMN_WIDTH,
                        defaultShape = defaultShape,
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_column2,
                        styleableResourceId = R.styleable.LineComponent,
                    ).getLineComponent(
                        context = context,
                        defaultColor = context.defaultColors.entity2Color.toInt(),
                        defaultThickness = DefaultDimens.COLUMN_WIDTH,
                        defaultShape = defaultShape,
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_column3,
                        styleableResourceId = R.styleable.LineComponent,
                    ).getLineComponent(
                        context = context,
                        defaultColor = context.defaultColors.entity3Color.toInt(),
                        defaultThickness = DefaultDimens.COLUMN_WIDTH,
                        defaultShape = defaultShape,
                    ),
                ),
            spacingDp =
                getRawDimension(
                    context = context,
                    index = R.styleable.ColumnCartesianLayerStyle_columnOuterSpacing,
                    defaultValue = DefaultDimens.COLUMN_OUTSIDE_SPACING,
                ),
            innerSpacingDp =
                getRawDimension(
                    context = context,
                    index = R.styleable.ColumnCartesianLayerStyle_columnInnerSpacing,
                    defaultValue = DefaultDimens.COLUMN_INSIDE_SPACING,
                ),
            mergeMode =
                getInteger(R.styleable.ColumnCartesianLayerStyle_mergeMode, 0)
                    .let(ColumnCartesianLayer.MergeMode.entries::get),
            dataLabel =
                if (getBoolean(R.styleable.ColumnCartesianLayerStyle_showDataLabels, false)) {
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.ColumnCartesianLayerStyle_dataLabelStyle,
                        styleableResourceId = R.styleable.TextComponentStyle,
                    ).getTextComponent(context = context)
                } else {
                    null
                },
            dataLabelVerticalPosition =
                getInteger(R.styleable.ColumnCartesianLayerStyle_dataLabelVerticalPosition, 0)
                    .let { value ->
                        val values = VerticalPosition.entries
                        values[value % values.size]
                    },
            dataLabelRotationDegrees =
                getFloat(
                    R.styleable.ColumnCartesianLayerStyle_dataLabelRotationDegrees,
                    0f,
                ),
        )
    }

internal fun TypedArray.getLineCartesianLayer(
    context: Context,
    @StyleableRes resourceId: Int = R.styleable.CartesianChartView_lineLayerStyle,
    @StyleableRes styleableResourceId: IntArray = R.styleable.LineCartesianLayerStyle,
): LineCartesianLayer =
    getNestedTypedArray(context, resourceId, styleableResourceId).run {
        LineCartesianLayer(
            lines =
                listOf(
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.LineCartesianLayerStyle_line1Spec,
                        styleableResourceId = R.styleable.LineSpec,
                    ).getLineSpec(
                        context = context,
                        defaultColor = context.defaultColors.entity1Color.toInt(),
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.LineCartesianLayerStyle_line2Spec,
                        styleableResourceId = R.styleable.LineSpec,
                    ).getLineSpec(
                        context = context,
                        defaultColor = context.defaultColors.entity2Color.toInt(),
                    ),
                    getNestedTypedArray(
                        context = context,
                        resourceId = R.styleable.LineCartesianLayerStyle_line3Spec,
                        styleableResourceId = R.styleable.LineSpec,
                    ).getLineSpec(
                        context = context,
                        defaultColor = context.defaultColors.entity3Color.toInt(),
                    ),
                ),
            spacingDp =
                getRawDimension(
                    context = context,
                    index = R.styleable.LineCartesianLayerStyle_spacing,
                    defaultValue = DefaultDimens.POINT_SPACING,
                ),
        )
    }
