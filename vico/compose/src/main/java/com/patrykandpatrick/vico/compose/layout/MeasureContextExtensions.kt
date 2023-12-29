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

package com.patrykandpatrick.vico.compose.layout

import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.context.MutableCartesianMeasureContext
import com.patrykandpatrick.vico.core.context.MutablePreMeasureContext
import com.patrykandpatrick.vico.core.context.PreMeasureContext
import com.patrykandpatrick.vico.core.extension.spToPx
import com.patrykandpatrick.vico.core.model.CartesianChartModel

/**
 * Creates the [PreMeasureContext].
 */
@Composable
public fun getPreMeasureContext(): PreMeasureContext {
    val density = LocalDensity.current.density
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val context = LocalContext.current
    val spToPx = context::spToPx
    return remember(density, isLtr, spToPx(1f)) {
        MutablePreMeasureContext(
            density = density,
            isLtr = isLtr,
            spToPx = spToPx,
        )
    }
}

/**
 * The anonymous implementation of the [CartesianMeasureContext].
 *
 * @param isHorizontalScrollEnabled whether horizontal scrolling is enabled.
 * @param canvasBounds the bounds of the canvas that will be used to draw the chart and its components.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 * @param spToPx converts dimensions from sp to px.
 * @param chartValues houses the [CartesianChart]’s [CartesianChartModel] and _x_ and _y_ ranges.
 */
@Composable
public fun getCartesianMeasureContext(
    isHorizontalScrollEnabled: Boolean,
    canvasBounds: RectF,
    horizontalLayout: HorizontalLayout,
    spToPx: (Float) -> Float,
    chartValues: ChartValues,
): MutableCartesianMeasureContext =
    remember {
        MutableCartesianMeasureContext(
            canvasBounds = canvasBounds,
            density = 0f,
            isLtr = true,
            spToPx = spToPx,
            chartValues = chartValues,
        )
    }.apply {
        this.density = LocalDensity.current.density
        this.isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        this.isHorizontalScrollEnabled = isHorizontalScrollEnabled
        this.horizontalLayout = horizontalLayout
        this.spToPx = spToPx
        this.chartValues = chartValues
    }
