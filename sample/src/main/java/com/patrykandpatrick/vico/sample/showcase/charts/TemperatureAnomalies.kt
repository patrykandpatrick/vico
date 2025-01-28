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

package com.patrykandpatrick.vico.sample.showcase.charts

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.databinding.TemperatureAnomaliesBinding
import com.patrykandpatrick.vico.sample.PreviewSurface
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import com.patrykandpatrick.vico.views.cartesian.ScrollHandler
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlinx.coroutines.runBlocking

private const val RANGE_PROVIDER_BASE = 0.1

private val RangeProvider =
  object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
      -RANGE_PROVIDER_BASE * ceil(max(abs(minY), maxY) / RANGE_PROVIDER_BASE)

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
      -getMinY(minY, maxY, extraStore)
  }

private val YDecimalFormat = DecimalFormat("#.## °C;−#.## °C")

private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)

private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

private fun getColumnProvider(positive: LineComponent, negative: LineComponent) =
  object : ColumnCartesianLayer.ColumnProvider {
    override fun getColumn(
      entry: ColumnCartesianLayerModel.Entry,
      seriesIndex: Int,
      extraStore: ExtraStore,
    ) = if (entry.y >= 0) positive else negative

    override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore) = positive
  }

@Composable
private fun ComposeTemperatureAnomalies(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val positiveColumn =
    rememberLineComponent(
      fill = fill(Color(0xff0ac285)),
      thickness = 8.dp,
      shape = CorneredShape.rounded(topLeftPercent = 40, topRightPercent = 40),
    )
  val negativeColumn =
    rememberLineComponent(
      fill = fill(Color(0xffe8304f)),
      thickness = 8.dp,
      shape = CorneredShape.rounded(bottomLeftPercent = 40, bottomRightPercent = 40),
    )
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider =
            remember(positiveColumn, negativeColumn) {
              getColumnProvider(positiveColumn, negativeColumn)
            },
          columnCollectionSpacing = 4.dp,
          rangeProvider = RangeProvider,
        ),
        startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
        bottomAxis = HorizontalAxis.rememberBottom(labelRotationDegrees = 45f),
        marker = rememberMarker(MarkerValueFormatter),
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(242.dp),
    scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End),
  )
}

@Composable
private fun ViewTemperatureAnomalies(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier,
) {
  val marker = rememberMarker(MarkerValueFormatter)
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      TemperatureAnomaliesBinding.inflate(inflater, parent, attachToParent).apply {
        val positiveColumn =
          LineComponent(
            fill = Fill(0xff0ac285.toInt()),
            thicknessDp = 8f,
            shape = CorneredShape.rounded(topLeftPercent = 40, topRightPercent = 40),
          )
        val negativeColumn =
          LineComponent(
            fill = Fill(0xffe8304f.toInt()),
            thicknessDp = 8f,
            shape = CorneredShape.rounded(bottomLeftPercent = 40, bottomRightPercent = 40),
          )
        with(chartView) {
          chart =
            chart!!.copy(
              (chart!!.layers[0] as ColumnCartesianLayer).copy(
                columnProvider = getColumnProvider(positiveColumn, negativeColumn),
                rangeProvider = RangeProvider,
              ),
              startAxis =
                (chart!!.startAxis as VerticalAxis).copy(valueFormatter = StartAxisValueFormatter),
              marker = marker,
            )
          this.modelProducer = modelProducer
          scrollHandler = ScrollHandler(initialScroll = Scroll.Absolute.End)
        }
      }
    },
    modifier,
  )
}

private val x = (1940..2024).toList()

private val y =
  listOf(
    -0.6681757,
    -0.49279118,
    -0.6796627,
    -0.7625942,
    -0.5167904,
    -0.5330181,
    -0.63816166,
    -0.5190487,
    -0.60219,
    -0.6836748,
    -0.64020824,
    -0.50012875,
    -0.5439844,
    -0.52441025,
    -0.72957134,
    -0.7196655,
    -0.79957485,
    -0.47146702,
    -0.54053307,
    -0.5163603,
    -0.56854534,
    -0.46285343,
    -0.6544447,
    -0.51910305,
    -0.6430321,
    -0.6732807,
    -0.5111141,
    -0.5937309,
    -0.6851549,
    -0.4773512,
    -0.5026636,
    -0.72236156,
    -0.41237164,
    -0.45542336,
    -0.7194414,
    -0.69483566,
    -0.74333096,
    -0.38822365,
    -0.62572765,
    -0.43188572,
    -0.25387764,
    -0.24944305,
    -0.42020607,
    -0.26593018,
    -0.50597286,
    -0.46650124,
    -0.44809914,
    -0.19133568,
    -0.15690517,
    -0.4063406,
    -0.18449306,
    0.027781487,
    -0.3227768,
    -0.30272102,
    -0.25410366,
    -0.0861969,
    -0.31589794,
    -0.08015442,
    0.18309498,
    -0.23231792,
    -0.24404716,
    -0.11108303,
    0.023076057,
    -0.13892269,
    -0.093503,
    0.07637882,
    0.08110142,
    -0.023880005,
    -0.19847584,
    0.0043258667,
    0.086499214,
    0.020365715,
    0.11935711,
    0.107367516,
    0.07204723,
    0.18616772,
    0.26113796,
    0.2018156,
    0.22495174,
    0.36853504,
    0.36007214,
    0.21241856,
    0.3078394,
    0.531106,
    0.6741648,
  )

@Composable
internal fun TemperatureAnomalies(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      /* Learn more: https://patrykandpatrick.com/eji9zq. */
      columnSeries { series(x, y) }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeTemperatureAnomalies(modelProducer, modifier)
    UIFramework.Views -> ViewTemperatureAnomalies(modelProducer, modifier)
  }
}

@Preview
@Composable
private fun TemperatureAnomaliesPreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution
  runBlocking { modelProducer.runTransaction { columnSeries { series(x, y) } } }
  PreviewSurface { ComposeTemperatureAnomalies(modelProducer) }
}
