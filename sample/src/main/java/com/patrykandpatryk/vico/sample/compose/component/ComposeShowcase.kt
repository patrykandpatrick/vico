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

package com.patrykandpatryk.vico.sample.compose.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatryk.vico.compose.style.ProvideChartStyle
import com.patrykandpatryk.vico.sample.compose.theme.MainTheme
import com.patrykandpatryk.vico.sample.viewmodel.ShowcaseViewModel

@Composable
internal fun ComposeShowcase(showcaseViewModel: ShowcaseViewModel) {
    MainTheme {
        ProvideChartStyle(
            chartStyle = m3ChartStyle(
                axisGuidelineColor = MaterialTheme.colorScheme.outline.copy(alpha = OUTLINE_COLOR_ALPHA),
                axisLineColor = MaterialTheme.colorScheme.outline.copy(alpha = OUTLINE_COLOR_ALPHA),
            ),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(20.dp),
            ) {
                ColumnChart(chartEntryModelProducer = showcaseViewModel.chartEntryModelProducer)
                LineChart(chartEntryModelProducer = showcaseViewModel.chartEntryModelProducer)
                StackedColumnChart(chartEntryModelProducer = showcaseViewModel.multiChartEntryModelProducer)
                ComposedChart(model = showcaseViewModel.composedChartEntryModelProducer)
                GroupedColumnChart(chartEntryModelProducer = showcaseViewModel.multiChartEntryModelProducer)
                LineChartWithLabelsInside(chartEntryModelProducer = showcaseViewModel.chartEntryModelProducer)
            }
        }
    }
}

private const val OUTLINE_COLOR_ALPHA = 0.5f
