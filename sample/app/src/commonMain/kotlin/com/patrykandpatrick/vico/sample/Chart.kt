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

class Chart(internal val details: Details, internal val content: @Composable (Modifier) -> Unit) {
  @Composable
  operator fun invoke(modifier: Modifier = Modifier) {
    content(modifier)
  }

  class Details(internal val title: String, internal val citation: String? = null) {
    companion object {
      val BasicColumnChart = Details("Basic column chart")
      val BasicLineChart = Details("Basic line chart")
      val BasicComboChart = Details("Basic combo chart")
      val AITestScores =
        Details("AI test scores", "Kiela et al. 2023. Processing by Our World in\u00A0Data.")
      val DailyDigitalMediaUse =
        Details(
          "Daily digital-media use (USA)",
          "BOND Internet Trends 2019. Processing by Our World in\u00A0Data.",
        )
      val TemperatureAnomalies =
        Details(
          "Temperature anomalies (June)",
          "Copernicus Climate Change Service 2019. Processing by Our World in\u00A0Data.",
        )
      val ElectricCarSales =
        Details(
          "Electric-car sales (Norway)",
          "International Energy Agency 2023. Processing by Our World in\u00A0Data.",
        )
      val RockMetalRatios =
        Details(
          "Rock–metal ratios",
          "Nassar et al. 2022; Wang et al. 2024. Processing by Our World in\u00A0Data.",
        )
      val GoldPrices = Details("Gold prices (12/30/2024)", "Yahoo Finance n.d.")
      val ClickableColumnChart = Details("Clickable column chart")
      val ColorChangingColumnChart = Details("Color-changing column chart")
      val MultiColorColumnChart = Details("Multi-color column chart")
    }
  }
}
