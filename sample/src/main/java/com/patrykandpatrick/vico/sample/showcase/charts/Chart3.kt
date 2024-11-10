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

package com.patrykandpatrick.vico.sample.showcase.charts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.databinding.Chart3Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlin.math.ceil
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun Chart3(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          /* Learn more:
          https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
          lineSeries { series(List(Defaults.ENTRY_COUNT) { Random.nextFloat() * 20 }) }
        }
        delay(Defaults.TRANSACTION_INTERVAL_MS)
      }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeChart3(modelProducer, modifier)
    UIFramework.Views -> ViewChart3(modelProducer, modifier)
  }
}

@Composable
private fun ComposeChart3(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.rememberLine(
                remember { LineCartesianLayer.LineFill.single(fill(lineColor)) }
              )
            ),
          rangeProvider = rangeProvider,
        ),
        startAxis =
          VerticalAxis.rememberStart(
            guideline = null,
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            titleComponent =
              rememberTextComponent(
                color = Color.Black,
                margins = dimensions(end = 4.dp),
                padding = dimensions(8.dp, 2.dp),
                background = rememberShapeComponent(fill(lineColor), CorneredShape.Pill),
              ),
            title = stringResource(R.string.y_axis),
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            itemPlacer =
              remember { HorizontalAxis.ItemPlacer.aligned(addExtremeLabelPadding = false) },
            titleComponent =
              rememberTextComponent(
                color = Color.White,
                margins = dimensions(top = 4.dp),
                padding = dimensions(8.dp, 2.dp),
                background =
                  shapeComponent(fill(bottomAxisLabelBackgroundColor), CorneredShape.Pill),
              ),
            title = stringResource(R.string.x_axis),
          ),
        marker = rememberMarker(DefaultCartesianMarker.LabelPosition.AroundPoint),
        fadingEdges = rememberFadingEdges(),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
private fun ViewChart3(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker(DefaultCartesianMarker.LabelPosition.AroundPoint)

  AndroidViewBinding(Chart3Binding::inflate, modifier) {
    chartView.modelProducer = modelProducer
    val chart = requireNotNull(chartView.chart)
    val lineLayer = (chart.layers[0] as LineCartesianLayer).copy(rangeProvider = rangeProvider)
    chartView.chart = chart.copy(lineLayer, marker = marker)
  }
}

private val lineColor = Color(0xffffbb00)
private val bottomAxisLabelBackgroundColor = Color(0xff9db591)
private val rangeProvider =
  object : CartesianLayerRangeProvider {
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) = ceil(1.2 * maxY)
  }
