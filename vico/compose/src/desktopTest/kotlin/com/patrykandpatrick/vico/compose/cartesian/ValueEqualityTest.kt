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

@file:OptIn(ExperimentalTestApi::class)

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberCandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

/** Guards against charts being recreated on every recomposition because of identity equality. */
class ValueEqualityTest {
  @Test
  fun `Charts built with default arguments are stable across recompositions`() = runComposeUiTest {
    val lineModel = LineCartesianLayerModel.build { series(1, 2, 3) }
    val columnModel = ColumnCartesianLayerModel.build { series(1, 2, 3) }
    val candlestickModel =
      CandlestickCartesianLayerModel.build(listOf(1, 2), listOf(2, 1), listOf(0, 0), listOf(3, 3))
    val pieModel = PieChartModel.build(1, 2)
    var trigger by mutableIntStateOf(0)
    val charts = mutableListOf<List<Any>>()
    setContent {
      Charts(trigger, lineModel, columnModel, candlestickModel, pieModel) { charts += it }
    }
    waitForIdle()
    runOnIdle { trigger++ }
    waitForIdle()
    val (first, second) = charts.takeLast(2)
    first.zip(second).forEach { (a, b) -> assertSame(a, b) }
  }

  @Composable
  private fun Charts(
    trigger: Int,
    lineModel: LineCartesianLayerModel,
    columnModel: ColumnCartesianLayerModel,
    candlestickModel: CandlestickCartesianLayerModel,
    pieModel: PieChartModel,
    record: (List<Any>) -> Unit,
  ) {
    val label = rememberTextComponent()
    val cartesianChart =
      rememberCartesianChart(
        rememberLineCartesianLayer(lineModel),
        rememberColumnCartesianLayer(columnModel),
        rememberCandlestickCartesianLayer(candlestickModel),
        startAxis = VerticalAxis.rememberStart(),
        bottomAxis = HorizontalAxis.rememberBottom(),
        marker = rememberDefaultCartesianMarker(label),
        legend =
          rememberHorizontalLegend(
            listOf(LegendItem(ShapeComponent(Fill(Color.Red)), TextComponent(), "a"))
          ),
        decorations = listOf(HorizontalLine(1.0, LineComponent(Fill(Color.Red)))),
        persistentMarkers = mapOf(1 to rememberDefaultCartesianMarker(label)),
      )
    val pieChart = rememberPieChart(pieModel)
    SideEffect { record(listOf(trigger, cartesianChart, pieChart).drop(1)) }
  }

  @Test
  fun `Value-like classes compare structurally`() {
    fun line() =
      LineCartesianLayer.Line(
        LineCartesianLayer.LineFill.single(Fill(Color.Red)),
        pointProvider =
          LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(ShapeComponent(Fill(Color.Blue)))
          ),
      )
    assertEquals(line(), line())
    assertEquals(line().hashCode(), line().hashCode())
    assertEquals(
      PieChart.Slice(Fill(Color.Red), label = PieChart.SliceLabel.Inside()),
      PieChart.Slice(Fill(Color.Red), label = PieChart.SliceLabel.Inside()),
    )
    assertEquals(PieChart.SliceLabel.Outside(), PieChart.SliceLabel.Outside())
    assertNotEquals(PieChart.Slice(Fill(Color.Red)), PieChart.Slice(Fill(Color.Blue)))
    assertEquals(
      LegendItem(ShapeComponent(Fill(Color.Red)), TextComponent(), "a"),
      LegendItem(ShapeComponent(Fill(Color.Red)), TextComponent(), "a"),
    )
    assertEquals(Zoom.max(Zoom.fixed(2f), Zoom.Content), Zoom.max(Zoom.fixed(2f), Zoom.Content))
    assertEquals(Zoom.x(3.0), Zoom.x(3.0))
    assertEquals(Scroll.Absolute.x(1.0, 0.5f), Scroll.Absolute.x(1.0, 0.5f))
    assertEquals(Scroll.Relative.pixels(4f), Scroll.Relative.pixels(4f))
    assertEquals(HorizontalAxis.ItemPlacer.segmented(), HorizontalAxis.ItemPlacer.segmented())
    assertEquals(VerticalAxis.ItemPlacer.count(4), VerticalAxis.ItemPlacer.count(4))
    val model = ColumnCartesianLayerModel.build { series(1, 2) }
    fun layer() =
      ColumnCartesianLayer(
        model,
        ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill(Color.Red), 4.dp)),
        key = "k",
      )
    assertEquals(layer(), layer())
  }
}
