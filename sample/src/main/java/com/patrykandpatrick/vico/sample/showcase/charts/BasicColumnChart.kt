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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.databinding.BasicColumnChartBinding
import com.patrykandpatrick.vico.sample.showcase.UIFramework

@Composable
private fun ComposeBasicColumnChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier,
) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
  )
}

@Composable
private fun ViewBasicColumnChart(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      BasicColumnChartBinding.inflate(inflater, parent, attachToParent).apply {
        chartView.modelProducer = modelProducer
      }
    },
    modifier,
  )
}

@Composable
internal fun BasicColumnChart(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      /* Learn more: https://patrykandpatrick.com/eji9zq. */
      columnSeries { series(5, 6, 5, 2, 11, 8, 5, 2, 15, 11, 8, 13, 12, 10, 2, 7) }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeBasicColumnChart(modelProducer, modifier)
    UIFramework.Views -> ViewBasicColumnChart(modelProducer, modifier)
  }
}
