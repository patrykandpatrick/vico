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

package com.patrykandpatrick.vico.sample.multiplatform

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.multiplatform.cartesian.data.columnSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.multiplatform.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore

private const val Y_DIVISOR = 1000

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()

private val StartAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
  "${(value / Y_DIVISOR).toInt()}K"
}

private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
  context.model.extraStore[BottomAxisLabelKey][x.toInt()]
}

private val MarkerValueFormatter =
  DefaultCartesianMarker.ValueFormatter { _, targets ->
    val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
    buildAnnotatedString {
      withStyle(SpanStyle(column.color)) {
        val value = (column.entry.y / Y_DIVISOR).toString()
        val decimalSeparatorIndex = value.indexOf('.')
        if (decimalSeparatorIndex >= 0) {
          append(value.substring(0, decimalSeparatorIndex + 3))
        } else {
          append(value)
        }
        append("K")
      }
    }
  }

private val data =
  mapOf("Ag" to 22378, "Mo" to 4478, "U" to 3624, "Sn" to 2231, "Li" to 1634, "W" to 1081)

@Composable
fun ComposeMultiplatformRockMetalRatios(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/3aqy4o.
      columnSeries { series(data.values) }
      extras { it[BottomAxisLabelKey] = data.keys.toList() }
    }
  }
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(fill = Fill(Color(0xffff5500)), thickness = 16.dp)
          )
        ),
        startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
            valueFormatter = BottomAxisValueFormatter,
          ),
        marker = rememberMarker(MarkerValueFormatter),
        layerPadding = { CartesianLayerPadding(scalableStart = 8.dp, scalableEnd = 8.dp) },
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(216.dp),
    scrollState = rememberVicoScrollState(scrollEnabled = false),
  )
}
