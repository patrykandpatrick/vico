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

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.rememberLegendItem
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.databinding.Chart7Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun Chart7(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          /* Learn more:
          https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
          lineSeries {
            repeat(Defaults.MULTI_SERIES_COUNT) {
              series(
                List(Defaults.ENTRY_COUNT) {
                  Defaults.COLUMN_LAYER_MIN_Y +
                    Random.nextFloat() * Defaults.COLUMN_LAYER_RELATIVE_MAX_Y
                }
              )
            }
          }
        }
        delay(Defaults.TRANSACTION_INTERVAL_MS)
      }
    }
  }

  when (uiFramework) {
    UIFramework.Compose -> ComposeChart7(modelProducer, modifier)
    UIFramework.Views -> ViewChart7(modelProducer, modifier)
  }
}

@Composable
private fun ComposeChart7(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lines =
            chartColors.map { color ->
              rememberLineSpec(shader = DynamicShader.color(color), backgroundShader = null)
            }
        ),
        startAxis =
          rememberStartAxis(
            label = rememberStartAxisLabel(),
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
          ),
        bottomAxis = rememberBottomAxis(),
        legend = rememberLegend(),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    marker = rememberMarker(),
    runInitialAnimation = false,
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
private fun ViewChart7(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val startAxisLabel = rememberStartAxisLabel()
  val marker = rememberMarker()
  val legend = rememberLegend()
  AndroidViewBinding(Chart7Binding::inflate, modifier) {
    with(chartView) {
      runInitialAnimation = false
      this.modelProducer = modelProducer
      (chart?.startAxis as VerticalAxis).horizontalLabelPosition =
        VerticalAxis.HorizontalLabelPosition.Inside
      (chart?.startAxis as VerticalAxis).label = startAxisLabel
      this.marker = marker
      chart?.legend = legend
    }
  }
}

@Composable
private fun rememberStartAxisLabel() =
  rememberAxisLabelComponent(
    color = Color.Black,
    background = rememberShapeComponent(shape = Shape.rounded(4.dp), color = Color(0xfffab94d)),
    padding = Dimensions.of(horizontal = 8.dp, vertical = 2.dp),
    margins = Dimensions.of(all = 4.dp),
  )

@Composable
private fun rememberLegend() =
  rememberVerticalLegend<CartesianMeasureContext, CartesianDrawContext>(
    items =
      chartColors.mapIndexed { index, chartColor ->
        rememberLegendItem(
          icon = rememberShapeComponent(Shape.Pill, chartColor),
          label =
            rememberTextComponent(
              color = vicoTheme.textColor,
              textSize = 12.sp,
              typeface = Typeface.MONOSPACE,
            ),
          labelText = stringResource(R.string.series_x, index + 1),
        )
      },
    iconSize = 8.dp,
    iconPadding = 8.dp,
    spacing = 4.dp,
    padding = Dimensions.of(top = 8.dp),
  )

private val chartColors = listOf(Color(0xffb983ff), Color(0xff91b1fd), Color(0xff8fdaff))
