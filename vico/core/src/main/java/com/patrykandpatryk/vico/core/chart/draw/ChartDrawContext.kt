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

package com.patrykandpatryk.vico.core.chart.draw

import android.graphics.RectF
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.context.DrawContext
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.model.Point

/**
 * An extension of [DrawContext] that holds additional data required to render the chart.
 */
public interface ChartDrawContext : DrawContext {

    /**
     * The bounds in which the [com.patrykandpatryk.vico.core.chart.Chart] will be drawn.
     */
    public val chartBounds: RectF

    /**
     * Holds information about the width of each individual segment on the x-axis.
     */
    public val segmentProperties: SegmentProperties

    /**
     * The point inside the chart’s coordinates where physical touch is occurring.
     */
    public val markerTouchPoint: Point?

    /**
     * The current amount of horizontal scroll.
     */
    public val horizontalScroll: Float

    /**
     * Returns the maximum horizontal scroll value.
     */
    public val maxScrollDistance: Float
        get() {
            val chartWidth = chartBounds.width()
            val cumulatedSegmentWidth = segmentProperties.segmentWidth *
                chartValuesManager.getChartValues().getDrawnEntryCount()

            return (layoutDirectionMultiplier * (cumulatedSegmentWidth - chartWidth)).run {
                if (isLtr) coerceAtLeast(minimumValue = 0f) else coerceAtMost(maximumValue = 0f)
            }
        }
}

public fun MeasureContext.getMaxScrollDistance(
    chartWidth: Float,
    segmentProperties: SegmentProperties,
): Float {
    val cumulatedSegmentWidth = segmentProperties.segmentWidth *
        chartValuesManager.getChartValues().getDrawnEntryCount()

    return (layoutDirectionMultiplier * (cumulatedSegmentWidth - chartWidth)).run {
        if (isLtr) coerceAtLeast(minimumValue = 0f) else coerceAtMost(maximumValue = 0f)
    }
}
