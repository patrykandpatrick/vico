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

package com.patrykandpatrick.vico.sample.charts.compose

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart

@Composable
private fun ComposeBasicPieChart(
  modelProducer: PieChartModelProducer,
  modifier: Modifier = Modifier,
) {
  PieChartHost(
    chart =
      rememberPieChart(
        sliceProvider =
          PieChart.SliceProvider.series(
            vicoTheme.pieChartColors.mapIndexed { index, color ->
              PieChart.Slice(
                fill = Fill(color),
                label =
                  PieChart.SliceLabel.Inside(
                    TextComponent(TextStyle(if (index == 2) Color.Black else Color.White))
                  ),
              )
            }
          ),
        valueFormatter = PieValueFormatter { _, value, _ -> "${value.toInt()}%" },
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(240.dp),
  )
}

@Composable
fun ComposeBasicPieChart(modifier: Modifier = Modifier) {
  val modelProducer = remember { PieChartModelProducer() }
  LaunchedEffect(Unit) { modelProducer.runTransaction { pieSeries { series(60, 20, 20) } } }
  ComposeBasicPieChart(modelProducer, modifier)
}

@Composable
@Preview
private fun ComposeBasicPieChartPreview() {
  val modelProducer = remember { PieChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking?.invoke { modelProducer.runTransaction { pieSeries { series(60, 20, 20) } } }
  PreviewBox { ComposeBasicPieChart(modelProducer) }
}
