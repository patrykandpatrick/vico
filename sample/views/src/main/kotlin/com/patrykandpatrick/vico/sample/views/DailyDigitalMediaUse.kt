/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.views

import android.content.res.Configuration
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.ContextCompat
import com.patrykandpatrick.vico.sample.views.databinding.DailyDigitalMediaUseBinding
import com.patrykandpatrick.vico.views.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.views.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.views.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.views.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.views.cartesian.data.columnSeries
import com.patrykandpatrick.vico.views.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.views.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.views.common.Fill
import com.patrykandpatrick.vico.views.common.HorizontalLegend
import com.patrykandpatrick.vico.views.common.Insets
import com.patrykandpatrick.vico.views.common.LegendItem
import com.patrykandpatrick.vico.views.common.component.ShapeComponent
import com.patrykandpatrick.vico.views.common.component.TextComponent
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import com.patrykandpatrick.vico.views.common.shape.CorneredShape
import java.text.DecimalFormat

private val x = (2008..2018).toList()

private val y =
  mapOf(
    "Laptop/desktop" to listOf<Number>(2.2, 2.3, 2.4, 2.6, 2.5, 2.3, 2.2, 2.2, 2.2, 2.1, 2),
    "Mobile" to listOf(0.3, 0.3, 0.4, 0.8, 1.6, 2.3, 2.6, 2.8, 3.1, 3.3, 3.6),
    "Other" to listOf(0.2, 0.3, 0.4, 0.3, 0.3, 0.3, 0.3, 0.4, 0.4, 0.6, 0.7),
  )

private val LegendLabelKey = ExtraStore.Key<Set<String>>()
private val YDecimalFormat = DecimalFormat("#.## h")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val StartAxisItemPlacer = VerticalAxis.ItemPlacer.step({ 0.5 })
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

@Composable
fun ViewDailyDigitalMediaUse(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/eji9zq.
      columnSeries { y.values.forEach { series(x, it) } }
      extras { it[LegendLabelKey] = y.keys }
    }
  }
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
            Color.WHITE
          } else {
            Color.BLACK
          }
        )
      val legend =
        HorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext>(
          items = { extraStore ->
            extraStore[LegendLabelKey].forEachIndexed { index, label ->
              add(
                LegendItem(
                  ShapeComponent(Fill(columnColors[index]), CorneredShape.Pill),
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
              legend = legend,
              marker = getMarker(context, MarkerValueFormatter),
              markerController = CartesianMarkerController.toggleOnTap(),
            )
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}
