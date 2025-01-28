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
import androidx.core.graphics.ColorUtils
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.databinding.ElectricCarSalesBinding
import com.patrykandpatrick.vico.sample.PreviewSurface
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import java.text.DecimalFormat
import kotlinx.coroutines.runBlocking

private val RangeProvider = CartesianLayerRangeProvider.fixed(maxY = 100.0)
private val YDecimalFormat = DecimalFormat("#.##'%'")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

@Composable
private fun ComposeElectricCarSales(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val lineColor = Color(0xffa485e0)
  CartesianChartHost(
    rememberCartesianChart(
      rememberLineCartesianLayer(
        lineProvider =
          LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.rememberLine(
              fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
              areaFill =
                LineCartesianLayer.AreaFill.single(
                  fill(
                    ShaderProvider.verticalGradient(
                      arrayOf(lineColor.copy(alpha = 0.4f), Color.Transparent)
                    )
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
    modifier.height(224.dp),
    rememberVicoScrollState(scrollEnabled = false),
  )
}

@Composable
private fun ViewElectricCarSales(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker(MarkerValueFormatter)
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      val lineColor = 0xffa485e0.toInt()
      val lineProvider =
        LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
            areaFill =
              LineCartesianLayer.AreaFill.single(
                Fill(
                  ShaderProvider.verticalGradient(
                    ColorUtils.setAlphaComponent(lineColor, 102),
                    android.graphics.Color.TRANSPARENT,
                  )
                )
              ),
          )
        )
      ElectricCarSalesBinding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart =
            chart!!.copy(
              (chart!!.layers[0] as LineCartesianLayer).copy(
                lineProvider = lineProvider,
                rangeProvider = RangeProvider,
              ),
              startAxis =
                (chart!!.startAxis as VerticalAxis).copy(valueFormatter = StartAxisValueFormatter),
              marker = marker,
            )
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}

private val x = (2010..2023).toList()
private val y = listOf<Number>(0.28, 1.4, 3.1, 5.8, 15, 22, 29, 39, 49, 56, 75, 86, 89, 93)

@Composable
internal fun ElectricCarSales(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      /* Learn more: https://patrykandpatrick.com/vmml6t. */
      lineSeries { series(x, y) }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeElectricCarSales(modelProducer, modifier)
    UIFramework.Views -> ViewElectricCarSales(modelProducer, modifier)
  }
}

@Preview
@Composable
private fun ElectricCarSalePreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution
  runBlocking { modelProducer.runTransaction { lineSeries { series(x, y) } } }
  PreviewSurface { ComposeElectricCarSales(modelProducer) }
}
