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

package com.patrykandpatrick.vico.sample.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.Fill

private val LineColors = listOf(Color(0xff3e6558), Color(0xff5e836a))
private val x = (1..12).toList()
private val y =
  listOf(
    listOf(2, 4, 3, 6, 5, 8, 7, 9, 8, 11, 10, 12),
    listOf(6, 5, 8, 7, 10, 9, 12, 11, 14, 13, 16, 15),
  )

@Composable
private fun ComposeGuidelinesOverAreaFills(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  CartesianChartHost(
    rememberCartesianChart(
      rememberLineCartesianLayer(
        lineProvider =
          LineCartesianLayer.LineProvider.series(
            LineColors.map { color ->
              LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(color)),
                areaFill = LineCartesianLayer.AreaFill.single(Fill(color.copy(alpha = 0.5f))),
              )
            }
          ),
        // Draws every series’ area fill before any series’ stroke. Required for the guidelines to
        // be drawn between the two, and useful on its own: no area fill covers a stroke.
        seriesDrawingOrder = LineCartesianLayer.SeriesDrawingOrder.AreaFillsFirst,
      ),
      startAxis =
        VerticalAxis.rememberStart(
          guidelineDrawingOrder = CartesianChart.DrawingOrder.OverAreaFills
        ),
      bottomAxis =
        HorizontalAxis.rememberBottom(
          guidelineDrawingOrder = CartesianChart.DrawingOrder.OverAreaFills
        ),
    ),
    modelProducer,
    modifier,
    rememberVicoScrollState(scrollEnabled = false),
  )
}

@Composable
fun ComposeGuidelinesOverAreaFills(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/z5ah6v.
      lineModel { y.forEach { series(x, it) } }
    }
  }
  ComposeGuidelinesOverAreaFills(modelProducer, modifier)
}

@Composable
@Preview
fun ComposeGuidelinesOverAreaFillsPreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking?.invoke {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/z5ah6v.
      lineModel { y.forEach { series(x, it) } }
    }
  }
  PreviewBox { ComposeGuidelinesOverAreaFills(modelProducer) }
}
