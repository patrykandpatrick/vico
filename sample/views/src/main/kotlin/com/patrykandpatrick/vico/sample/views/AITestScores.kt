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
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
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
import com.patrykandpatrick.vico.sample.views.databinding.AiTestScoresBinding

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

private val LegendLabelKey = ExtraStore.Key<Set<String>>()

private fun getHorizontalLine(): HorizontalLine {
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
fun ViewAITestScores(modifier: Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/vmml6t.
      lineSeries { data.forEach { (_, map) -> series(map.keys, map.values) } }
      extras { extraStore -> extraStore[LegendLabelKey] = data.keys }
    }
  }
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
            Color.WHITE
          } else {
            Color.BLACK
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
          chart = chart!!.copy(legend = legend, decorations = listOf(getHorizontalLine()))
          this.modelProducer = modelProducer
        }
      }
    },
    modifier,
  )
}
