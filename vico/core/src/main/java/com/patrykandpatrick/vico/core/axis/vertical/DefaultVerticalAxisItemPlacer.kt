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
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half
import kotlin.math.max

internal class DefaultVerticalAxisItemPlacer(
    private val maxItemCount: Int,
    private val shiftTopLines: Boolean,
) : AxisItemPlacer.Vertical {

    init {
        require(maxItemCount >= 0) { "`maxItemCount` must be nonnegative." }
    }

    override fun getShiftTopLines(chartDrawContext: ChartDrawContext): Boolean = shiftTopLines

    override fun getLabelValues(
        context: ChartDrawContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical,
    ) = getWidthMeasurementLabelValues(context, axisHeight, maxLabelHeight, position)

    public override fun getWidthMeasurementLabelValues(
        context: MeasureContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical,
    ): List<Float> {
        if (maxItemCount == 0) return emptyList()
        val chartValues = context.chartValuesManager.getChartValues(position)
        return if (chartValues.minY * chartValues.maxY >= 0) {
            getSimpleLabelValues(axisHeight, maxLabelHeight, chartValues)
        } else {
            getMixedLabelValues(axisHeight, maxLabelHeight, chartValues)
        }
    }

    public override fun getHeightMeasurementLabelValues(
        context: MeasureContext,
        position: AxisPosition.Vertical,
    ): List<Float> {
        val chartValues = context.chartValuesManager.getChartValues(position)
        return listOf(chartValues.minY, (chartValues.minY + chartValues.maxY).half, chartValues.maxY)
    }

    override fun getTopVerticalAxisInset(
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float,
    ) = when (verticalLabelPosition) {
        VerticalAxis.VerticalLabelPosition.Top ->
            maxLabelHeight + (if (shiftTopLines) maxLineThickness else -maxLineThickness).half

        VerticalAxis.VerticalLabelPosition.Center ->
            (max(maxLabelHeight, maxLineThickness) + if (shiftTopLines) maxLineThickness else -maxLineThickness).half

        VerticalAxis.VerticalLabelPosition.Bottom -> if (shiftTopLines) maxLineThickness else 0f
    }

    override fun getBottomVerticalAxisInset(
        verticalLabelPosition: VerticalAxis.VerticalLabelPosition,
        maxLabelHeight: Float,
        maxLineThickness: Float,
    ): Float = when (verticalLabelPosition) {
        VerticalAxis.VerticalLabelPosition.Top -> maxLineThickness
        VerticalAxis.VerticalLabelPosition.Center -> (maxOf(maxLabelHeight, maxLineThickness) + maxLineThickness).half
        VerticalAxis.VerticalLabelPosition.Bottom -> maxLabelHeight + maxLineThickness.half
    }

    private fun getSimpleLabelValues(axisHeight: Float, maxLabelHeight: Float, chartValues: ChartValues): List<Float> {
        val values = mutableListOf(chartValues.minY)
        if (maxItemCount == 1) return values
        val extraItemCount = (axisHeight / maxLabelHeight).toInt().coerceAtMost(maxItemCount - 1)
        val step = chartValues.lengthY / extraItemCount
        repeat(extraItemCount) { values += (it + 1) * step }
        return values
    }

    private fun getMixedLabelValues(axisHeight: Float, maxLabelHeight: Float, chartValues: ChartValues): List<Float> {
        val values = mutableListOf(0f)
        if (maxItemCount == 1) return values
        val topHeight = chartValues.maxY / chartValues.lengthY * axisHeight
        val bottomHeight = -chartValues.minY / chartValues.lengthY * axisHeight
        val maxTopItemCount = (maxItemCount - 1) * topHeight / axisHeight
        val maxBottomItemCount = (maxItemCount - 1) * bottomHeight / axisHeight
        val topItemCountByHeight = topHeight / maxLabelHeight
        val bottomItemCountByHeight = bottomHeight / maxLabelHeight
        var topItemCount = topItemCountByHeight.coerceAtMost(maxTopItemCount).toInt()
        var bottomItemCount = bottomItemCountByHeight.coerceAtMost(maxBottomItemCount).toInt()
        if (maxTopItemCount % 1f != 0f) {
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
            val step = chartValues.maxY / topItemCount
            repeat(topItemCount) { values += (it + 1) * step }
        }
        if (bottomItemCount != 0) {
            val step = chartValues.minY / bottomItemCount
            repeat(bottomItemCount) { values += (it + 1) * step }
        }
        return values
    }
}
