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

package com.patrykandpatrick.vico.compose.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.entry.ChartEntryModel

/**
 * Holds a chart’s current [ChartEntryModel] ([chartEntryModel]), previous [ChartEntryModel]
 * ([previousChartEntryModel]), and [ChartValuesProvider] ([chartValuesProvider]).
 */
@Immutable
public class ChartEntryModelWrapper<T : ChartEntryModel>(
    public val chartEntryModel: T? = null,
    public val previousChartEntryModel: T? = null,
    public val chartValuesProvider: ChartValuesProvider = ChartValuesProvider.Empty,
)

/**
 * Returns [ChartEntryModelWrapper.chartEntryModel].
 */
public operator fun <T : ChartEntryModel> ChartEntryModelWrapper<T>.component1(): T? = chartEntryModel

/**
 * Returns [ChartEntryModelWrapper.previousChartEntryModel].
 */
public operator fun <T : ChartEntryModel> ChartEntryModelWrapper<T>.component2(): T? = previousChartEntryModel

/**
 * Returns [ChartEntryModelWrapper.chartValuesProvider].
 */
public operator fun <T : ChartEntryModel> ChartEntryModelWrapper<T>.component3(): ChartValuesProvider =
    chartValuesProvider

internal class ChartEntryModelWrapperState<T : ChartEntryModel> : State<ChartEntryModelWrapper<T>> {
    private var previousChartEntryModel: T? = null

    override var value by mutableStateOf<ChartEntryModelWrapper<T>>(ChartEntryModelWrapper())
        private set

    fun set(chartEntryModel: T?, chartValuesProvider: ChartValuesProvider) {
        val currentChartEntryModel = value.chartEntryModel
        if (chartEntryModel?.id != currentChartEntryModel?.id) previousChartEntryModel = currentChartEntryModel
        value = ChartEntryModelWrapper(chartEntryModel, previousChartEntryModel, chartValuesProvider)
    }
}
