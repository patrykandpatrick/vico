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

import android.graphics.PorterDuff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.component
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shader.TopBottomShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.databinding.Chart9Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun Chart9(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          /* Learn more:
          https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/line-layer#data. */
          lineSeries { series(x = x, y = x.map { Random.nextFloat() * 30 - 10 }) }
        }
        delay(Defaults.TRANSACTION_INTERVAL_MS)
      }
    }
  }

  when (uiFramework) {
    UIFramework.Compose -> ComposeChart9(modelProducer, modifier)
    UIFramework.Views -> ViewChart9(modelProducer, modifier)
  }
}

@Composable
private fun ComposeChart9(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val colors = chartColors
  val marker = rememberMarker()
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lines =
            listOf(
              rememberLineSpec(
                shader =
                  TopBottomShader(DynamicShader.color(colors[0]), DynamicShader.color(colors[1])),
                backgroundShader =
                  TopBottomShader(
                    DynamicShader.compose(
                      DynamicShader.component(
                        componentSize = 6.dp,
                        component =
                          rememberShapeComponent(
                            shape = Shape.Pill,
                            color = colors[0],
                            margins = Dimensions.of(1.dp),
                          ),
                      ),
                      DynamicShader.verticalGradient(arrayOf(Color.Black, Color.Transparent)),
                      PorterDuff.Mode.DST_IN,
                    ),
                    DynamicShader.compose(
                      DynamicShader.component(
                        componentSize = 5.dp,
                        component =
                          rememberShapeComponent(
                            shape = Shape.Rectangle,
                            color = colors[1],
                            margins = Dimensions.of(horizontal = 2.dp),
                          ),
                        checkeredArrangement = false,
                      ),
                      DynamicShader.verticalGradient(arrayOf(Color.Transparent, Color.Black)),
                      PorterDuff.Mode.DST_IN,
                    ),
                  ),
              )
            )
        ),
        startAxis =
          rememberStartAxis(
            label =
              rememberAxisLabelComponent(
                color = MaterialTheme.colorScheme.onBackground,
                background =
                  rememberShapeComponent(
                    shape = Shape.Pill,
                    color = Color.Transparent,
                    strokeColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeWidth = 1.dp,
                  ),
                padding = Dimensions.of(horizontal = 6.dp, vertical = 2.dp),
                margins = Dimensions.of(end = 8.dp),
              ),
            axis = null,
            tick = null,
            guideline =
              rememberLineComponent(
                color = MaterialTheme.colorScheme.outlineVariant,
                shape =
                  remember { Shape.dashed(shape = Shape.Pill, dashLength = 4.dp, gapLength = 8.dp) },
              ),
            itemPlacer = remember { AxisItemPlacer.Vertical.count(count = { 4 }) },
          ),
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
    modifier = modifier,
    marker = marker,
    runInitialAnimation = false,
    horizontalLayout = HorizontalLayout.fullWidth(),
  )
}

@Composable
private fun ViewChart9(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker()
  val colors = chartColors
  AndroidViewBinding(Chart9Binding::inflate, modifier) {
    with(chartView) {
      runInitialAnimation = false
      this.modelProducer = modelProducer
      (chart?.bottomAxis as BaseAxis).guideline = null
      this.marker = marker
      with(chart?.layers?.get(0) as LineCartesianLayer) {
        lines =
          listOf(
            LineCartesianLayer.LineSpec(
              shader =
                TopBottomShader(DynamicShader.color(colors[0]), DynamicShader.color(colors[1])),
              backgroundShader =
                TopBottomShader(
                  DynamicShader.compose(
                    DynamicShader.component(
                      componentSize = 6.dp,
                      component =
                        ShapeComponent(
                          shape = Shape.Pill,
                          color = colors[0].toArgb(),
                          margins = Dimensions.of(1.dp),
                        ),
                    ),
                    DynamicShader.verticalGradient(arrayOf(Color.Black, Color.Transparent)),
                    PorterDuff.Mode.DST_IN,
                  ),
                  DynamicShader.compose(
                    DynamicShader.component(
                      componentSize = 5.dp,
                      component =
                        ShapeComponent(
                          shape = Shape.Rectangle,
                          color = colors[1].toArgb(),
                          margins = Dimensions.of(horizontal = 2.dp),
                        ),
                      checkeredArrangement = false,
                    ),
                    DynamicShader.verticalGradient(arrayOf(Color.Transparent, Color.Black)),
                    PorterDuff.Mode.DST_IN,
                  ),
                ),
            )
          )
      }
    }
  }
}

private val chartColors
  @ReadOnlyComposable
  @Composable
  get() =
    listOf(
      colorResource(id = R.color.chart_9_color_positive),
      colorResource(id = R.color.chart_9_color_negative),
    )

private val x = (1..100).toList()
