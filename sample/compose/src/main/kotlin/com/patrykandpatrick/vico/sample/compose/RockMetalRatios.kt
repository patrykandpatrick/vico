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

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
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
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.text.DecimalFormat
import kotlinx.coroutines.runBlocking

private const val Y_DIVISOR = 1000

private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()

private val YDecimalFormat = DecimalFormat("#.##K")

private val StartAxisValueFormatter = CartesianValueFormatter { _, value, _ ->
  YDecimalFormat.format(value / Y_DIVISOR)
}

private val BottomAxisValueFormatter = CartesianValueFormatter { context, x, _ ->
  context.model.extraStore[BottomAxisLabelKey][x.toInt()]
}

private val MarkerValueFormatter =
  DefaultCartesianMarker.ValueFormatter { _, targets ->
    val column = (targets[0] as ColumnCartesianLayerMarkerTarget).columns[0]
    SpannableStringBuilder()
      .append(
        YDecimalFormat.format(column.entry.y / Y_DIVISOR),
        ForegroundColorSpan(column.color),
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
      )
  }

@Composable
private fun JetpackComposeRockMetalRatios(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          onColumnClick = { entry, seriesIndex ->
            Log.d("ColumnCartesianLayer", "Clicked: Series $seriesIndex, Value ${entry.y} at X ${entry.x}")
//            setClickedInfo("Clicked: Series $seriesIndex, Value ${entry.y} at X ${entry.x}")
          },
          columnProvider = ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(fill = fill(Color(0xffff5500)), thickness = 16.dp)
          )
        ),
        startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() },
            valueFormatter = BottomAxisValueFormatter,
          ),
        marker = rememberMarker(MarkerValueFormatter),
        layerPadding = { cartesianLayerPadding(scalableStart = 8.dp, scalableEnd = 8.dp) },
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(220.dp),
    scrollState = rememberVicoScrollState(scrollEnabled = false),
  )
}

private val data =
  mapOf("Ag" to 22378, "Mo" to 4478, "U" to 3624, "Sn" to 2231, "Li" to 1634, "W" to 1081)

@Composable
fun JetpackComposeRockMetalRatios(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/eji9zq.
      columnSeries { series(data.values) }
      extras { it[BottomAxisLabelKey] = data.keys.toList() }
    }
  }
  JetpackComposeRockMetalRatios(modelProducer, modifier)
}

@Composable
@Preview
private fun Preview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/eji9zq.
      columnSeries { series(data.values) }
      extras { it[BottomAxisLabelKey] = data.keys.toList() }
    }
  }
  PreviewBox { JetpackComposeRockMetalRatios(modelProducer) }
}
