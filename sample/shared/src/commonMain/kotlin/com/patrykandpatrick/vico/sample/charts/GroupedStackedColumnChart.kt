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

package com.patrykandpatrick.vico.sample.charts

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme

private val LegendLabelKey = ExtraStore.Key<Set<String>>()

private val Months = listOf("Jan", "Feb", "Mar", "Apr")

private val X = listOf<Number>(0, 1, 2, 3)

private val Y =
  mapOf(
    "Income" to listOf(8, 9, 10, 11),
    "House" to listOf(3, 3, 4, 3),
    "Car" to listOf(2, 2, 1, 2),
    "Food" to listOf(1, 2, 2, 2),
  )

private val BottomAxisValueFormatter =
  object : CartesianValueFormatter {
    override fun format(
      context: CartesianMeasuringContext,
      value: Double,
      verticalAxisPosition: Axis.Position.Vertical?,
    ) =
      value
        .takeIf { it.isFinite() && it == it.toInt().toDouble() }
        ?.let { Months.getOrElse(it.toInt()) { "" } }
        ?: ""
  }

@Composable
private fun ComposeGroupedStackedColumnChart(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val columnColors = listOf(Color(0xff4f6bed), Color(0xfff2a65a), Color(0xffdc6b87), Color(0xff73b88d))
  val legendItemLabelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor, 12.sp))
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
              columnColors.map { color -> rememberLineComponent(fill = Fill(color), thickness = 16.dp) }
            ),
          mergeMode = {
            ColumnCartesianLayer.MergeMode.GroupedStacked(
              groupKeySelector = { seriesKey -> if (seriesKey == "Income") "Income" else "Expenses" }
            )
          },
        ),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            valueFormatter = BottomAxisValueFormatter,
            titleComponent = rememberTextComponent(TextStyle(vicoTheme.textColor, 12.sp)),
            title = { "Month" },
          ),
        legend =
          rememberHorizontalLegend(
            items = { extraStore ->
              extraStore[LegendLabelKey].forEachIndexed { index, label ->
                add(
                  LegendItem(
                    ShapeComponent(Fill(columnColors[index]), CircleShape),
                    legendItemLabelComponent,
                    label,
                  )
                )
              }
            },
            padding = Insets(top = 16.dp),
          ),
      ),
    modelProducer = modelProducer,
    modifier = modifier,
  )
}

@Composable
fun ComposeGroupedStackedColumnChart(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      columnModel { Y.forEach { (key, values) -> series(x = X, y = values, key = key) } }
      extras { it[LegendLabelKey] = Y.keys }
    }
  }
  ComposeGroupedStackedColumnChart(modelProducer, modifier)
}

@Composable
@Preview
fun ComposeGroupedStackedColumnChartPreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking?.invoke {
    modelProducer.runTransaction {
      columnModel { Y.forEach { (key, values) -> series(x = X, y = values, key = key) } }
      extras { it[LegendLabelKey] = Y.keys }
    }
  }
  PreviewBox { ComposeGroupedStackedColumnChart(modelProducer) }
}
