/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.layout

import android.graphics.RectF
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.axis.AxisManager
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.legend.Legend

/**
 * [VirtualLayout] measures and lays out the components of a chart.
 *
 * @param axisManager the [AxisManager] that manages the associated chart’s axes.
 */
public open class VirtualLayout(
    private val axisManager: AxisManager,
) {

    private val tempInsetters = ArrayList<ChartInsetter>(TEMP_INSETTERS_INITIAL_SIZE)

    private val finalInsets: Insets = Insets()

    private val tempInsets: Insets = Insets()

    /**
     * Measures and sets the bounds for the components of the chart.
     *
     * @param context holds data used for the measuring of components.
     * @param contentBounds the bounds in which the chart should be drawn.
     * @param chart the chart itself.
     * @param legend the legend for the chart.
     * @param segmentProperties the [SegmentProperties] of the chart.
     * @param chartInsetter additional components that influence the chart layout, such as markers.
     *
     * @return the bounds applied to the chart.
     */
    @LongParameterListDrawFunction
    public open fun <Model : ChartEntryModel> setBounds(
        context: MeasureContext,
        contentBounds: RectF,
        chart: Chart<Model>,
        legend: Legend?,
        segmentProperties: SegmentProperties,
        vararg chartInsetter: ChartInsetter?,
    ): RectF = with(context) {

        tempInsetters.clear()
        finalInsets.clear()
        tempInsets.clear()

        val legendHeight = legend?.getHeight(context, contentBounds.width()).orZero

        axisManager.addInsetters(tempInsetters)
        chartInsetter.filterNotNull().forEach(tempInsetters::add)
        tempInsetters.addAll(chart.chartInsetters)
        tempInsetters.add(chart)

        tempInsetters.forEach { insetter ->
            insetter.getInsets(context, tempInsets, segmentProperties)
            finalInsets.setValuesIfGreater(tempInsets)
        }

        val availableHeight = contentBounds.height() - finalInsets.vertical - legendHeight

        tempInsetters.forEach { insetter ->
            insetter.getHorizontalInsets(context, availableHeight, tempInsets)
            finalInsets.setValuesIfGreater(tempInsets)
        }

        val chartBounds = RectF().apply {
            left = contentBounds.left + finalInsets.getLeft(isLtr)
            top = contentBounds.top + finalInsets.top
            right = contentBounds.right - finalInsets.getRight(isLtr)
            bottom = contentBounds.bottom - finalInsets.bottom - legendHeight
        }

        chart.setBounds(
            left = chartBounds.left,
            top = chartBounds.top,
            right = chartBounds.right,
            bottom = chartBounds.bottom,
        )

        axisManager.setAxesBounds(context, contentBounds, chartBounds, finalInsets)

        legend?.setBounds(
            left = contentBounds.left,
            top = chart.bounds.bottom + finalInsets.bottom,
            right = contentBounds.right,
            bottom = chart.bounds.bottom + finalInsets.bottom + legendHeight,
        )

        chartBounds
    }

    private companion object {
        const val TEMP_INSETTERS_INITIAL_SIZE = 5
    }
}
