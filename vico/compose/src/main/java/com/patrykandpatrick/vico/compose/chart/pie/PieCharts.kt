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

package com.patrykandpatrick.vico.compose.chart.pie

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice

/**
 * TODO
 */
@Composable
public fun pieChart(
    slices: List<Slice>,
    spacing: Dp = 0.dp,
    outerSize: Size.OuterSize = Size.OuterSize.fill(),
    innerSize: Size.InnerSize = Size.InnerSize.zero(),
    startAngle: Float = DefaultDimens.PIE_CHART_START_ANGLE,
): PieChart = remember {
    PieChart(
        slices = slices,
        spacingDp = spacing.value,
        innerSize = innerSize,
        outerSize = outerSize,
        startAngle = startAngle,
    )
}.apply {
    this.slices = slices
    this.spacingDp = spacing.value
    this.innerSize = innerSize
    this.outerSize = outerSize
    this.startAngle = startAngle
}
