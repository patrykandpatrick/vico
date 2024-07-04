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
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.RandomCartesianModelGenerator
import com.patrykandpatrick.vico.databinding.Chart10Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun Chart10(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(key1 = Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          /* Learn more:
          https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/candlestick-layer#data. */
          add(RandomCartesianModelGenerator.getRandomCandlestickLayerModelPartial())
        }
        delay(Defaults.TRANSACTION_INTERVAL_MS)
      }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeChart10(modelProducer, modifier)
    UIFramework.Views -> ViewChart10(modelProducer, modifier)
  }
}

@Composable
private fun ComposeChart10(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker(showIndicator = false)
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberCandlestickCartesianLayer(),
        startAxis = rememberStartAxis(),
        bottomAxis =
          rememberBottomAxis(
            guideline = null,
            itemPlacer =
              remember {
                AxisItemPlacer.Horizontal.default(spacing = 3, addExtremeLabelPadding = true)
              },
          ),
      ),
    modelProducer = modelProducer,
    marker = marker,
    modifier = modifier,
    horizontalLayout = HorizontalLayout.fullWidth(),
  )
}

@Composable
private fun ViewChart10(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker(showIndicator = false)
  AndroidViewBinding(Chart10Binding::inflate, modifier = modifier) {
    chartView.modelProducer = modelProducer
    chartView.marker = marker
  }
}
