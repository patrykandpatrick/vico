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
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.VerticalLegend
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.databinding.AiTestScoresBinding
import com.patrykandpatrick.vico.sample.PreviewSurface
import com.patrykandpatrick.vico.sample.showcase.UIFramework
import com.patrykandpatrick.vico.sample.showcase.rememberMarker
import kotlinx.coroutines.runBlocking

private val LegendLabelKey = ExtraStore.Key<Set<String>>()

@Composable
private fun rememberComposeHorizontalLine(): HorizontalLine {
  val fill = fill(Color(0xfffdc8c4))
  val line = rememberLineComponent(fill = fill, thickness = 2.dp)
  val labelComponent =
    rememberTextComponent(
      margins = insets(start = 6.dp),
      padding = insets(start = 8.dp, end = 8.dp, bottom = 2.dp),
      background =
        shapeComponent(fill, CorneredShape.rounded(bottomLeft = 4.dp, bottomRight = 4.dp)),
    )
  return remember {
    HorizontalLine(
      y = { 0.0 },
      line = line,
      labelComponent = labelComponent,
      label = { "Human score" },
      verticalLabelPosition = Position.Vertical.Bottom,
    )
  }
}

@Composable
private fun ComposeAITestScores(
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  val lineColors = listOf(Color(0xff916cda), Color(0xffd877d8), Color(0xfff094bb))
  val legendItemLabelComponent = rememberTextComponent(vicoTheme.textColor)
  CartesianChartHost(
    rememberCartesianChart(
      rememberLineCartesianLayer(
        LineCartesianLayer.LineProvider.series(
          lineColors.map { color ->
            LineCartesianLayer.rememberLine(
              fill = LineCartesianLayer.LineFill.single(fill(color)),
              areaFill = null,
              pointProvider =
                LineCartesianLayer.PointProvider.single(
                  LineCartesianLayer.point(rememberShapeComponent(fill(color), CorneredShape.Pill))
                ),
            )
          }
        )
      ),
      startAxis = VerticalAxis.rememberStart(),
      bottomAxis = HorizontalAxis.rememberBottom(),
      marker = rememberMarker(),
      legend =
        rememberVerticalLegend(
          items = { extraStore ->
            extraStore[LegendLabelKey].forEachIndexed { index, label ->
              add(
                LegendItem(
                  shapeComponent(fill(lineColors[index]), CorneredShape.Pill),
                  legendItemLabelComponent,
                  label,
                )
              )
            }
          },
          padding = insets(top = 16.dp),
        ),
      decorations = listOf(rememberComposeHorizontalLine()),
    ),
    modelProducer,
    modifier.height(304.dp),
    rememberVicoScrollState(scrollEnabled = false),
  )
}

private fun getViewHorizontalLine(): HorizontalLine {
  val fill = Fill(0xfffdc8c4.toInt())
  return HorizontalLine(
    y = { 0.0 },
    line = LineComponent(fill = fill, thicknessDp = 2f),
    labelComponent =
      TextComponent(
        margins = Insets(startDp = 6f),
        padding = Insets(startDp = 8f, endDp = 8f, bottomDp = 2f),
        background =
          ShapeComponent(fill, CorneredShape.rounded(bottomLeftDp = 4f, bottomRightDp = 4f)),
      ),
    label = { "Human score" },
    verticalLabelPosition = Position.Vertical.Bottom,
  )
}

@Composable
private fun ViewAITestScores(modelProducer: CartesianChartModelProducer, modifier: Modifier) {
  val marker = rememberMarker()
  val context = LocalContext.current
  AndroidViewBinding(
    { inflater, parent, attachToParent ->
      val lineColors =
        listOf(
          ContextCompat.getColor(context, R.color.ai_test_score_line_1_color),
          ContextCompat.getColor(context, R.color.ai_test_score_line_2_color),
          ContextCompat.getColor(context, R.color.ai_test_score_line_3_color),
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
        VerticalLegend<CartesianMeasuringContext, CartesianDrawingContext>(
          items = { extraStore ->
            extraStore[LegendLabelKey].forEachIndexed { index, label ->
              add(
                LegendItem(
                  ShapeComponent(Fill(lineColors[index]), CorneredShape.Pill),
                  legendItemLabelComponent,
                  label,
                )
              )
            }
          },
          padding = Insets(topDp = 16f),
        )
      AiTestScoresBinding.inflate(inflater, parent, attachToParent).apply {
        with(chartView) {
          chart =
            chart!!.copy(
              marker = marker,
              legend = legend,
              decorations = listOf(getViewHorizontalLine()),
            )
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}

private val data =
  mapOf<String, Map<Int, Number>>(
    "Image recognition" to
      mapOf(
        2009 to -100,
        2012 to -44.16,
        2014 to -6.8,
        2015 to 0.69,
        2016 to 6.62,
        2018 to 11.69,
        2019 to 9.52,
        2020 to 16.45,
      ),
    "Nuanced-language interpretation" to mapOf(2019 to -100, 2021 to 2.73, 2022 to 8.2),
    "Programming" to mapOf(2021 to -100, 2022 to -48.04, 2023 to -12.64),
  )

@Composable
internal fun AITestScores(uiFramework: UIFramework, modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      /* Learn more: https://patrykandpatrick.com/vmml6t. */
      lineSeries { data.forEach { (_, map) -> series(map.keys, map.values) } }
      extras { extraStore -> extraStore[LegendLabelKey] = data.keys }
    }
  }
  when (uiFramework) {
    UIFramework.Compose -> ComposeAITestScores(modelProducer, modifier)
    UIFramework.Views -> ViewAITestScores(modelProducer, modifier)
  }
}

@Preview
@Composable
private fun AITestScorePreview() {
  val modelProducer = remember { CartesianChartModelProducer() }
  // Use `runBlocking` only for previews, which don’t support asynchronous execution.
  runBlocking {
    modelProducer.runTransaction {
      lineSeries { data.forEach { (_, map) -> series(map.keys, map.values) } }
      extras { extraStore -> extraStore[LegendLabelKey] = data.keys }
    }
  }
  PreviewSurface { ComposeAITestScores(modelProducer) }
}
