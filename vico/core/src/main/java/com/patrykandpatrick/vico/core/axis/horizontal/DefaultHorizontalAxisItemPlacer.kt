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

package com.patrykandpatrick.vico.core.axis.horizontal

import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.round

internal class DefaultHorizontalAxisItemPlacer(
    private val spacing: Int,
    private val offset: Int,
    private val shiftExtremeTicks: Boolean,
) : AxisItemPlacer.Horizontal {

    override fun getShiftExtremeTicks(context: ChartDrawContext): Boolean = shiftExtremeTicks

    @Suppress("LoopWithTooManyJumpStatements")
    override fun getLabelValues(
        context: ChartDrawContext,
        visibleXRange: ClosedFloatingPointRange<Float>,
        fullXRange: ClosedFloatingPointRange<Float>,
    ): List<Float> {
        val chartValues = context.chartValuesManager.getChartValues()
        val remainder = ((visibleXRange.start - chartValues.minX) / chartValues.xStep - offset) % spacing
        val firstValue = visibleXRange.start + (spacing - remainder) % spacing * chartValues.xStep
        val minXOffset = chartValues.minX % chartValues.xStep
        val values = mutableListOf<Float>()
        var multiplier = -LABEL_OVERFLOW_SIZE
        var hasEndOverflow = false
        while (true) {
            var potentialValue = firstValue + multiplier++ * spacing * chartValues.xStep
            potentialValue = chartValues.xStep * ((potentialValue - minXOffset) / chartValues.xStep).round + minXOffset
            if (potentialValue < chartValues.minX || potentialValue == fullXRange.start) continue
            if (potentialValue > chartValues.maxX || potentialValue == fullXRange.endInclusive) break
            values += potentialValue
            if (potentialValue > visibleXRange.endInclusive && hasEndOverflow.also { hasEndOverflow = true }) break
        }
        return values
    }

    override fun getMeasuredLabelValues(
        context: MeasureContext,
        horizontalDimensions: HorizontalDimensions,
        fullXRange: ClosedFloatingPointRange<Float>,
    ): List<Float> {
        val chartValues = context.chartValuesManager.getChartValues()
        return listOf(chartValues.minX, (chartValues.minX + chartValues.maxX).half, chartValues.maxX)
    }

    @Suppress("LoopWithTooManyJumpStatements")
    override fun getLineValues(
        context: ChartDrawContext,
        visibleXRange: ClosedFloatingPointRange<Float>,
        fullXRange: ClosedFloatingPointRange<Float>,
    ): List<Float>? {
        val chartValues = context.chartValuesManager.getChartValues()
        return when (context.horizontalLayout) {
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
        context: MeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
    ): Float {
        val tickSpace = if (shiftExtremeTicks) tickThickness else tickThickness.half
        return when (context.horizontalLayout) {
            is HorizontalLayout.Segmented -> tickSpace
            is HorizontalLayout.FullWidth -> (tickSpace - horizontalDimensions.unscalableStartPadding).coerceAtLeast(0f)
        }
    }

    override fun getEndHorizontalAxisInset(
        context: MeasureContext,
        horizontalDimensions: HorizontalDimensions,
        tickThickness: Float,
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
