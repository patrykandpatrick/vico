/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.patrykandpatrick.vico.sample.paparazzi.lightConfig
import com.patrykandpatrick.vico.sample.paparazzi.nightConfig
import com.patrykandpatrick.vico.sample.previews.composables.column.DefaultColumnChart
import com.patrykandpatrick.vico.sample.previews.composables.column.DefaultColumnChartLongNonScrollable
import com.patrykandpatrick.vico.sample.previews.composables.column.DefaultColumnChartLongScrollable
import com.patrykandpatrick.vico.sample.previews.composables.column.DefaultColumnChartLongScrollableEnd
import com.patrykandpatrick.vico.sample.previews.composables.line.DefaultLineChart
import com.patrykandpatrick.vico.sample.previews.composables.line.DefaultLineChartLongNonScrollable
import com.patrykandpatrick.vico.sample.previews.composables.line.DefaultLineChartLongScrollable
import com.patrykandpatrick.vico.sample.previews.composables.line.DefaultLineChartLongScrollableEnd
import org.junit.Rule
import org.junit.Test

public class PaparazziTest {
  private val defaultCharts =
    listOf<Pair<String, @Composable () -> Unit>>(
      "LineChart" to { DefaultLineChart() },
      "LineChart Long Scrollable" to { DefaultLineChartLongScrollable() },
      "LineChart Long Scrollable with initial scroll end" to
        {
          DefaultLineChartLongScrollableEnd()
        },
      "LineChart Long Not Scrollable" to { DefaultLineChartLongNonScrollable() },
      "ColumnChart" to { DefaultColumnChart() },
      "ColumnChart Long Scrollable" to { DefaultColumnChartLongScrollable() },
      "ColumnChart Long Scrollable with initial scroll end" to
        {
          DefaultColumnChartLongScrollableEnd()
        },
      "ColumnChart Long Not Scrollable" to { DefaultColumnChartLongNonScrollable() },
    )

  @get:Rule
  public val paparazzi =
    Paparazzi(
      deviceConfig = lightConfig,
      renderingMode = SessionParams.RenderingMode.SHRINK,
      maxPercentDifference = 0.2,
    )

  private fun List<Pair<String, @Composable () -> Unit>>.snapshotAll() {
    forEach { (name, composable) -> paparazzi.snapshot(name) { composable() } }
  }

  @Test
  public fun `Test default charts in NOT NIGHT`() {
    defaultCharts.snapshotAll()
  }

  @Test
  public fun `Test default charts in NIGHT`() {
    paparazzi.unsafeUpdateConfig(nightConfig)
    defaultCharts.snapshotAll()
  }
}
