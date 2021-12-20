/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartEntryModelProducer
import pl.patrykgoworowski.vico.core.entry.composed.ComposedChartEntryModelProducer
import pl.patrykgoworowski.vico.core.entry.composed.plus
import pl.patrykgoworowski.vico.core.entry.diff.DefaultDiffAnimator
import pl.patrykgoworowski.vico.core.util.RandomEntriesGenerator

public class ShowcaseViewModel : ViewModel() {

    private val generator = RandomEntriesGenerator(0..GENERATOR_X_RANGE_TOP)
    private val multiGenerator = RandomEntriesGenerator(0..MULTI_GENERATOR_X_RANGE_TOP)

    public val entries: ChartEntryModelProducer = ChartEntryModelProducer(diffAnimator = DefaultDiffAnimator())
    public val multiEntries: ChartEntryModelProducer = ChartEntryModelProducer(diffAnimator = DefaultDiffAnimator())

    public val composedEntries: ComposedChartEntryModelProducer<ChartEntryModel> = multiEntries + entries

    init {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                entries.setEntries(generator.generateRandomEntries())
                multiEntries.setEntries(
                    List(size = MULTI_ENTRIES_COMBINED) { multiGenerator.generateRandomEntries() }
                )
                delay(UPDATE_FREQUENCY)
            }
        }
    }

    private companion object {
        const val MULTI_ENTRIES_COMBINED = 3
        const val GENERATOR_X_RANGE_TOP = 96
        const val MULTI_GENERATOR_X_RANGE_TOP = GENERATOR_X_RANGE_TOP / MULTI_ENTRIES_COMBINED
        const val UPDATE_FREQUENCY = 2000L
    }
}
