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
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shader.component
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shader.ComponentShader
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
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
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              rememberLine(
                fill =
                  remember(colors) {
                    LineCartesianLayer.LineFill.double(fill(colors[0]), fill(colors[1]))
                  },
                areaFill =
                  remember(colors) {
                    LineCartesianLayer.AreaFill.double(
                      topFill =
                        fill(
                          DynamicShader.compose(
                            DynamicShader.component(
                              component =
                                shapeComponent(
                                  color = colors[0],
                                  shape = Shape.Pill,
                                  margins = Dimensions.of(1.dp),
                                ),
                              componentSize = 6.dp,
                            ),
                            DynamicShader.verticalGradient(arrayOf(Color.Black, Color.Transparent)),
                            PorterDuff.Mode.DST_IN,
                          )
                        ),
                      bottomFill =
                        fill(
                          DynamicShader.compose(
                            DynamicShader.component(
                              component =
                                shapeComponent(
                                  color = colors[1],
                                  shape = Shape.Rectangle,
                                  margins = Dimensions.of(horizontal = 2.dp),
                                ),
                              componentSize = 5.dp,
                              checkeredArrangement = false,
                            ),
                            DynamicShader.verticalGradient(arrayOf(Color.Transparent, Color.Black)),
                            PorterDuff.Mode.DST_IN,
                          )
                        ),
                    )
                  },
              )
            )
        ),
        startAxis =
          rememberStartAxis(
            label =
              rememberAxisLabelComponent(
                color = MaterialTheme.colorScheme.onBackground,
                margins = Dimensions.of(end = 8.dp),
                padding = Dimensions.of(6.dp, 2.dp),
                background =
                  rememberShapeComponent(
                    color = Color.Transparent,
                    shape = Shape.Pill,
                    strokeColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeThickness = 1.dp,
                  ),
              ),
            line = null,
            tick = null,
            guideline =
              rememberLineComponent(
                color = MaterialTheme.colorScheme.outlineVariant,
                shape =
                  remember { Shape.dashed(shape = Shape.Pill, dashLength = 4.dp, gapLength = 8.dp) },
              ),
            itemPlacer = remember { VerticalAxis.ItemPlacer.count(count = { 4 }) },
          ),
        bottomAxis =
          rememberBottomAxis(
            guideline = null,
            itemPlacer =
              remember {
                HorizontalAxis.ItemPlacer.default(spacing = 3, addExtremeLabelPadding = true)
              },
          ),
        marker = marker,
        horizontalLayout = HorizontalLayout.fullWidth(),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
  )
}

@Composable
private fun ViewChart9(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker()
  val colors = chartColors
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      Chart9Binding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          this.modelProducer = modelProducer
          (chart?.bottomAxis as BaseAxis).guideline = null
          chart?.marker = marker
          with(chart?.layers?.get(0) as LineCartesianLayer) {
            lineProvider =
              LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                  fill =
                    LineCartesianLayer.LineFill.double(
                      topFill = Fill(colors[0].toArgb()),
                      bottomFill = Fill(colors[1].toArgb()),
                    ),
                  areaFill =
                    LineCartesianLayer.AreaFill.double(
                      topFill =
                        Fill(
                          DynamicShader.compose(
                            ComponentShader(
                              component =
                                ShapeComponent(
                                  color = colors[0].toArgb(),
                                  shape = Shape.Pill,
                                  margins = Dimensions(allDp = 1f),
                                ),
                              componentSizeDp = 6f,
                            ),
                            DynamicShader.verticalGradient(
                              android.graphics.Color.BLACK,
                              android.graphics.Color.TRANSPARENT,
                            ),
                            PorterDuff.Mode.DST_IN,
                          )
                        ),
                      bottomFill =
                        Fill(
                          DynamicShader.compose(
                            ComponentShader(
                              component =
                                ShapeComponent(
                                  color = colors[1].toArgb(),
                                  shape = Shape.Rectangle,
                                  margins = Dimensions(horizontalDp = 2f, verticalDp = 0f),
                                ),
                              componentSizeDp = 5f,
                              checkeredArrangement = false,
                            ),
                            DynamicShader.verticalGradient(
                              android.graphics.Color.TRANSPARENT,
                              android.graphics.Color.BLACK,
                            ),
                            PorterDuff.Mode.DST_IN,
                          )
                        ),
                    ),
                )
              )
          }
        }
      }
    },
    modifier,
  )
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
