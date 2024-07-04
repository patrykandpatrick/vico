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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.decoration.rememberHorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.copyColor
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.databinding.Chart6Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun Chart6(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          /* Learn more:
          https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/column-layer#data. */
          columnSeries {
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
    UIFramework.Compose -> ComposeChart6(modelProducer, modifier)
    UIFramework.Views -> ViewChart6(modelProducer, modifier)
  }
}

@Composable
private fun ComposeChart6(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val horizontalBox = rememberComposeHorizontalBox()
  val shape = remember { Shape.cut(topLeftPercent = 50) }
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          ColumnCartesianLayer.ColumnProvider.series(
            columnColors.map { rememberLineComponent(color = it, thickness = 8.dp, shape = shape) }
          )
        ),
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter),
        decorations = remember(horizontalBox) { listOf(horizontalBox) },
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    marker = rememberMarker(),
    runInitialAnimation = false,
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
private fun ViewChart6(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker()
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      Chart6Binding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart?.addDecoration(getViewHorizontalBox())
          runInitialAnimation = false
          this.modelProducer = modelProducer
          (chart?.bottomAxis as HorizontalAxis<AxisPosition.Horizontal.Bottom>).valueFormatter =
            bottomAxisValueFormatter
          this.marker = marker
        }
      }
    },
    modifier,
  )
}

@Composable
private fun rememberComposeHorizontalBox(): HorizontalBox {
  val color = Color(HORIZONTAL_BOX_COLOR)
  return rememberHorizontalBox(
    y = { horizontalBoxY },
    box = rememberShapeComponent(color = color.copy(HORIZONTAL_BOX_ALPHA)),
    labelComponent =
      rememberTextComponent(
        background = rememberShapeComponent(Shape.Rectangle, color),
        padding =
          Dimensions.of(
            HORIZONTAL_BOX_LABEL_HORIZONTAL_PADDING_DP.dp,
            HORIZONTAL_BOX_LABEL_VERTICAL_PADDING_DP.dp,
          ),
        margins = Dimensions.of(HORIZONTAL_BOX_LABEL_MARGIN_DP.dp),
        typeface = Typeface.MONOSPACE,
      ),
  )
}

private fun getViewHorizontalBox() =
  HorizontalBox(
    y = { horizontalBoxY },
    box = ShapeComponent(color = HORIZONTAL_BOX_COLOR.copyColor(HORIZONTAL_BOX_ALPHA)),
    labelComponent =
      TextComponent.build {
        typeface = Typeface.MONOSPACE
        background = ShapeComponent(Shape.Rectangle, HORIZONTAL_BOX_COLOR)
        padding =
          Dimensions(
            HORIZONTAL_BOX_LABEL_HORIZONTAL_PADDING_DP,
            HORIZONTAL_BOX_LABEL_VERTICAL_PADDING_DP,
          )
        margins = Dimensions(HORIZONTAL_BOX_LABEL_MARGIN_DP)
      },
  )

private const val HORIZONTAL_BOX_COLOR = -1448529
private const val HORIZONTAL_BOX_ALPHA = .36f
private const val HORIZONTAL_BOX_LABEL_HORIZONTAL_PADDING_DP = 8f
private const val HORIZONTAL_BOX_LABEL_VERTICAL_PADDING_DP = 2f
private const val HORIZONTAL_BOX_LABEL_MARGIN_DP = 4f

private val columnColors = listOf(Color(0xff3e6558), Color(0xff5e836a), Color(0xffa5ba8e))
private val horizontalBoxY = 7f..14f
private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val bottomAxisValueFormatter = CartesianValueFormatter { x, _, _ ->
  daysOfWeek[x.toInt() % daysOfWeek.size]
}
