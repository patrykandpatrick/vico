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
import com.patrykandpatrick.vico.compose.cartesian.decoration.rememberHorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.patrykandpatrick.vico.databinding.Chart2Binding
import com.patrykandpatrick.vico.sample.showcase.Defaults
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
internal fun Chart2(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    withContext(Dispatchers.Default) {
      while (isActive) {
        modelProducer.runTransaction {
          /* Learn more:
          https://patrykandpatrick.com/vico/wiki/cartesian-charts/layers/column-layer#data. */
          columnSeries { series(List(47) { 2 + Random.nextFloat() * 18 }) }
        }
        delay(Defaults.TRANSACTION_INTERVAL_MS)
      }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeChart2(modelProducer, modifier)
    UIFramework.Views -> ViewChart2(modelProducer, modifier)
  }
}

@Composable
private fun ComposeChart2(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(
              color = Color(0xffff5500),
              thickness = 16.dp,
              shape = remember { Shape.rounded(allPercent = 40) },
            )
          )
        ),
        startAxis = rememberStartAxis(),
        bottomAxis =
          rememberBottomAxis(
            valueFormatter = bottomAxisValueFormatter,
            itemPlacer =
              remember {
                HorizontalAxis.ItemPlacer.default(spacing = 3, addExtremeLabelPadding = true)
              },
          ),
        marker = rememberMarker(),
        horizontalLayout = HorizontalLayout.fullWidth(),
        decorations = listOf(rememberComposeHorizontalLine()),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
  )
}

@Composable
private fun ViewChart2(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker()
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      Chart2Binding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart?.decorations = listOf(getViewHorizontalLine())
          this.modelProducer = modelProducer
          chart?.bottomAxis =
            (chart?.bottomAxis as HorizontalAxis).copy(valueFormatter = bottomAxisValueFormatter)
          chart?.marker = marker
        }
      }
    },
    modifier,
  )
}

@Composable
private fun rememberComposeHorizontalLine(): HorizontalLine {
  val color = Color(HORIZONTAL_LINE_COLOR)
  return rememberHorizontalLine(
    y = { HORIZONTAL_LINE_Y },
    line = rememberLineComponent(color, HORIZONTAL_LINE_THICKNESS_DP.dp),
    labelComponent =
      rememberTextComponent(
        margins = Dimensions.of(HORIZONTAL_LINE_LABEL_MARGIN_DP.dp),
        padding =
          Dimensions.of(
            HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP.dp,
            HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP.dp,
          ),
        background = rememberShapeComponent(color, Shape.Pill),
      ),
  )
}

private fun getViewHorizontalLine() =
  HorizontalLine(
    y = { HORIZONTAL_LINE_Y },
    line = LineComponent(HORIZONTAL_LINE_COLOR, HORIZONTAL_LINE_THICKNESS_DP),
    labelComponent =
      TextComponent(
        margins = Dimensions(HORIZONTAL_LINE_LABEL_MARGIN_DP),
        padding =
          Dimensions(
            HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP,
            HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP,
          ),
        background = ShapeComponent(HORIZONTAL_LINE_COLOR, Shape.Pill),
      ),
  )

private const val HORIZONTAL_LINE_Y = 14.0
private const val HORIZONTAL_LINE_COLOR = -2893786
private const val HORIZONTAL_LINE_THICKNESS_DP = 2f
private const val HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP = 8f
private const val HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP = 2f
private const val HORIZONTAL_LINE_LABEL_MARGIN_DP = 4f

private val monthNames = DateFormatSymbols.getInstance(Locale.US).shortMonths
private val bottomAxisValueFormatter = CartesianValueFormatter { x, _, _ ->
  "${monthNames[x.toInt() % 12]} ’${20 + x.toInt() / 12}"
}
