/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.sample.Chart.Details
import com.patrykandpatrick.vico.sample.multiplatform.ComposeMultiplatformBasicColumnChart
import com.patrykandpatrick.vico.sample.multiplatform.ComposeMultiplatformBasicComboChart
import com.patrykandpatrick.vico.sample.multiplatform.ComposeMultiplatformBasicLineChart

object Charts {
  val ComposeMultiplatform =
    listOf(
      Chart(Details.BasicColumnChart) { ComposeMultiplatformBasicColumnChart(it) },
      Chart(Details.BasicLineChart) { ComposeMultiplatformBasicLineChart(it) },
      Chart(Details.BasicComboChart) { ComposeMultiplatformBasicComboChart(it) },
    )

  fun default(
    basicColumnChart: @Composable (Modifier) -> Unit,
    basicLineChart: @Composable (Modifier) -> Unit,
    basicComboChart: @Composable (Modifier) -> Unit,
    aiTestScores: @Composable (Modifier) -> Unit,
    dailyDigitalMediaUse: @Composable (Modifier) -> Unit,
    temperatureAnomalies: @Composable (Modifier) -> Unit,
    electricCarSales: @Composable (Modifier) -> Unit,
    rockMetalRatios: @Composable (Modifier) -> Unit,
    goldPrices: @Composable (Modifier) -> Unit,
  ) =
    listOf(
      Chart(Details.BasicColumnChart, basicColumnChart),
      Chart(Details.BasicLineChart, basicLineChart),
      Chart(Details.BasicComboChart, basicComboChart),
      Chart(Details.AITestScores, aiTestScores),
      Chart(Details.DailyDigitalMediaUse, dailyDigitalMediaUse),
      Chart(Details.TemperatureAnomalies, temperatureAnomalies),
      Chart(Details.ElectricCarSales, electricCarSales),
      Chart(Details.RockMetalRatios, rockMetalRatios),
      Chart(Details.GoldPrices, goldPrices),
    )
}

expect val Charts.overridden: LinkedHashMap<UIFramework, List<Chart>>?

internal val Charts.all
  get() = overridden ?: linkedMapOf(UIFramework.ComposeMultiplatform to ComposeMultiplatform)
