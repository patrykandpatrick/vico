/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.patrykandpatrick.vico.sample.charts.compose.ComposeAITestScoresPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeBasicColumnChartPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeBasicComboChartPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeBasicLineChartPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeBasicPieChartPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeDailyDigitalMediaUsePreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeElectricCarSalesPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeGoldPricesPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeRockMetalRatiosPreview
import com.patrykandpatrick.vico.sample.charts.compose.ComposeTemperatureAnomaliesPreview

@PreviewTest
@Preview
@Composable
internal fun BasicColumnChartScreenshotTest() {
  ComposeBasicColumnChartPreview()
}

@PreviewTest
@Preview
@Composable
internal fun BasicLineChartScreenshotTest() {
  ComposeBasicLineChartPreview()
}

@PreviewTest
@Preview
@Composable
internal fun BasicComboChartScreenshotTest() {
  ComposeBasicComboChartPreview()
}

@PreviewTest
@Preview
@Composable
internal fun BasicPieChartScreenshotTest() {
  ComposeBasicPieChartPreview()
}

@PreviewTest
@Preview
@Composable
internal fun AITestScoresScreenshotTest() {
  ComposeAITestScoresPreview()
}

@PreviewTest
@Preview
@Composable
internal fun DailyDigitalMediaUseScreenshotTest() {
  ComposeDailyDigitalMediaUsePreview()
}

@PreviewTest
@Preview
@Composable
internal fun TemperatureAnomaliesScreenshotTest() {
  ComposeTemperatureAnomaliesPreview()
}

@PreviewTest
@Preview
@Composable
internal fun ElectricCarSalesScreenshotTest() {
  ComposeElectricCarSalesPreview()
}

@PreviewTest
@Preview
@Composable
internal fun RockMetalRatiosScreenshotTest() {
  ComposeRockMetalRatiosPreview()
}

@PreviewTest
@Preview
@Composable
internal fun GoldPricesScreenshotTest() {
  ComposeGoldPricesPreview()
}
