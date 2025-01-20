/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.layout

import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.MutableMeasureContext

/**
 * The anonymous implementation of the [MeasureContext].
 *
 * @param isHorizontalScrollEnabled whether horizontal scrolling is enabled.
 * @param canvasBounds the bounds of the canvas that will be used to draw the chart and its components.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 * @param spToPx converts dimensions from sp to px.
 * @param chartValuesProvider provides the chart’s [ChartValues] instances.
 */
@Composable
public fun getMeasureContext(
    isHorizontalScrollEnabled: Boolean,
    canvasBounds: RectF,
    horizontalLayout: HorizontalLayout,
    chartValuesProvider: ChartValuesProvider,
): MutableMeasureContext = remember {
    MutableMeasureContext(
        canvasBounds = canvasBounds,
        density = 0f,
        isLtr = true,
        spToPx = { 0f },
        chartValuesProvider = chartValuesProvider,
    )
}.apply {
    val density = LocalDensity.current
    this.density = density.density
    this.isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    this.isHorizontalScrollEnabled = isHorizontalScrollEnabled
    this.horizontalLayout = horizontalLayout
    this.spToPx = density.run { { it.sp.toPx() } }
    this.chartValuesProvider = chartValuesProvider
}
