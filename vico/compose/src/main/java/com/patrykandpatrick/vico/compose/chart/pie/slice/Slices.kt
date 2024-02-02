/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.chart.pie.slice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.pie.Slice
import com.patrykandpatrick.vico.core.pie.SliceLabel

/**
 * A factory function for [Slice].
 *
 * @param color the color of the [Slice].
 * @param dynamicShader an optional [DynamicShader] to apply to the slice.
 * @param strokeWidth the width of the stroke.
 * @param strokeColor the color of the stroke.
 * @param offsetFromCenter the offset of the slice from the center of the pie chart.
 * @param label the [SliceLabel] to use for the slice.
 */
@Composable
public fun rememberSlice(
    color: Color,
    dynamicShader: DynamicShader? = null,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Black,
    offsetFromCenter: Dp = 0.dp,
    label: SliceLabel? = currentChartStyle.pieChart.sliceLabel,
): Slice =
    remember {
        Slice(
            color = color.toArgb(),
            dynamicShader = dynamicShader,
            strokeWidthDp = strokeWidth.value,
            strokeColor = strokeColor.toArgb(),
            offsetFromCenterDp = offsetFromCenter.value,
            label = label,
        )
    }.apply {
        this.color = color.toArgb()
        this.dynamicShader = dynamicShader
        this.strokeWidthDp = strokeWidth.value
        this.strokeColor = strokeColor.toArgb()
        this.offsetFromCenterDp = offsetFromCenter.value
        this.label = label
    }

/**
 * A factory function for [Slice].
 *
 * @param color the color of the [Slice].
 * @param brush an optional [Brush] to apply to the slice.
 * @param strokeWidth the width of the stroke.
 * @param strokeColor the color of the stroke.
 * @param offsetFromCenter the offset of the slice from the center of the pie chart.
 * @param label the [SliceLabel] to use for the slice.
 */
@Composable
public fun rememberSlice(
    color: Color,
    brush: Brush?,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Black,
    offsetFromCenter: Dp = 0.dp,
    label: SliceLabel? = currentChartStyle.pieChart.sliceLabel,
): Slice =
    rememberSlice(
        color = color,
        dynamicShader = brush?.toDynamicShader(),
        strokeWidth = strokeWidth,
        strokeColor = strokeColor,
        offsetFromCenter = offsetFromCenter,
        label = label,
    )
