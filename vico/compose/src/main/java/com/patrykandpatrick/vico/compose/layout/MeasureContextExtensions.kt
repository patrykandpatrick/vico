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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.context.MutableCartesianMeasureContext
import com.patrykandpatrick.vico.core.context.MutableMeasureContext

/**
 * The anonymous implementation of the [CartesianMeasureContext].
 *
 * @param isHorizontalScrollEnabled whether horizontal scrolling is enabled.
 * @param chartScale the scale of the chart. Used to handle zooming in and out.
 * @param canvasBounds the bounds of the canvas that will be used to draw the chart and its components.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 */
@Composable
public fun getMeasureContext(
    canvasBounds: RectF,
): MutableMeasureContext = remember {
    MutableMeasureContext(
        canvasBounds = canvasBounds,
        density = 0f,
        fontScale = 0f,
        isLtr = true,
    )
}.apply {
    density = getDensity()
    fontScale = getFontScale()
    isLtr = getIsLtr()
}

/**
 * The anonymous implementation of the [CartesianMeasureContext].
 *
 * @param isHorizontalScrollEnabled whether horizontal scrolling is enabled.
 * @param chartScale the scale of the chart. Used to handle zooming in and out.
 * @param canvasBounds the bounds of the canvas that will be used to draw the chart and its components.
 * @param horizontalLayout defines how the chart’s content is positioned horizontally.
 */
@Composable
public fun getCartesianMeasureContext(
    isHorizontalScrollEnabled: Boolean,
    chartScale: Float,
    canvasBounds: RectF,
    horizontalLayout: HorizontalLayout,
): MutableCartesianMeasureContext = remember {
    MutableCartesianMeasureContext(
        canvasBounds = canvasBounds,
        density = 0f,
        fontScale = 0f,
        isLtr = true,
        isHorizontalScrollEnabled = isHorizontalScrollEnabled,
        chartScale = chartScale,
        horizontalLayout = horizontalLayout,
    )
}.apply {
    density = getDensity()
    fontScale = getFontScale()
    isLtr = getIsLtr()
    this.isHorizontalScrollEnabled = isHorizontalScrollEnabled
    this.chartScale = chartScale
}

@Composable
internal fun getDensity(): Float = LocalDensity.current.density

@Composable
internal fun getFontScale(): Float = LocalDensity.current.fontScale * LocalDensity.current.density

@Composable
internal fun getIsLtr(): Boolean = LocalLayoutDirection.current == LayoutDirection.Ltr
