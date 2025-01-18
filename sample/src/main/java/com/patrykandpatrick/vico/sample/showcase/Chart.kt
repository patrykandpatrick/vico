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

package com.patrykandpatrick.vico.sample.showcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.sample.showcase.charts.AITestScores
import com.patrykandpatrick.vico.sample.showcase.charts.BasicColumnChart
import com.patrykandpatrick.vico.sample.showcase.charts.BasicComboChart
import com.patrykandpatrick.vico.sample.showcase.charts.BasicLineChart
import com.patrykandpatrick.vico.sample.showcase.charts.DailyDigitalMediaUse
import com.patrykandpatrick.vico.sample.showcase.charts.ElectricCarSales
import com.patrykandpatrick.vico.sample.showcase.charts.GoldPrices
import com.patrykandpatrick.vico.sample.showcase.charts.RockMetalRatios
import com.patrykandpatrick.vico.sample.showcase.charts.TemperatureAnomalies

internal class Chart(
  val title: String,
  val citation: String? = null,
  val content: @Composable (UIFramework, Modifier) -> Unit,
)

internal val charts =
  listOf(
    Chart("Basic column chart") { uiFramework, modifier ->
      BasicColumnChart(uiFramework, modifier)
    },
    Chart("Basic line chart") { uiFramework, modifier -> BasicLineChart(uiFramework, modifier) },
    Chart("Basic combo chart") { uiFramework, modifier -> BasicComboChart(uiFramework, modifier) },
    Chart("AI test scores", "Kiela et al. 2023. Processing by Our World in\u00A0Data.") {
      uiFramework,
      modifier ->
      AITestScores(uiFramework, modifier)
    },
    Chart(
      "Daily digital-media use (USA)",
      "BOND Internet Trends 2019. Processing by Our World in\u00A0Data.",
    ) { uiFramework, modifier ->
      DailyDigitalMediaUse(uiFramework, modifier)
    },
    Chart(
      "Temperature anomalies (June)",
      "Copernicus Climate Change Service 2019. Processing by Our World in\u00A0Data.",
    ) { uiFramework, modifier ->
      TemperatureAnomalies(uiFramework, modifier)
    },
    Chart(
      "Electric-car sales (Norway)",
      "International Energy Agency 2023. Processing by Our World in\u00A0Data.",
    ) { uiFramework, modifier ->
      ElectricCarSales(uiFramework, modifier)
    },
    Chart(
      "Rock–metal ratios",
      "Nassar et al. 2022; Wang et al. 2024. Processing by Our World in\u00A0Data.",
    ) { uiFramework, modifier ->
      RockMetalRatios(uiFramework, modifier)
    },
    Chart("Gold prices (12/30/2024)", "Yahoo Finance n.d.") { uiFramework, modifier ->
      GoldPrices(uiFramework, modifier)
    },
  )
