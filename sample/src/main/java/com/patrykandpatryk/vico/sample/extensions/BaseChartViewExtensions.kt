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

package com.patrykandpatryk.vico.sample.extensions

import android.content.Context
import com.patrykandpatryk.vico.R
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.entry.ChartModelProducer
import com.patrykandpatryk.vico.core.marker.Marker
import com.patrykandpatryk.vico.view.chart.BaseChartView

private val Context.m3ElevationOverlayColor: Int
    get() = resolveColorAttribute(R.attr.colorPrimary)

internal fun <T : ChartEntryModel> BaseChartView<T>.setUpChart(
    chartModelProducer: ChartModelProducer<T>,
    marker: Marker,
    block: (BaseChartView<T>.() -> Unit)? = null,
) {
    this.marker = marker
    entryProducer = chartModelProducer
    elevationOverlayColor = context.m3ElevationOverlayColor
    block?.invoke(this)
}
