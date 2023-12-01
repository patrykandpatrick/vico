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

package com.patrykandpatrick.vico.core.chart.values

import com.patrykandpatrick.vico.core.axis.AxisPosition

/**
 * Provides a chart’s [ChartValues] instances.
 */
public interface ChartValuesProvider {
    /**
     * Returns the [ChartValues] instance associated with the specified [AxisPosition.Vertical] subclass. If
     * [axisPosition] is `null`, the chart’s main [ChartValues] instance is returned.
     */
    public fun getChartValues(axisPosition: AxisPosition.Vertical? = null): ChartValues

    /**
     * An empty [ChartValuesProvider] implementation. [getChartValues] throws an exception when called.
     */
    public companion object Empty : ChartValuesProvider {
        override fun getChartValues(axisPosition: AxisPosition.Vertical?): ChartValues =
            error("`ChartValuesProvider.Empty#getChartValues` shouldn’t be used.")
    }
}
