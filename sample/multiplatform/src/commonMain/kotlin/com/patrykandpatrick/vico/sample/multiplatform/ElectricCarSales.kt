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

package com.patrykandpatrick.vico.sample.multiplatform

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill

private val RangeProvider = CartesianLayerRangeProvider.fixed(maxY = 100.0)
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(suffix = "%")
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(suffix = "%")
private val x = (2010..2023).toList()
private val y = listOf<Number>(0.28, 1.4, 3.1, 5.8, 15, 22, 29, 39, 49, 56, 75, 86, 89, 93)

@Composable
fun ComposeMultiplatformElectricCarSales(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/z5ah6v.
      lineSeries { series(x, y) }
    }
  }
  val lineColor = Color(0xffa485e0)
  CartesianChartHost(
    rememberCartesianChart(
      rememberLineCartesianLayer(
        lineProvider =
          LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.rememberLine(
              fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
              areaFill =
                LineCartesianLayer.AreaFill.single(
                  Fill(
                    Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.4f), Color.Transparent))
                  )
                ),
            )
          ),
        rangeProvider = RangeProvider,
      ),
      startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
      bottomAxis = HorizontalAxis.rememberBottom(),
      marker = rememberMarker(MarkerValueFormatter),
    ),
    modelProducer,
    modifier.height(216.dp),
    rememberVicoScrollState(scrollEnabled = false),
  )
}
