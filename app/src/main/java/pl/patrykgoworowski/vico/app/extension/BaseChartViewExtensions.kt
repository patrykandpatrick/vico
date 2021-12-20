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

package pl.patrykgoworowski.vico.app.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.entry.ChartModelProducer
import pl.patrykgoworowski.vico.core.entry.collectAsFlow
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.view.chart.BaseChartView

@OptIn(ExperimentalCoroutinesApi::class)
public fun <T : ChartEntryModel> BaseChartView<T>.setUpChart(
    entries: ChartModelProducer<T>,
    coroutineScope: CoroutineScope,
    marker: Marker
) {
    this.marker = marker
    entries.collectAsFlow
        .onEach(::setModel)
        .launchIn(coroutineScope)
}
