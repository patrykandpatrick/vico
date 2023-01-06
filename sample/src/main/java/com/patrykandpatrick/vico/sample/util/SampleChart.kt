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

package com.patrykandpatrick.vico.sample.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.composed.ComposedChartEntryModelProducer
import com.patrykandpatrick.vico.sample.chart.ComposeChart1
import com.patrykandpatrick.vico.sample.chart.ComposeChart2
import com.patrykandpatrick.vico.sample.chart.ComposeChart3
import com.patrykandpatrick.vico.sample.chart.ComposeChart4
import com.patrykandpatrick.vico.sample.chart.ComposeChart5
import com.patrykandpatrick.vico.sample.chart.ComposeChart6
import com.patrykandpatrick.vico.sample.chart.ComposeChart7
import com.patrykandpatrick.vico.sample.chart.ComposeChart8
import com.patrykandpatrick.vico.sample.chart.ViewChart1
import com.patrykandpatrick.vico.sample.chart.ViewChart2
import com.patrykandpatrick.vico.sample.chart.ViewChart3
import com.patrykandpatrick.vico.sample.chart.ViewChart4
import com.patrykandpatrick.vico.sample.chart.ViewChart5
import com.patrykandpatrick.vico.sample.chart.ViewChart6
import com.patrykandpatrick.vico.sample.chart.ViewChart7
import com.patrykandpatrick.vico.sample.chart.ViewChart8

internal data class SampleChart(
    val composeBased: @Composable () -> Unit,
    val viewBased: @Composable () -> Unit,
    @StringRes val descriptionResourceID: Int,
)

@Composable
internal fun rememberSampleCharts(
    chartEntryModelProducer: ChartEntryModelProducer,
    customStepChartEntryModelProducer: ChartEntryModelProducer,
    multiDataSetChartEntryModelProducer: ChartEntryModelProducer,
    composedChartEntryModelProducer: ComposedChartEntryModelProducer<ChartEntryModel>,
) = remember {
    listOf(
        SampleChart(
            composeBased = { ComposeChart1(customStepChartEntryModelProducer) },
            viewBased = { ViewChart1(customStepChartEntryModelProducer) },
            descriptionResourceID = R.string.chart_1_description,
        ),
        SampleChart(
            composeBased = { ComposeChart2(chartEntryModelProducer) },
            viewBased = { ViewChart2(chartEntryModelProducer) },
            descriptionResourceID = R.string.chart_2_description,
        ),
        SampleChart(
            composeBased = { ComposeChart3(chartEntryModelProducer) },
            viewBased = { ViewChart3(chartEntryModelProducer) },
            descriptionResourceID = R.string.chart_3_description,
        ),
        SampleChart(
            composeBased = { ComposeChart4(composedChartEntryModelProducer) },
            viewBased = { ViewChart4(composedChartEntryModelProducer) },
            descriptionResourceID = R.string.chart_4_description,
        ),
        SampleChart(
            composeBased = { ComposeChart5(multiDataSetChartEntryModelProducer) },
            viewBased = { ViewChart5(multiDataSetChartEntryModelProducer) },
            descriptionResourceID = R.string.chart_5_description,
        ),
        SampleChart(
            composeBased = { ComposeChart6(multiDataSetChartEntryModelProducer) },
            viewBased = { ViewChart6(multiDataSetChartEntryModelProducer) },
            descriptionResourceID = R.string.chart_6_description,
        ),
        SampleChart(
            composeBased = { ComposeChart7(multiDataSetChartEntryModelProducer) },
            viewBased = { ViewChart7(multiDataSetChartEntryModelProducer) },
            descriptionResourceID = R.string.chart_7_description,
        ),
        SampleChart(
            composeBased = { ComposeChart8(composedChartEntryModelProducer) },
            viewBased = { ViewChart8(composedChartEntryModelProducer) },
            descriptionResourceID = R.string.chart_8_description,
        ),
    )
}
