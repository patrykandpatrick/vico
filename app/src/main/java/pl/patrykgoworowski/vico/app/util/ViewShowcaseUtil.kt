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

package pl.patrykgoworowski.vico.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.patrykgoworowski.vico.app.ShowcaseViewModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.collectAsFlow
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.view.chart.ChartView
import pl.patrykgoworowski.vico.view.chart.ComposedChartView

@ExperimentalCoroutinesApi
internal class ViewShowcaseUtil(
    private val viewModel: ShowcaseViewModel,
    private val coroutineScope: CoroutineScope
) {

    fun setUpColumnChart(chartView: ChartView, marker: Marker?) {
        viewModel.entries.collectAsFlow
            .onEach(chartView::setModel)
            .launchIn(coroutineScope)

        chartView.marker = marker
    }

    fun setUpComposedChart(chartView: ComposedChartView, marker: Marker?) {
        viewModel.composedEntries.collectAsFlow
            .onEach(chartView::setModel)
            .launchIn(coroutineScope)

        chartView.marker = marker
    }

    fun setUpLineChart(chartView: ChartView, marker: Marker?) {
        viewModel.entries.collectAsFlow
            .onEach(chartView::setModel)
            .launchIn(coroutineScope)

        chartView.marker = marker
    }

    fun setUpGroupedColumnChart(chartView: ChartView, marker: Marker?) {
        viewModel.multiEntries.collectAsFlow
            .onEach(chartView::setModel)
            .launchIn(coroutineScope)

        chartView.marker = marker
    }

    fun setUpStackedColumnChart(chartView: ChartView, marker: Marker?) {
        viewModel.multiEntries.collectAsFlow
            .onEach(chartView::setModel)
            .launchIn(coroutineScope)

        chartView.marker = marker
    }
}
