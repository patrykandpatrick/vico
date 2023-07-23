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
import com.patrykandpatrick.vico.core.chart.Chart

/**
 * Provides a [Chart]’s [ChartValues].
 */
public interface ChartValuesProvider {

    /**
     * Returns the [Chart]’s main [ChartValues].
     */
    public fun getChartValues(): ChartValues

    /**
     * Returns the [ChartValues] associated with the specified [AxisPosition.Vertical] subclass, or `null` if there is
     * no such association.
     */
    public fun getChartValuesForAxisPosition(axisPosition: AxisPosition.Vertical): ChartValues?
}
