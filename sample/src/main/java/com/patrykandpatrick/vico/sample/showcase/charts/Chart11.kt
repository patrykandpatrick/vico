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


import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CandlestickCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.half
import com.patrykandpatrick.vico.databinding.Chart2Binding
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import java.text.DateFormatSymbols
import java.util.Locale
import kotlin.random.Random

@Composable
internal fun Chart11(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  val series = List(47) { 2 + Random.nextFloat() * 18 }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      columnSeries { series(series) }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeChart11(modelProducer, modifier, series)
    UIFramework.Views -> ViewChart11(modelProducer, modifier, series)
  }
}

@Composable
private fun ComposeChart11(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier,
  series: List<Float>,
) {
  val currentSelectedIndex = remember { mutableIntStateOf(series.lastIndex) }

  val color = remember { Color.Blue }
  val selectedColor = remember { Color.Green }
  val barThickness = remember { 10f }

  val columnProvider = remember {
    object : ColumnCartesianLayer.ColumnProvider {
      override fun getColumn(
        entry: ColumnCartesianLayerModel.Entry,
        seriesIndex: Int,
        extraStore: ExtraStore,
      ): LineComponent {
        val barColor = if (entry.x.toInt() == currentSelectedIndex.intValue) {
          selectedColor
        } else {
          color
        }
        return LineComponent(
          color = barColor.toArgb(),
          thicknessDp = barThickness,
        )
      }

      override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent {
        return LineComponent(
          color = color.toArgb(),
          thicknessDp = barThickness,
        )
      }
    }
  }

  val invisibleMarker = remember {
    object : CartesianMarker {
      override val displayOnTap: Boolean
        get() = true

      override fun draw(context: CartesianDrawingContext, targets: List<CartesianMarker.Target>) {
        context.canvas.drawRoundRect(
          targets.first().canvasX - 25,
          0f,
          targets.first().canvasX + 25,
          50f,
          3f,
          5f,
          Paint(),
        )
      }
    }
  }

  val persistentMarkers = remember(currentSelectedIndex.intValue) {
    mutableStateOf<(CartesianChart.PersistentMarkerScope.(ExtraStore) -> Unit)>(
      {
        invisibleMarker at currentSelectedIndex.intValue
      },
    )
  }

  val selectedBarListener = remember(series.size) {
    object : CartesianMarkerVisibilityListener {
      override fun onTap(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
        currentSelectedIndex.intValue = targets.first().x.toInt()
      }
    }
  }

  CartesianChartHost(
    chart =
    rememberCartesianChart(
      rememberColumnCartesianLayer(
        columnProvider = columnProvider,
      ),
      startAxis = VerticalAxis.rememberStart(),
      bottomAxis =
      HorizontalAxis.rememberBottom(
        valueFormatter = bottomAxisValueFormatter,
        itemPlacer =
        remember {
          HorizontalAxis.ItemPlacer.aligned(spacing = 3, addExtremeLabelPadding = true)
        },
      ),
      marker = invisibleMarker,
      persistentMarkers = persistentMarkers.value,
      markerVisibilityListener = selectedBarListener,
    ),
    modelProducer = modelProducer,
    modifier = modifier,
  )
}

@Composable
private fun ViewChart11(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier,
  series: List<Float>,
) {
  val currentSelectedIndex = remember { mutableIntStateOf(series.lastIndex) }
  val color = remember { Color.Blue }
  val selectedColor = remember { Color.Green }
  val barThickness = remember { 10f }

  val marker = remember {
    object : CartesianMarker {
      override val displayOnTap: Boolean
        get() = true

      override fun draw(context: CartesianDrawingContext, targets: List<CartesianMarker.Target>) {
        context.canvas.drawRoundRect(
          targets.first().canvasX - 25,
          0f,
          targets.first().canvasX + 25,
          50f,
          3f,
          5f,
          Paint(),
        )
      }
    }
  }

  val selectedBarListener = remember(series.size) {
    object : CartesianMarkerVisibilityListener {
      override fun onTap(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
        currentSelectedIndex.intValue = targets.first().x.toInt()
      }
    }
  }

  val columnProvider = remember {
    object : ColumnCartesianLayer.ColumnProvider {
      override fun getColumn(
        entry: ColumnCartesianLayerModel.Entry,
        seriesIndex: Int,
        extraStore: ExtraStore,
      ): LineComponent {
        val barColor = if (entry.x.toInt() == currentSelectedIndex.intValue) {
          selectedColor
        } else {
          color
        }
        return LineComponent(
          color = barColor.toArgb(),
          thicknessDp = barThickness,
        )
      }

      override fun getWidestSeriesColumn(seriesIndex: Int, extraStore: ExtraStore): LineComponent {
        return LineComponent(
          color = color.toArgb(),
          thicknessDp = barThickness,
        )
      }
    }
  }

  val layer = rememberColumnCartesianLayer(
    columnProvider = columnProvider,
  )

  val persistentMarkers = remember(currentSelectedIndex.intValue) {
    mutableStateOf<(CartesianChart.PersistentMarkerScope.(ExtraStore) -> Unit)>(
      {
        marker at currentSelectedIndex.intValue
      },
    )
  }

  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      Chart2Binding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          this.modelProducer = modelProducer
          chart = chart?.copy(
            bottomAxis =
            (chart?.bottomAxis as HorizontalAxis).copy(
              valueFormatter = bottomAxisValueFormatter,
            ),
            marker = marker,
            persistentMarkers = persistentMarkers.value,
            markerVisibilityListener = selectedBarListener,
            layers = arrayOf(layer),
          )
        }
      }
    },
    modifier,
  )
}

private val monthNames = DateFormatSymbols.getInstance(Locale.US).shortMonths
private val bottomAxisValueFormatter = CartesianValueFormatter { _, x, _ ->
  "${monthNames[x.toInt() % 12]} ’${20 + x.toInt() / 12}"
}
