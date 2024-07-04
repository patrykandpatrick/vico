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
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberFadingEdges
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.databinding.Chart3Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
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
          lines = listOf(rememberLineSpec(shader = DynamicShader.color(lineColor))),
          axisValueOverrider = axisValueOverrider,
        ),
        startAxis =
          rememberStartAxis(
            guideline = null,
            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            titleComponent =
              rememberTextComponent(
                color = Color.Black,
                background = rememberShapeComponent(Shape.Pill, lineColor),
                padding = Dimensions.of(horizontal = 8.dp, vertical = 2.dp),
                margins = Dimensions.of(end = 4.dp),
                typeface = Typeface.MONOSPACE,
              ),
            title = stringResource(R.string.y_axis),
          ),
        bottomAxis =
          rememberBottomAxis(
            titleComponent =
              rememberTextComponent(
                background = rememberShapeComponent(Shape.Pill, bottomAxisLabelBackgroundColor),
                color = Color.White,
                padding = Dimensions.of(horizontal = 8.dp, vertical = 2.dp),
                margins = Dimensions.of(top = 4.dp),
                typeface = Typeface.MONOSPACE,
              ),
            title = stringResource(R.string.x_axis),
          ),
        fadingEdges = rememberFadingEdges(),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    marker = rememberMarker(DefaultCartesianMarker.LabelPosition.AroundPoint),
    runInitialAnimation = false,
    horizontalLayout = HorizontalLayout.fullWidth(),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
private fun ViewChart3(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker(DefaultCartesianMarker.LabelPosition.AroundPoint)

  AndroidViewBinding(Chart3Binding::inflate, modifier) {
    with(chartView) {
      (chart?.layers?.get(0) as LineCartesianLayer?)?.axisValueOverrider = axisValueOverrider
      runInitialAnimation = false
      this.modelProducer = modelProducer
      this.marker = marker
    }
  }
}

private val lineColor = Color(0xffffbb00)
private val bottomAxisLabelBackgroundColor = Color(0xff9db591)
private val axisValueOverrider = AxisValueOverrider.adaptiveYValues(yFraction = 1.2f, round = true)
