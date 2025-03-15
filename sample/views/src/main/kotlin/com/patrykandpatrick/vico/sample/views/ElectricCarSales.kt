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

package com.patrykandpatrick.vico.sample.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.graphics.ColorUtils
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.sample.views.databinding.ElectricCarSalesBinding
import java.text.DecimalFormat

private val x = (2010..2023).toList()
private val y = listOf<Number>(0.28, 1.4, 3.1, 5.8, 15, 22, 29, 39, 49, 56, 75, 86, 89, 93)
private val RangeProvider = CartesianLayerRangeProvider.fixed(maxY = 100.0)
private val YDecimalFormat = DecimalFormat("#.##'%'")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

@Composable
fun ViewElectricCarSales(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/vmml6t.
      lineSeries { series(x, y) }
    }
  }
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
              marker = getMarker(context, MarkerValueFormatter),
            )
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}
