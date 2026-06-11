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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerDrawingModelInterpolator
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill

@Composable
private fun ComposeRevealLineChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val lineColor = Color(0xff3287ff)
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                areaFill =
                  LineCartesianLayer.AreaFill.single(
                    Fill(
                      Brush.verticalGradient(
                        listOf(lineColor.copy(alpha = 0.4f), Color.Transparent)
                      )
                    )
                  ),
              )
            ),
          drawingModelInterpolator = CartesianLayerDrawingModelInterpolator.line(reveal = true),
        ),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
  )
}

@Composable
fun ComposeRevealLineChart(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      lineModel { series(4, 9, 2, 11, 7, 13, 5, 14, 3, 10, 8, 15, 6, 12, 1, 11) }
    }
  }
  ComposeRevealLineChart(modelProducer, modifier)
}

@Composable
@Preview
private fun ComposeRevealLineChartPreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  runBlocking?.invoke {
    modelProducer.runTransaction {
      lineModel { series(4, 9, 2, 11, 7, 13, 5, 14, 3, 10, 8, 15, 6, 12, 1, 11) }
    }
  }
  PreviewBox { ComposeRevealLineChart(modelProducer) }
}
