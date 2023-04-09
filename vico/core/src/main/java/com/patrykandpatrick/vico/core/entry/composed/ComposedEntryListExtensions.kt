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

package com.patrykandpatrick.vico.core.entry.composed

import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer

/**
 * Combines two [ChartModelProducer] implementations into a [ComposedChartEntryModelProducer].
 */
public operator fun <Model : ChartEntryModel> ChartModelProducer<Model>.plus(
    other: ChartModelProducer<Model>,
): ComposedChartEntryModelProducer<Model> =
    ComposedChartEntryModelProducer(listOf(this, other))

/**
 * Combines this [ComposedChartEntryModelProducer] and a [ChartModelProducer]
 * into a single [ComposedChartEntryModelProducer].
 */
public operator fun <Model : ChartEntryModel> ComposedChartEntryModelProducer<Model>.plus(
    other: ChartModelProducer<Model>,
): ComposedChartEntryModelProducer<Model> =
    ComposedChartEntryModelProducer(chartModelProducers + other)

/**
 * Combines two [ComposedChartEntryModelProducer] instances into a single one.
 */
public operator fun <Model : ChartEntryModel> ComposedChartEntryModelProducer<Model>.plus(
    other: ComposedChartEntryModelProducer<Model>,
): ComposedChartEntryModelProducer<Model> =
    ComposedChartEntryModelProducer(chartModelProducers + other.chartModelProducers)

/**
 * Combines two [ChartEntryModel] implementations—the receiver and [other]—into a [ComposedChartEntryModel].
 */
public operator fun <Model : ChartEntryModel> Model.plus(other: Model): ComposedChartEntryModel<Model> =
    ComposedChartEntryModelProducer.composedChartEntryModelOf(listOf(this, other))
