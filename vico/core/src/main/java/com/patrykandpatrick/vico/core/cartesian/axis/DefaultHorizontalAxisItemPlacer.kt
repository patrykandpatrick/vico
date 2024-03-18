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

package com.patrykandpatrick.vico.core.cartesian.axis

import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.cartesian.values.ChartValues
import com.patrykandpatrick.vico.core.common.extension.ceil
import com.patrykandpatrick.vico.core.common.extension.half
import com.patrykandpatrick.vico.core.common.extension.round

internal class DefaultHorizontalAxisItemPlacer(
    private val spacing: Int,
    private val offset: Int,
    private val shiftExtremeTicks: Boolean,
    private val addExtremeLabelPadding: Boolean,
) : AxisItemPlacer.Horizontal {
    private val CartesianMeasureContext.addExtremeLabelPadding
        get() =
            this@DefaultHorizontalAxisItemPlacer.addExtremeLabelPadding &&
                horizontalLayout is HorizontalLayout.FullWidth

    private val ChartValues.measuredLabelValues get() = listOf(minX, (minX + maxX).half, maxX)

    override fun getShiftExtremeTicks(context: CartesianChartDrawContext): Boolean = shiftExtremeTicks

    override fun getFirstLabelValue(
        context: CartesianMeasureContext,
        maxLabelWidth: Float,
    ) = if (context.addExtremeLabelPadding) context.chartValues.minX + offset * context.chartValues.xStep else null

    override fun getLastLabelValue(
        context: CartesianMeasureContext,
        maxLabelWidth: Float,
    ) = if (context.addExtremeLabelPadding) {
        with(context.chartValues) { maxX - (xLength - xStep * offset) % (xStep * spacing) }
    } else {
        null
    }

    override fun getLabelValues(
        context: CartesianChartDrawContext,
        visibleXRange: ClosedFloatingPointRange<Float>,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float,
    ): List<Float> {
        with(context) {
            val dynamicSpacing =
                spacing *
                    if (this.addExtremeLabelPadding) {
                        (maxLabelWidth / (horizontalDimensions.xSpacing * spacing)).ceil.toInt()
                    } else {
                        1
                    }
            val remainder = ((visibleXRange.start - chartValues.minX) / chartValues.xStep - offset) % dynamicSpacing
            val firstValue = visibleXRange.start + (dynamicSpacing - remainder) % dynamicSpacing * chartValues.xStep
            val minXOffset = chartValues.minX % chartValues.xStep
            val values = mutableListOf<Float>()
            var multiplier = -LABEL_OVERFLOW_SIZE
            var hasEndOverflow = false
            while (true) {
                var potentialValue = firstValue + multiplier++ * dynamicSpacing * chartValues.xStep
                potentialValue = chartValues.xStep * ((potentialValue - minXOffset) / chartValues.xStep).round +
                    minXOffset
                if (potentialValue < chartValues.minX || potentialValue == fullXRange.start) continue
                if (potentialValue > chartValues.maxX || potentialValue == fullXRange.endInclusive) break
                values += potentialValue
                if (potentialValue > visibleXRange.endInclusive && hasEndOverflow.also { hasEndOverflow = true }) break
            }
            return values
        }
    }

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
    ) = if (context.addExtremeLabelPadding) context.chartValues.measuredLabelValues else emptyList()

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float,
    ) = context.chartValues.measuredLabelValues

    override fun getLineValues(
        context: CartesianChartDrawContext,
        visibleXRange: ClosedFloatingPointRange<Float>,
        fullXRange: ClosedFloatingPointRange<Float>,
        maxLabelWidth: Float,
    ): List<Float>? =
        with(context) {
            when (horizontalLayout) {
                is HorizontalLayout.Segmented -> {
                    val remainder = (visibleXRange.start - fullXRange.start) % chartValues.xStep
                    val firstValue = visibleXRange.start + (chartValues.xStep - remainder) % chartValues.xStep
                    var multiplier = -TICK_OVERFLOW_SIZE
                    val values = mutableListOf<Float>()
                    while (true) {
                        val potentialValue = firstValue + multiplier++ * chartValues.xStep
                        if (potentialValue < fullXRange.start) continue
                        if (potentialValue > fullXRange.endInclusive) break
                        values += potentialValue
                        if (potentialValue > visibleXRange.endInclusive) break
                    }
                    values
                }

                is HorizontalLayout.FullWidth -> null
            }
        }

    override fun getStartHorizontalAxisInset(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float {
        val tickSpace = if (shiftExtremeTicks) tickThickness else tickThickness.half
        return when (context.horizontalLayout) {
            is HorizontalLayout.Segmented -> tickSpace
            is HorizontalLayout.FullWidth -> (tickSpace - horizontalDimensions.unscalableStartPadding).coerceAtLeast(0f)
        }
    }

    override fun getEndHorizontalAxisInset(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
        maxLabelWidth: Float,
    ): Float {
        val tickSpace = if (shiftExtremeTicks) tickThickness else tickThickness.half
        return when (context.horizontalLayout) {
            is HorizontalLayout.Segmented -> tickSpace
            is HorizontalLayout.FullWidth -> (tickSpace - horizontalDimensions.unscalableEndPadding).coerceAtLeast(0f)
        }
    }

    private companion object {
        const val LABEL_OVERFLOW_SIZE = 2
        const val TICK_OVERFLOW_SIZE = 1
    }
}
