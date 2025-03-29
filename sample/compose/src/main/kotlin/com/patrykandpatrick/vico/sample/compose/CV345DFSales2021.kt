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

package com.patrykandpatrick.vico.sample.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.dashed
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlin.collections.component1
import kotlin.collections.component2
import kotlinx.coroutines.runBlocking

@Composable
private fun JetpackComposeCV345DFSales2021(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val colour1 = Color(0xff916cda)
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.rememberLine(
                stroke = LineCartesianLayer.LineStroke.dashed(dashLength = 0.dp),
                pointProvider =
                  LineCartesianLayer.PointProvider.single(
                    LineCartesianLayer.Point(
                      rememberShapeComponent(fill(colour1), CorneredShape.Pill)
                    )
                  ),
              )
            )
        ),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
        marker = rememberMarker(),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    zoomState = rememberVicoZoomState(zoomEnabled = true, initialZoom = Zoom.Content),
  )
}

@Composable
fun JetpackComposeCV345DFSales2021(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/vmml6t.
      lineSeries { data.forEach { series(it.keys, it.values) } }
    }
  }
  JetpackComposeCV345DFSales2021(modelProducer, modifier)
}

private val data =
  listOf(
    mapOf<Int, Int>(13 to 225, 17 to 200, 18 to 221, 32 to 270, 34 to 246, 38 to 205, 40 to 215),
    mapOf<Int, Int>(17 to 230),
  )

@Composable
@Preview
private fun Preview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/vmml6t.
      lineSeries { data.forEach { series(it.keys, it.values) } }
    }
  }
  PreviewBox { JetpackComposeCV345DFSales2021(modelProducer) }
}
