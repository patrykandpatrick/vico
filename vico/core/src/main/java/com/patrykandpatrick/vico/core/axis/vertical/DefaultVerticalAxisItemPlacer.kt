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

package com.patrykandpatrick.vico.core.axis.vertical

import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.draw.CartesianChartDrawContext
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.CartesianMeasureContext
import com.patrykandpatrick.vico.core.extension.half
import kotlin.math.max

internal class DefaultVerticalAxisItemPlacer(
    private val maxItemCount: (ChartValues) -> Int,
    private val shiftTopLines: Boolean,
) : AxisItemPlacer.Vertical {
    override fun getShiftTopLines(chartDrawContext: CartesianChartDrawContext): Boolean = shiftTopLines

    override fun getLabelValues(
        context: CartesianChartDrawContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical,
    ) = getWidthMeasurementLabelValues(context, axisHeight, maxLabelHeight, position)

    override fun getWidthMeasurementLabelValues(
        context: CartesianMeasureContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical,
    ): List<Float> {
        val maxItemCount = context.getMaxItemCount()
        if (maxItemCount == 0) return emptyList()
        val yRange = context.chartValues.getYRange(position)
        return if (yRange.minY * yRange.maxY >= 0) {
            getSimpleLabelValues(maxItemCount, axisHeight, maxLabelHeight, yRange)
        } else {
            getMixedLabelValues(maxItemCount, axisHeight, maxLabelHeight, yRange)
        }
    }

    override fun getHeightMeasurementLabelValues(
        context: CartesianMeasureContext,
        position: AxisPosition.Vertical,
    ): List<Float> {
        val yRange = context.chartValues.getYRange(position)
        return listOf(yRange.minY, (yRange.minY + yRange.maxY).half, yRange.maxY)
    }

    override fun getTopVerticalAxisInset(
        context: CartesianMeasureContext,
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float,
    ) = when {
        context.getMaxItemCount() == 0 -> 0f

        verticalLabelPosition == VerticalAxis.VerticalLabelPosition.Top ->
            maxLabelHeight + (if (shiftTopLines) maxLineThickness else -maxLineThickness).half

        verticalLabelPosition == VerticalAxis.VerticalLabelPosition.Center ->
            (max(maxLabelHeight, maxLineThickness) + if (shiftTopLines) maxLineThickness else -maxLineThickness).half

        else -> if (shiftTopLines) maxLineThickness else 0f
    }

    override fun getBottomVerticalAxisInset(
        context: CartesianMeasureContext,
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float,
    ): Float =
        when {
            context.getMaxItemCount() == 0 -> 0f
            verticalLabelPosition == VerticalAxis.VerticalLabelPosition.Top -> maxLineThickness

            verticalLabelPosition == VerticalAxis.VerticalLabelPosition.Center ->
                (maxOf(maxLabelHeight, maxLineThickness) + maxLineThickness).half

            else -> maxLabelHeight + maxLineThickness.half
        }

    private fun getSimpleLabelValues(
        maxItemCount: Int,
        axisHeight: Float,
        maxLabelHeight: Float,
        yRange: ChartValues.YRange,
    ): List<Float> {
        val values = mutableListOf(yRange.minY)
        if (maxItemCount == 1) return values
        val extraItemCount = (axisHeight / maxLabelHeight).toInt().coerceAtMost(maxItemCount - 1)
        val step = yRange.length / extraItemCount
        repeat(extraItemCount) { values += yRange.minY + (it + 1) * step }
        return values
    }

    private fun getMixedLabelValues(
        maxItemCount: Int,
        axisHeight: Float,
        maxLabelHeight: Float,
        yRange: ChartValues.YRange,
    ): List<Float> {
        val values = mutableListOf(0f)
        if (maxItemCount == 1) return values
        val topHeight = yRange.maxY / yRange.length * axisHeight
        val bottomHeight = -yRange.minY / yRange.length * axisHeight
        val maxTopItemCount = (maxItemCount - 1) * topHeight / axisHeight
        val maxBottomItemCount = (maxItemCount - 1) * bottomHeight / axisHeight
        val topItemCountByHeight = topHeight / maxLabelHeight
        val bottomItemCountByHeight = bottomHeight / maxLabelHeight
        var topItemCount = topItemCountByHeight.coerceAtMost(maxTopItemCount).toInt()
        var bottomItemCount = bottomItemCountByHeight.coerceAtMost(maxBottomItemCount).toInt()
        val currentItemCount = topItemCount + bottomItemCount + 1 // +1 for zero label
        if (currentItemCount < maxItemCount) {
            val isTopNotDenser = topItemCount / topHeight <= bottomItemCount / bottomHeight
            val isTopFillable = topItemCountByHeight - topItemCount >= 1
            val isBottomFillable = bottomItemCountByHeight - bottomItemCount >= 1
            if (isTopFillable && (isTopNotDenser || !isBottomFillable)) {
                topItemCount++
            } else if (isBottomFillable) {
                bottomItemCount++
            }
        }
        if (topItemCount != 0) {
            val step = yRange.maxY / topItemCount
            repeat(topItemCount) { values += (it + 1) * step }
        }
        if (bottomItemCount != 0) {
            val step = yRange.minY / bottomItemCount
            repeat(bottomItemCount) { values += (it + 1) * step }
        }
        return values
    }

    private fun CartesianMeasureContext.getMaxItemCount() =
        maxItemCount(chartValues).also { require(it >= 0) { "`maxItemCount` must return a nonnegative value." } }
}
