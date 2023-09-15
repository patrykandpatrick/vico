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

package com.patrykandpatrick.vico.sample.showcase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class ShowcaseViewModel : ViewModel() {

    private val generator = RandomEntriesGenerator(
        xRange = 0..GENERATOR_X_RANGE_TOP,
        yRange = GENERATOR_Y_RANGE_BOTTOM..GENERATOR_Y_RANGE_TOP,
    )

    private val customStepGenerator = RandomEntriesGenerator(
        xRange = IntProgression.fromClosedRange(rangeStart = 0, rangeEnd = GENERATOR_X_RANGE_TOP, step = 2),
        yRange = GENERATOR_Y_RANGE_BOTTOM..GENERATOR_Y_RANGE_TOP,
    )

    internal val chartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    internal val customStepChartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    internal val multiDataSetChartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    internal val composedChartEntryModelProducer = ComposedChartEntryModelProducer.build()

    var uiSystem by mutableStateOf(UISystem.Compose)
        private set

    init {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                val randomSeries = generator.generateRandomEntries()
                val randomDataSet = List(MULTI_ENTRIES_COMBINED) { generator.generateRandomEntries() }
                chartEntryModelProducer.setEntries(randomSeries)
                multiDataSetChartEntryModelProducer.setEntries(randomDataSet)
                customStepChartEntryModelProducer.setEntries(customStepGenerator.generateRandomEntries())
                composedChartEntryModelProducer.runTransaction {
                    add(randomDataSet)
                    add(randomSeries)
                }
                delay(UPDATE_FREQUENCY)
            }
        }
    }

    fun setUISystem(uiSystem: UISystem) {
        this.uiSystem = uiSystem
    }

    private companion object {
        const val MULTI_ENTRIES_COMBINED = 3
        const val GENERATOR_X_RANGE_TOP = 96
        const val GENERATOR_Y_RANGE_BOTTOM = 2
        const val GENERATOR_Y_RANGE_TOP = 20
        const val UPDATE_FREQUENCY = 2000L
    }
}
