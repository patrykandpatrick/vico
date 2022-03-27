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

package pl.patrykgoworowski.vico.sample.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.sample.ShowcaseViewModel

@Composable
internal fun ComposeShowcase(showcaseViewModel: ShowcaseViewModel) {
    Box(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(all = 20.dp)
                .navigationBarsPadding(),
        ) {
            ColumnChart(chartEntryModelProducer = showcaseViewModel.chartEntryModelProducer)
            StackedColumnChart(chartEntryModelProducer = showcaseViewModel.multiChartEntryModelProducer)
            ComposedChart(model = showcaseViewModel.composedChartEntryModelProducer)
            StackedColumnChart(chartEntryModelProducer = showcaseViewModel.multiChartEntryModelProducer)
            ComposedChart(model = showcaseViewModel.composedChartEntryModelProducer)
            LineChart(chartEntryModelProducer = showcaseViewModel.chartEntryModelProducer)
            GroupedColumnChart(chartEntryModelProducer = showcaseViewModel.multiChartEntryModelProducer)
        }
    }
}
