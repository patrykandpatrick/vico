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

package com.patrykandpatrick.vico.sample.compose

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.cartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.DecimalFormat
import kotlinx.coroutines.runBlocking

private val LegendLabelKey = ExtraStore.Key<Set<String>>()
private val YDecimalFormat = DecimalFormat("#.## h")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val StartAxisItemPlacer = VerticalAxis.ItemPlacer.step({ 0.5 })
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

@Composable
private fun JetpackComposeDailyDigitalMediaUse(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val columnColors = listOf(Color(0xff6438a7), Color(0xff3490de), Color(0xff73e8dc))
  val legendItemLabelComponent = rememberTextComponent(vicoTheme.textColor)
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
              columnColors.map { color ->
                rememberLineComponent(fill = fill(color), thickness = 16.dp)
              }
            ),
          columnCollectionSpacing = 32.dp,
          mergeMode = { ColumnCartesianLayer.MergeMode.stacked() },
        ),
        startAxis =
          VerticalAxis.rememberStart(
            valueFormatter = StartAxisValueFormatter,
            itemPlacer = StartAxisItemPlacer,
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() }
          ),
        marker = rememberMarker(MarkerValueFormatter),
        layerPadding = { cartesianLayerPadding(scalableStart = 16.dp, scalableEnd = 16.dp) },
        legend =
          rememberHorizontalLegend(
            items = { extraStore ->
              extraStore[LegendLabelKey].forEachIndexed { index, label ->
                add(
                  LegendItem(
                    shapeComponent(fill(columnColors[index]), CorneredShape.Pill),
                    legendItemLabelComponent,
                    label,
                  )
                )
              }
            },
            padding = insets(top = 16.dp),
          ),
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(256.dp),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

private val x = (2008..2018).toList()

private val y =
  mapOf(
    "Laptop/desktop" to listOf<Number>(2.2, 2.3, 2.4, 2.6, 2.5, 2.3, 2.2, 2.2, 2.2, 2.1, 2),
    "Mobile" to listOf(0.3, 0.3, 0.4, 0.8, 1.6, 2.3, 2.6, 2.8, 3.1, 3.3, 3.6),
    "Other" to listOf(0.2, 0.3, 0.4, 0.3, 0.3, 0.3, 0.3, 0.4, 0.4, 0.6, 0.7),
  )

@Composable
fun JetpackComposeDailyDigitalMediaUse(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/eji9zq.
      columnSeries { y.values.forEach { series(x, it) } }
      extras { it[LegendLabelKey] = y.keys }
    }
  }
  JetpackComposeDailyDigitalMediaUse(modelProducer, modifier)
}

@Composable
@Preview
private fun Preview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/eji9zq.
      columnSeries { y.values.forEach { series(x, it) } }
      extras { it[LegendLabelKey] = y.keys }
    }
  }
  PreviewBox { JetpackComposeDailyDigitalMediaUse(modelProducer) }
}
