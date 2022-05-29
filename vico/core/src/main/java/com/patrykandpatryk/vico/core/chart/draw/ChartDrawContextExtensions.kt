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

import android.graphics.Canvas
import android.graphics.RectF
import com.patrykandpatryk.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.context.DrawContext
import com.patrykandpatryk.vico.core.context.MeasureContext
import com.patrykandpatryk.vico.core.model.Point

private const val DEFAULT_SCALE = 1f

/**
 * The anonymous implementation of [ChartDrawContext].
 *
 * @param canvas the canvas to draw the chart on.
 * @param elevationOverlayColor the color of elevation overlay, applied to components that cast shadows in
 * [com.patrykandpatryk.vico.core.component.shape.ShapeComponent].
 * @param measureContext the measuring context that holds the data used for component measurements.
 * @param markerTouchPoint the point inside the chart’s coordinates where physical touch is occurring.
 * @param segmentProperties holds information about the width of each individual segment on the x-axis.
 * @param chartBounds the bounds in which the [com.patrykandpatryk.vico.core.chart.Chart] will be drawn.
 *
 * @see [com.patrykandpatryk.vico.core.component.shape.ShapeComponent.setShadow]
 */
@LongParameterListDrawFunction
public fun chartDrawContext(
    canvas: Canvas,
    elevationOverlayColor: Int,
    measureContext: MeasureContext,
    markerTouchPoint: Point?,
    segmentProperties: SegmentProperties,
    chartBounds: RectF,
): ChartDrawContext = object : ChartDrawContext, MeasureContext by measureContext {

    override val chartBounds: RectF = chartBounds

    override var canvas: Canvas = canvas

    override val elevationOverlayColor: Long = elevationOverlayColor.toLong()

    override val markerTouchPoint: Point? = markerTouchPoint

    override val chartScale: Float = calculateDrawScale()

    override val segmentProperties: SegmentProperties = segmentProperties.scaled(chartScale)

    override fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit) {
        val originalCanvas = this.canvas
        this.canvas = canvas
        block(this)
        this.canvas = originalCanvas
    }

    private fun calculateDrawScale(): Float =
        if (isHorizontalScrollEnabled) {
            measureContext.chartScale
        } else {
            (
                chartBounds.width() /
                    (segmentProperties.segmentWidth * chartValuesManager.getChartValues().getDrawnEntryCount())
                ).coerceAtMost(DEFAULT_SCALE)
        }
}

internal inline val ChartDrawContext.segmentWidth: Int
    get() = segmentProperties.segmentWidth.pixels.toInt()
