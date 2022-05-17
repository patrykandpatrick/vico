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
 * Manages the [ChartValues] used by a chart. There may be many [ChartValues], but all of them have the same
 * [ChartValues.minX] and [ChartValues.maxX] values. The following [ChartValues] instances exist in a chart:
 * - A main [ChartValues] instance, which is used by all components by default. It’s accessible with a null key and
 * always available in the drawing phase.
 * - A [ChartValues] instance for [AxisRenderer]s with [AxisPosition.Vertical.Start]. It’s available when the [Chart]
 * is configured to use [AxisPosition.Vertical.Start] as a key to update and retrieve its [ChartValues].
 * - A [ChartValues] instance for [AxisRenderer]s with [AxisPosition.Vertical.End]. It’s available when the [Chart]
 * is configured to use [AxisPosition.Vertical.End] as a key to update and retrieve its [ChartValues].
 *
 * @see com.patrykandpatryk.vico.core.chart.column.ColumnChart.targetVerticalAxisPosition
 * @see com.patrykandpatryk.vico.core.chart.line.LineChart.targetVerticalAxisPosition
 */
public class ChartValuesManager {

    private val chartValues: MutableMap<AxisPosition.Vertical?, MutableChartValues> = mutableMapOf()

    /**
     * Returns the [ChartValues] associated with the given [axisPosition].
     * @param axisPosition if this is null, the main [ChartValues] instance is returned. Otherwise, the [ChartValues]
     * instance associated with the given [AxisPosition.Vertical] is returned.
     */
    public fun getChartValues(axisPosition: AxisPosition.Vertical? = null): MutableChartValues =
        chartValues[axisPosition]
            ?.takeIf { it.hasValuesSet }
            ?: chartValues.getOrPut(null) { MutableChartValues() }

    /**
     * Attempts to update the stored values to the provided params.
     * [minX] and [minY] can be updated to a lower value.
     * [maxX] and [maxY] can be updated to a higher value.
     * The [chartEntryModel] is always be updated.
     * If [axisPosition] is null, only the main [ChartValues] are updated. Otherwise, both the main [ChartValues]
     * and the [ChartValues] associated with the given [axisPosition] are updated.
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
     * Resets the values stored in all the [ChartValues] instances in the [chartValues] map.
     */
    public fun resetChartValues() {
        chartValues.values.forEach { it.reset() }
    }
}
