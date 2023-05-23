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

package com.patrykandpatrick.vico.compose.chart.pie.slice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.chart.pie.label.SliceLabel
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader

/**
 * TODO
 */
@Composable
public fun slice(
    color: Color,
    dynamicShader: DynamicShader? = null,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Black,
    offsetFromCenter: Dp = 0.dp,
    label: SliceLabel? = currentChartStyle.pieChart.sliceLabel,
): Slice = remember {
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
