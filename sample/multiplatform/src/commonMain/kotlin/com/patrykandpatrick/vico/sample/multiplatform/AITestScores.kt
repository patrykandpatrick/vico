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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.multiplatform.cartesian.data.lineSeries
import com.patrykandpatrick.vico.multiplatform.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.multiplatform.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.multiplatform.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.multiplatform.common.Fill
import com.patrykandpatrick.vico.multiplatform.common.Insets
import com.patrykandpatrick.vico.multiplatform.common.LegendItem
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.component.ShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberLineComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.rememberTextComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.rememberVerticalLegend
import com.patrykandpatrick.vico.multiplatform.common.shape.CorneredShape
import com.patrykandpatrick.vico.multiplatform.common.vicoTheme

private val LegendLabelKey = ExtraStore.Key<Set<String>>()

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
private fun rememberHorizontalLine(): HorizontalLine {
  val fill = Fill(Color(0xfffdc8c4))
  val line = rememberLineComponent(fill = fill, thickness = 2.dp)
  val labelComponent =
    rememberTextComponent(
      margins = Insets(start = 6.dp),
      padding = Insets(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 4.dp),
      background =
        rememberShapeComponent(fill, CorneredShape.rounded(bottomLeft = 4.dp, bottomRight = 4.dp)),
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
fun ComposeMultiplatformAITestScores(modifier: Modifier = Modifier) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(Unit) {
    modelProducer.runTransaction {
      // Learn more: https://patrykandpatrick.com/z5ah6v.
      lineSeries { data.forEach { (_, map) -> series(map.keys, map.values) } }
      extras { extraStore -> extraStore[LegendLabelKey] = data.keys }
    }
  }
  val lineColors = listOf(Color(0xff916cda), Color(0xffd877d8), Color(0xfff094bb))
  val legendItemLabelComponent = rememberTextComponent(TextStyle(vicoTheme.textColor, 12.sp))
  CartesianChartHost(
    rememberCartesianChart(
      rememberLineCartesianLayer(
        LineCartesianLayer.LineProvider.series(
          lineColors.map { color ->
            LineCartesianLayer.rememberLine(
              fill = LineCartesianLayer.LineFill.single(Fill(color)),
              areaFill = null,
              pointProvider =
                LineCartesianLayer.PointProvider.single(
                  LineCartesianLayer.Point(rememberShapeComponent(Fill(color), CorneredShape.Pill))
                ),
            )
          }
        )
      ),
      startAxis = VerticalAxis.rememberStart(),
      bottomAxis = HorizontalAxis.rememberBottom(),
      legend =
        rememberVerticalLegend(
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
          padding = Insets(top = 16.dp),
        ),
      decorations = listOf(rememberHorizontalLine()),
      marker = rememberMarker(),
    ),
    modelProducer,
    modifier.height(294.dp),
    rememberVicoScrollState(scrollEnabled = false),
  )
}
