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

import android.content.res.Configuration
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import com.patrykandpatrick.vico.R
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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.HorizontalLegend
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.databinding.DailyDigitalMediaUseBinding
import com.patrykandpatrick.vico.sample.PreviewSurface
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import java.text.DecimalFormat
import kotlinx.coroutines.runBlocking

private val LegendLabelKey = ExtraStore.Key<Set<String>>()
private val YDecimalFormat = DecimalFormat("#.## h")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val StartAxisItemPlacer = VerticalAxis.ItemPlacer.step({ 0.5 })
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

@Composable
private fun ComposeDigitalMediaUse(
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

@Composable
private fun ViewDigitalMediaUse(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker(MarkerValueFormatter)
  val context = LocalContext.current
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      val columnColors =
        listOf(
          ContextCompat.getColor(context, R.color.daily_digital_media_use_column_1_color),
          ContextCompat.getColor(context, R.color.daily_digital_media_use_column_2_color),
          ContextCompat.getColor(context, R.color.daily_digital_media_use_column_3_color),
        )
      val legendItemLabelComponent =
        TextComponent(
          if (
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
              Configuration.UI_MODE_NIGHT_YES
          ) {
            android.graphics.Color.WHITE
          } else {
            android.graphics.Color.BLACK
          }
        )
      val legend =
        HorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext>(
          items = { extraStore ->
            extraStore[LegendLabelKey].forEachIndexed { index, label ->
              add(
                LegendItem(
                  shapeComponent(Fill(columnColors[index]), CorneredShape.Pill),
                  legendItemLabelComponent,
                  label,
                )
              )
            }
          },
          padding = Insets(topDp = 16f),
        )
      DailyDigitalMediaUseBinding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart =
            chart!!.copy(
              startAxis =
                (chart!!.startAxis as VerticalAxis).copy(
                  valueFormatter = StartAxisValueFormatter,
                  itemPlacer = StartAxisItemPlacer,
                ),
              marker = marker,
              legend = legend,
            )
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
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
internal fun DailyDigitalMediaUse(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      /* Learn more: https://patrykandpatrick.com/eji9zq. */
      columnSeries { y.values.forEach { series(x, it) } }
      extras { it[LegendLabelKey] = y.keys }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeDigitalMediaUse(modelProducer, modifier)
    UIFramework.Views -> ViewDigitalMediaUse(modelProducer, modifier)
  }
}

@Preview
@Composable
private fun DailyDigitalMediaUsePreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking {
    modelProducer.runTransaction {
      columnSeries { y.values.forEach { series(x, it) } }
      extras { it[LegendLabelKey] = y.keys }
    }
  }
  PreviewSurface { ComposeDigitalMediaUse(modelProducer) }
}
