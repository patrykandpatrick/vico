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

package com.patrykandpatryk.vico.sample.util

import com.patrykandpatryk.vico.R
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatryk.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatryk.vico.sample.chart.ComposeColumnChart
import com.patrykandpatryk.vico.sample.chart.ComposeComplexComposedChart
import com.patrykandpatryk.vico.sample.chart.ComposeComposedChart
import com.patrykandpatryk.vico.sample.chart.ComposeGroupedColumnChart
import com.patrykandpatryk.vico.sample.chart.ComposeLineChart
import com.patrykandpatryk.vico.sample.chart.ComposeLineChartWithFadingEdges
import com.patrykandpatryk.vico.sample.chart.ComposeLineChartWithLabelsInside
import com.patrykandpatryk.vico.sample.chart.ComposeStackedColumnChart
import com.patrykandpatryk.vico.sample.chart.ViewColumnChart
import com.patrykandpatryk.vico.sample.chart.ViewComplexComposedChart
import com.patrykandpatryk.vico.sample.chart.ViewComposedChart
import com.patrykandpatryk.vico.sample.chart.ViewGroupedColumnChart
import com.patrykandpatryk.vico.sample.chart.ViewLineChart
import com.patrykandpatryk.vico.sample.chart.ViewLineChartWithFadingEdges
import com.patrykandpatryk.vico.sample.chart.ViewLineChartWithLabelsInside
import com.patrykandpatryk.vico.sample.chart.ViewStackedColumnChart

internal fun getSampleCharts(
    chartEntryModelProducer: ChartEntryModelProducer,
    chartStepEntryModelProducer: ChartEntryModelProducer,
    multiChartEntryModelProducer: ChartEntryModelProducer,
    composedChartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>,
) = listOf(
    SampleChart(
        labelResourceId = R.string.line_chart_label,
        descriptionResourceId = R.string.line_chart_description,
        composeBased = { ComposeLineChart(chartEntryModelProducer = chartStepEntryModelProducer) },
        viewBased = { ViewLineChart(chartEntryModelProducer = chartStepEntryModelProducer) },
    ),
    SampleChart(
        labelResourceId = R.string.column_chart_label,
        descriptionResourceId = R.string.column_chart_description,
        composeBased = { ComposeColumnChart(chartEntryModelProducer = chartEntryModelProducer) },
        viewBased = { ViewColumnChart(chartEntryModelProducer = chartEntryModelProducer) },
    ),
    SampleChart(
        labelResourceId = R.string.line_chart_with_fading_edges_label,
        descriptionResourceId = R.string.line_chart_with_fading_edges_description,
        composeBased = { ComposeLineChartWithFadingEdges(chartEntryModelProducer = chartEntryModelProducer) },
        viewBased = { ViewLineChartWithFadingEdges(chartEntryModelProducer = chartEntryModelProducer) },
    ),
    SampleChart(
        labelResourceId = R.string.composed_chart_label,
        descriptionResourceId = R.string.composed_chart_description,
        composeBased = { ComposeComposedChart(composedChartEntryModelProducer = composedChartEntryModelProducer) },
        viewBased = { ViewComposedChart(composedChartEntryModelProducer = composedChartEntryModelProducer) },
    ),
    SampleChart(
        labelResourceId = R.string.stacked_column_chart_label,
        descriptionResourceId = R.string.stacked_column_chart_description,
        composeBased = { ComposeStackedColumnChart(chartEntryModelProducer = multiChartEntryModelProducer) },
        viewBased = { ViewStackedColumnChart(chartEntryModelProducer = multiChartEntryModelProducer) },
    ),
    SampleChart(
        labelResourceId = R.string.grouped_column_chart_label,
        descriptionResourceId = R.string.grouped_column_chart_description,
        composeBased = { ComposeGroupedColumnChart(chartEntryModelProducer = multiChartEntryModelProducer) },
        viewBased = { ViewGroupedColumnChart(chartEntryModelProducer = multiChartEntryModelProducer) },
    ),
    SampleChart(
        labelResourceId = R.string.line_chart_with_labels_inside_label,
        descriptionResourceId = R.string.line_chart_with_labels_inside_description,
        composeBased = { ComposeLineChartWithLabelsInside(chartEntryModelProducer = multiChartEntryModelProducer) },
        viewBased = { ViewLineChartWithLabelsInside(chartEntryModelProducer = multiChartEntryModelProducer) },
    ),
    SampleChart(
        labelResourceId = R.string.complex_composed_chart_label,
        descriptionResourceId = R.string.complex_composed_chart_description,
        composeBased = {
            ComposeComplexComposedChart(composedChartEntryModelProducer = composedChartEntryModelProducer)
        },
        viewBased = { ViewComplexComposedChart(composedChartEntryModelProducer = composedChartEntryModelProducer) },
    ),
)
