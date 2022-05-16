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

package com.patrykandpatryk.vico.core.chart.values

import com.patrykandpatryk.vico.core.axis.AxisPosition
import com.patrykandpatryk.vico.core.axis.AxisRenderer
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.chart.Chart

/**
 * Manages [ChartValues] used in the chart. There may be many [ChartValues], but all of them have the same
 * [ChartValues.minX] and [ChartValues.maxX] values. There is:
 * - Main [ChartValues] used by all components by default. It’s accessible with a null key and always available in
 * drawing phase.
 * - [ChartValues] for [AxisRenderer] at [AxisPosition.Vertical.Start]. It’s available when [Chart] is configured to use
 * [AxisPosition.Vertical.Start] as a key to update and retrieve the [ChartValues].
 * - [ChartValues] for [AxisRenderer] at [AxisPosition.Vertical.End]. It’s available when [Chart] is configured to use
 * [AxisPosition.Vertical.End] as a key to update and retrieve the [ChartValues].
 *
 * @see com.patrykandpatryk.vico.core.chart.column.ColumnChart.targetVerticalAxisPosition
 * @see com.patrykandpatryk.vico.core.chart.line.LineChart.targetVerticalAxisPosition
 */
public class ChartValuesManager {

    private val chartValues: MutableMap<AxisPosition.Vertical?, MutableChartValues> = mutableMapOf()

    /**
     * Returns [ChartValues] associated with given [axisPosition].
     * @param axisPosition if null, the main [ChartValues] is returned. If not null, [ChartValues] associated with
     * given [AxisPosition.Vertical] is returned.
     */
    public fun getChartValues(axisPosition: AxisPosition.Vertical? = null): MutableChartValues =
        chartValues[axisPosition]
            ?.takeIf { it.hasValuesSet }
            ?: chartValues.getOrPut(null) { MutableChartValues() }

    /**
     * Attempts to update stored values by provided params.
     * The [minX] and the [minY] can be updated by a smaller value.
     * The [maxX] and the [maxY] can be updated by a higher value.
     * The [chartEntryModel] is always be updated.
     * If [axisPosition] is null, only the main [ChartValues] are updated. In other case both main [ChartValues]
     * and [ChartValues] associated with given [axisPosition] are updated.
     */
    public fun updateBy(
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float,
        chartEntryModel: ChartEntryModel,
        axisPosition: AxisPosition.Vertical? = null,
    ) {
        chartValues.getOrPut(axisPosition) { MutableChartValues() }
            .updateBy(
                minX = minX,
                maxX = maxX,
                minY = minY,
                maxY = maxY,
                chartEntryModel = chartEntryModel,
            )

        if (axisPosition != null) {
            updateBy(minX, maxX, minY, maxY, chartEntryModel)
        } else {
            val mainValues = getChartValues(null)
            chartValues.forEach { (key, values) ->
                if (key != null) {
                    values.updateBy(minX = mainValues.minX, maxX = mainValues.maxX)
                }
            }
        }
    }

    /**
     * Resets values stored in all [ChartValues] in the [chartValues] map.
     */
    public fun resetChartValues() {
        chartValues.values.forEach { it.reset() }
    }
}
