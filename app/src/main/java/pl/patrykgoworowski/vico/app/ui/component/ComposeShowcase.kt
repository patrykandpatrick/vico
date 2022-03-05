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

package pl.patrykgoworowski.vico.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.app.ShowcaseViewModel

@Composable
internal fun ComposeShowcase(showcaseViewModel: ShowcaseViewModel) {
    Box(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(all = 20.dp),
        ) {
            ColumnChart(chartEntryModelProducer = showcaseViewModel.entries)
            ComposedChart(model = showcaseViewModel.composedEntries)
            StackedColumnChart(chartEntryModelProducer = showcaseViewModel.multiEntries)
            ComposedChart(model = showcaseViewModel.composedEntries)
            LineChart(chartEntryModelProducer = showcaseViewModel.entries)
            GroupedColumnChart(chartEntryModelProducer = showcaseViewModel.multiEntries)
        }
    }
}
