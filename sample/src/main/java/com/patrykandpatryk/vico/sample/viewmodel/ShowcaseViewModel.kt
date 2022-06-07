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

package com.patrykandpatryk.vico.sample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.composed.plus
import com.patrykandpatryk.vico.core.util.RandomEntriesGenerator
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class ShowcaseViewModel : ViewModel() {

    private val generator = RandomEntriesGenerator(
        xRange = 0..GENERATOR_X_RANGE_TOP,
        yRange = GENERATOR_Y_RANGE_BOTTOM..GENERATOR_Y_RANGE_TOP,
    )

    private val multiGenerator = RandomEntriesGenerator(
        xRange = 0..MULTI_GENERATOR_X_RANGE_TOP,
        yRange = GENERATOR_Y_RANGE_BOTTOM..GENERATOR_Y_RANGE_TOP,
    )

    internal val chartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    internal val multiChartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()

    internal val composedChartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel> =
        multiChartEntryModelProducer + chartEntryModelProducer

    init {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                chartEntryModelProducer.setEntries(generator.generateRandomEntries())
                multiChartEntryModelProducer.setEntries(
                    entries = List(size = MULTI_ENTRIES_COMBINED) {
                        multiGenerator.generateRandomEntries()
                    },
                )
                delay(UPDATE_FREQUENCY)
            }
        }
    }

    private companion object {
        const val MULTI_ENTRIES_COMBINED = 3
        const val GENERATOR_X_RANGE_TOP = 96
        const val GENERATOR_Y_RANGE_BOTTOM = 2
        const val GENERATOR_Y_RANGE_TOP = 20
        const val MULTI_GENERATOR_X_RANGE_TOP = GENERATOR_X_RANGE_TOP / MULTI_ENTRIES_COMBINED
        const val UPDATE_FREQUENCY = 2000L
    }
}
