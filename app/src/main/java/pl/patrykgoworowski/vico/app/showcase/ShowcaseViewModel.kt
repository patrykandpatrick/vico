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

package pl.patrykgoworowski.vico.app.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.patrykgoworowski.vico.app.data.RandomEntriesGenerator
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryList
import pl.patrykgoworowski.vico.core.dataset.entry.collection.composed.plus

class ShowcaseViewModel : ViewModel() {

    private val generator = RandomEntriesGenerator(0..96)
    private val multiGenerator = RandomEntriesGenerator(0..32)

    val entries = EntryList()
    val multiEntries = EntryList()

    val composedEntries = multiEntries + entries

    init {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                entries.setEntries(generator.generateRandomEntries())
                multiEntries.setEntries(
                    listOf(
                        multiGenerator.generateRandomEntries(),
                        multiGenerator.generateRandomEntries(),
                        multiGenerator.generateRandomEntries(),
                    )
                )
                delay(2_000)
            }
        }
    }
}
