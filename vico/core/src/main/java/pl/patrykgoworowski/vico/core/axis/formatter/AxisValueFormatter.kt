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

package pl.patrykgoworowski.vico.core.axis.formatter

import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.model.ChartModel

/**
 * Formats the values displayed along chart axes.
 */
public fun interface AxisValueFormatter<Position : AxisPosition> {

    /**
     * Called by [pl.patrykgoworowski.vico.core.axis.AxisRenderer] subclasses in order to format either y-axis
     * or x-axis values for display.
     *
     * @param value The value to be formatted. This is either an x-axis value or a y-axis value.
     * @param chartModel The model used by the chart. This holds information on the values along the x-axis and y-axis.
     *
     * @see ChartModel
     *
     * @return a formatted value that will be displayed on the axis.
     */
    public fun formatValue(
        value: Float,
        chartModel: ChartModel,
    ): String
}
