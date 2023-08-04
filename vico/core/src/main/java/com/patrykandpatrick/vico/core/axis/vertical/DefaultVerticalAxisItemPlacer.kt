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
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half
import kotlin.math.max

internal class DefaultVerticalAxisItemPlacer(
    private val maxItemCount: Int,
    private val shiftTopLines: Boolean,
) : AxisItemPlacer.Vertical {

    override fun getShiftTopLines(chartDrawContext: ChartDrawContext): Boolean = shiftTopLines

    override fun getLabelValues(
        context: ChartDrawContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical,
    ): List<Float> = getWidthMeasurementLabelValues(context, axisHeight, maxLabelHeight, position)

    public override fun getWidthMeasurementLabelValues(
        context: MeasureContext,
        axisHeight: Float,
        maxLabelHeight: Float,
        position: AxisPosition.Vertical,
    ): List<Float> {
        val itemCount = ((axisHeight / maxLabelHeight).toInt() + 1).coerceAtMost(maxItemCount)
        val chartValues = context.chartValuesManager.getChartValues(position)
        val step = chartValues.lengthY / (itemCount - 1)
        return List(itemCount) { chartValues.minY + it * step }
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
    ): Float {
        return when (verticalLabelPosition) {
            VerticalAxis.VerticalLabelPosition.Top ->
                maxLabelHeight + (if (shiftTopLines) maxLineThickness else -maxLineThickness).half

            VerticalAxis.VerticalLabelPosition.Center ->
                (max(maxLabelHeight, maxLineThickness) + if (shiftTopLines) maxLineThickness else -maxLineThickness)
                    .half

            VerticalAxis.VerticalLabelPosition.Bottom -> if (shiftTopLines) maxLineThickness else 0f
        }
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
}
