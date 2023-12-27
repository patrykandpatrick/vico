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

package com.patrykandpatrick.vico.core.chart.draw

import android.graphics.RectF
import com.patrykandpatrick.vico.core.DEF_MAX_ZOOM
import com.patrykandpatrick.vico.core.chart.CartesianChart
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.context.CartesianDrawContext
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.util.Point

/**
 * An extension of [CartesianDrawContext] that holds additional data required to render a [CartesianChart].
 */
public interface CartesianChartDrawContext : CartesianDrawContext {
    /**
     * The bounds in which the [CartesianChart] will be drawn.
     */
    public val chartBounds: RectF

    /**
     * Holds information on the [CartesianChart]’s horizontal dimensions.
     */
    public val horizontalDimensions: HorizontalDimensions

    /**
     * The point inside the chart’s coordinates where physical touch is occurring.
     */
    public val markerTouchPoint: Point?

    /**
     * The current amount of horizontal scroll.
     */
    public val horizontalScroll: Float

    /**
     * The zoom factor.
     */
    public val zoom: Float
}

/**
 * Returns the maximum scroll distance.
 */
public fun CartesianMeasureContext.getMaxScrollDistance(
    chartWidth: Float,
    horizontalDimensions: HorizontalDimensions,
    zoom: Float? = null,
): Float {
    val contentWidth =
        horizontalDimensions
            .run { if (zoom != null) scaled(zoom) else this }
            .getContentWidth(chartValues.getMaxMajorEntryCount())

    return (layoutDirectionMultiplier * (contentWidth - chartWidth)).run {
        if (isLtr) coerceAtLeast(minimumValue = 0f) else coerceAtMost(maximumValue = 0f)
    }
}

/**
 * Returns the maximum scroll distance.
 */
public fun CartesianChartDrawContext.getMaxScrollDistance(): Float =
    getMaxScrollDistance(
        chartWidth = chartBounds.width(),
        horizontalDimensions = horizontalDimensions,
    )

/**
 * Returns the automatic zoom factor for a chart.
 */
public fun CartesianMeasureContext.getAutoZoom(
    horizontalDimensions: HorizontalDimensions,
    chartBounds: RectF,
    autoScaleUp: AutoScaleUp,
): Float {
    val scalableContentWidth =
        horizontalDimensions.getScalableContentWidth(chartValues.getMaxMajorEntryCount())
    val reducedChartWidth = chartBounds.width() - horizontalDimensions.unscalablePadding
    val fillingZoom = reducedChartWidth / scalableContentWidth
    return when {
        scalableContentWidth < reducedChartWidth ->
            if (autoScaleUp == AutoScaleUp.Full) fillingZoom.coerceAtMost(DEF_MAX_ZOOM) else 1f

        !isHorizontalScrollEnabled -> fillingZoom
        else -> 1f
    }
}
