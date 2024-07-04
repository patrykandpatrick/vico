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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.databinding.Chart5Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun Chart5(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          /* Learn more:
          https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/column-layer#data. */
          columnSeries {
            repeat(3) {
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
    UIFramework.Compose -> ComposeChart5(modelProducer, modifier)
    UIFramework.Views -> ViewChart5(modelProducer, modifier)
  }
}

@Composable
private fun ComposeChart5(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
              rememberLineComponent(
                color = color1,
                thickness = COLUMN_THICKNESS_DP.dp,
                shape =
                  Shape.rounded(
                    bottomLeftPercent = COLUMN_ROUNDNESS_PERCENT,
                    bottomRightPercent = COLUMN_ROUNDNESS_PERCENT,
                  ),
              ),
              rememberLineComponent(color = color2, thickness = COLUMN_THICKNESS_DP.dp),
              rememberLineComponent(
                color = color3,
                thickness = COLUMN_THICKNESS_DP.dp,
                shape =
                  Shape.rounded(
                    topLeftPercent = COLUMN_ROUNDNESS_PERCENT,
                    topRightPercent = COLUMN_ROUNDNESS_PERCENT,
                  ),
              ),
            ),
          mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
        ),
        startAxis =
          rememberStartAxis(
            itemPlacer = startAxisItemPlacer,
            labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES,
          ),
        bottomAxis = rememberBottomAxis(labelRotationDegrees = AXIS_LABEL_ROTATION_DEGREES),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    marker = rememberMarker(),
    runInitialAnimation = false,
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
private fun ViewChart5(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker()
  AndroidViewBinding(Chart5Binding::inflate, modifier) {
    with(chartView) {
      runInitialAnimation = false
      this.modelProducer = modelProducer
      (chart?.startAxis as VerticalAxis).itemPlacer = startAxisItemPlacer
      this.marker = marker
    }
  }
}

private const val COLUMN_ROUNDNESS_PERCENT: Int = 40
private const val COLUMN_THICKNESS_DP: Int = 10
private const val AXIS_LABEL_ROTATION_DEGREES = 45f

private val color1 = Color(0xff6438a7)
private val color2 = Color(0xff3490de)
private val color3 = Color(0xff73e8dc)
private val startAxisItemPlacer = AxisItemPlacer.Vertical.count({ 3 })
