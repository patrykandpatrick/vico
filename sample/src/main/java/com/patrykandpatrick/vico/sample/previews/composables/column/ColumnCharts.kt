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

package com.patrykandpatrick.vico.sample.previews.composables.column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.scroll.AutoScrollCondition
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import com.patrykandpatrick.vico.sample.previews.annotation.ChartPreview
import com.patrykandpatrick.vico.sample.previews.resource.PreviewSurface
import com.patrykandpatrick.vico.sample.previews.resource.longEntryModel
import com.patrykandpatrick.vico.sample.previews.resource.mediumEntryModel
import com.patrykandpatrick.vico.sample.previews.resource.shortEntryModel
import kotlinx.coroutines.delay

@ChartPreview
@Composable
public fun DefaultColumnChart(
    model: ChartEntryModel = shortEntryModel,
    oldModel: ChartEntryModel? = null,
    scrollable: Boolean = true,
    initialScroll: InitialScroll = InitialScroll.Start,
    autoScrollCondition: AutoScrollCondition<ChartEntryModel> = AutoScrollCondition.Never,
) = PreviewSurface {
    Chart(
        chart = columnChart(),
        model = model,
        oldModel = oldModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        chartScrollSpec = rememberChartScrollSpec(
            isScrollEnabled = scrollable,
            initialScroll = initialScroll,
            autoScrollCondition = autoScrollCondition,
        ),
    )
}

@ChartPreview
@Composable
public fun DefaultColumnChartLongScrollable() {
    DefaultColumnChart(model = mediumEntryModel)
}

@ChartPreview
@Composable
public fun DefaultColumnChartLongScrollableEnd() {
    DefaultColumnChart(model = mediumEntryModel, initialScroll = InitialScroll.End)
}

@ChartPreview
@Composable
public fun DefaultColumnChartAutoScrollOnModelSizeIncreased() {
    var model by remember { mutableStateOf(shortEntryModel) }

    LaunchedEffect(key1 = Unit) {
        delay(100)
        model = longEntryModel
    }

    DefaultColumnChart(
        model = model,
        oldModel = shortEntryModel,
        initialScroll = InitialScroll.End,
        autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased,
    )
}

@ChartPreview
@Composable
public fun DefaultColumnChartLongNonScrollable() {
    DefaultColumnChart(model = mediumEntryModel, scrollable = false)
}
