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

package com.patrykandpatryk.vico.core.layout

import android.graphics.RectF
import com.patrykandpatryk.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatryk.vico.core.axis.AxisManager
import com.patrykandpatryk.vico.core.axis.model.ChartModel
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.chart.insets.ChartInsetter
import com.patrykandpatryk.vico.core.chart.insets.Insets
import com.patrykandpatryk.vico.core.chart.Chart
import kotlin.math.max
import com.patrykandpatryk.vico.core.context.MeasureContext

/**
 * [VirtualLayout] measures and lays out the chart, the axis, and other components (such as markers).
 */
public open class VirtualLayout {

    private val tempInsetters = ArrayList<ChartInsetter>(TEMP_INSETTERS_INITIAL_SIZE)
    private val finalInsets: Insets = Insets()
    private val tempInsets: Insets = Insets()

    /**
     * Measures and sets the bounds for the chart, the axes, and other components.
     *
     * @param context the measuring context that holds the data used for component measurements.
     * @param contentBounds the bounds in which the chart contents must be drawn.
     * @param chart the actual chart.
     * @param chartModel the model used by the chart. This holds information about the values on both the y-axis and
     * the x-axis.
     * @param axisManager the [AxisManager] that manages this chart’s axes.
     * @param chartInsetter additional components that influence the chart layout, such as markers.
     */
    @LongParameterListDrawFunction
    public open fun <Model : ChartEntryModel> setBounds(
        context: MeasureContext,
        contentBounds: RectF,
        chart: Chart<Model>,
        chartModel: ChartModel,
        axisManager: AxisManager,
        vararg chartInsetter: ChartInsetter?,
    ): Unit = with(context) {
        tempInsetters.clear()
        finalInsets.clear()
        tempInsets.clear()
        axisManager.addInsetters(tempInsetters)
        chartInsetter.filterNotNull().forEach(tempInsetters::add)

        tempInsetters.forEach { insetter ->
            insetter.getInsets(context, tempInsets)
            finalInsets.setAllGreater(tempInsets)
        }

        val availableHeight = contentBounds.height() - finalInsets.vertical

        tempInsetters.forEach { insetter ->
            insetter.getHorizontalInsets(context, availableHeight, tempInsets)
            finalInsets.setAllGreater(tempInsets)
        }

        chart.setBounds(
            left = contentBounds.left + finalInsets.getLeft(isLtr),
            top = contentBounds.top + finalInsets.top,
            right = contentBounds.right - finalInsets.getRight(isLtr),
            bottom = contentBounds.bottom - finalInsets.bottom
        )
        axisManager.setAxesBounds(context, contentBounds, finalInsets)
    }

    private fun Insets.setAllGreater(other: Insets) {
        start = max(start, other.start)
        top = max(top, other.top)
        end = max(end, other.end)
        bottom = max(bottom, other.bottom)
    }

    private companion object {
        const val TEMP_INSETTERS_INITIAL_SIZE = 5
    }
}
