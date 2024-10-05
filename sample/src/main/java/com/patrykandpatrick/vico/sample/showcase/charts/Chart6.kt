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
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.copyColor
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
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
  val columnShape = CorneredShape.cut(topLeftPercent = 50)
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          ColumnCartesianLayer.ColumnProvider.series(
            columnColors.map {
              rememberLineComponent(color = it, thickness = 8.dp, shape = columnShape)
            }
          )
        ),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            valueFormatter = bottomAxisValueFormatter,
            itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
          ),
        marker = rememberMarker(),
        layerPadding =
          cartesianLayerPadding(scalableStartPadding = 16.dp, scalableEndPadding = 16.dp),
        decorations = listOf(rememberComposeHorizontalBox()),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

@Composable
private fun ViewChart6(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker()
  AndroidViewBinding(Chart6Binding::inflate, modifier) {
    chartView.modelProducer = modelProducer
    val chart = chartView.chart!!
    chartView.chart =
      chart.copy(
        bottomAxis =
          (chart.bottomAxis as HorizontalAxis).copy(valueFormatter = bottomAxisValueFormatter),
        decorations = listOf(getViewHorizontalBox()),
        marker = marker,
      )
  }
}

@Composable
private fun rememberComposeHorizontalBox(): HorizontalBox {
  val color = Color(HORIZONTAL_BOX_COLOR)
  val box = rememberShapeComponent(color = color.copy(HORIZONTAL_BOX_ALPHA))
  val labelComponent =
    rememberTextComponent(
      margins = dimensions(HORIZONTAL_BOX_LABEL_MARGIN_DP.dp),
      padding =
        dimensions(
          HORIZONTAL_BOX_LABEL_HORIZONTAL_PADDING_DP.dp,
          HORIZONTAL_BOX_LABEL_VERTICAL_PADDING_DP.dp,
        ),
      background = shapeComponent(color, Shape.Rectangle),
    )
  return remember { HorizontalBox({ horizontalBoxY }, box, labelComponent) }
}

private fun getViewHorizontalBox() =
  HorizontalBox(
    y = { horizontalBoxY },
    box = ShapeComponent(color = HORIZONTAL_BOX_COLOR.copyColor(HORIZONTAL_BOX_ALPHA)),
    labelComponent =
      TextComponent(
        margins = Dimensions(HORIZONTAL_BOX_LABEL_MARGIN_DP),
        padding =
          Dimensions(
            HORIZONTAL_BOX_LABEL_HORIZONTAL_PADDING_DP,
            HORIZONTAL_BOX_LABEL_VERTICAL_PADDING_DP,
          ),
        background = ShapeComponent(HORIZONTAL_BOX_COLOR, Shape.Rectangle),
      ),
  )

private const val HORIZONTAL_BOX_COLOR = -1448529
private const val HORIZONTAL_BOX_ALPHA = 0.36f
private const val HORIZONTAL_BOX_LABEL_HORIZONTAL_PADDING_DP = 8f
private const val HORIZONTAL_BOX_LABEL_VERTICAL_PADDING_DP = 2f
private const val HORIZONTAL_BOX_LABEL_MARGIN_DP = 4f

private val columnColors = listOf(Color(0xff3e6558), Color(0xff5e836a), Color(0xffa5ba8e))
private val horizontalBoxY = 7.0..14.0
private val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val bottomAxisValueFormatter = CartesianValueFormatter { _, x, _ ->
  daysOfWeek[x.toInt() % daysOfWeek.size]
}
