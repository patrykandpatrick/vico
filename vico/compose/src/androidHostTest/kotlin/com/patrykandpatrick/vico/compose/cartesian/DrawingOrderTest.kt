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

package com.patrykandpatrick.vico.compose.cartesian

import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.data.MutableExtraStore
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class DrawingOrderTest {
  private fun bottomAxis(
    guideline: LineComponent? = LineComponent(Fill.Black),
    line: LineComponent? = LineComponent(Fill.Black),
    tick: LineComponent? = LineComponent(Fill.Black),
    lineDrawingOrder: CartesianChart.DrawingOrder = CartesianChart.DrawingOrder.UnderLayers,
    guidelineDrawingOrder: CartesianChart.DrawingOrder = CartesianChart.DrawingOrder.UnderLayers,
  ) =
    HorizontalAxis(
      Axis.Position.Horizontal.Bottom,
      line,
      null,
      0f,
      tick,
      4.dp,
      guideline,
      HorizontalAxis.ItemPlacer.aligned(),
      null,
      { null },
      BaseAxis.TickPosition.Outside,
      lineDrawingOrder,
      BaseAxis.TitlePosition.Side,
      guidelineDrawingOrder,
    )

  private fun lineLayer(
    seriesDrawingOrder: LineCartesianLayer.SeriesDrawingOrder,
    areaFill: LineCartesianLayer.AreaFill? = LineCartesianLayer.AreaFill.single(Fill.Black),
  ) =
    LineCartesianLayer(
      lineProvider =
        LineCartesianLayer.LineProvider.series(
          LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(Fill.Black),
            areaFill = areaFill,
          )
        ),
      seriesDrawingOrder = seriesDrawingOrder,
    )

  private fun lineModel() =
    LineCartesianLayerModel(
      listOf(listOf(LineCartesianLayerModel.Entry(0, 0), LineCartesianLayerModel.Entry(1, 1)))
    )

  private fun drawingContext(extraStore: ExtraStore = MutableExtraStore()) =
    mockk<CartesianDrawingContext> { every { this@mockk.extraStore } returns extraStore }

  @Test
  fun `Given no axis part is set to OverAreaFills, when the axis is asked, then it reports no such content`() {
    assertFalse(bottomAxis().hasContentOverAreaFills)
  }

  @Test
  fun `Given the guidelines are set to OverAreaFills, when the axis is asked, then it reports such content`() {
    assertTrue(
      bottomAxis(guidelineDrawingOrder = CartesianChart.DrawingOrder.OverAreaFills)
        .hasContentOverAreaFills
    )
  }

  @Test
  fun `Given the guidelines are set to OverAreaFills but absent, when the axis is asked, then it reports no such content`() {
    assertFalse(
      bottomAxis(
          guideline = null,
          guidelineDrawingOrder = CartesianChart.DrawingOrder.OverAreaFills,
        )
        .hasContentOverAreaFills
    )
  }

  @Test
  fun `Given the axis line is set to OverAreaFills, when the axis is asked, then it reports such content`() {
    assertTrue(
      bottomAxis(lineDrawingOrder = CartesianChart.DrawingOrder.OverAreaFills)
        .hasContentOverAreaFills
    )
  }

  @Test
  fun `Given the axis line is set to OverAreaFills but there is no line or tick, when the axis is asked, then it reports no such content`() {
    assertFalse(
      bottomAxis(
          line = null,
          tick = null,
          lineDrawingOrder = CartesianChart.DrawingOrder.OverAreaFills,
        )
        .hasContentOverAreaFills
    )
  }

  @Test
  fun `Given a Sequential LineCartesianLayer, when it is asked, then it declines to separate its area fills`() {
    val layer = lineLayer(LineCartesianLayer.SeriesDrawingOrder.Sequential)

    assertFalse(layer.canSeparateAreaFills(drawingContext(), lineModel()))
  }

  @Test
  fun `Given an AreaFillsFirst LineCartesianLayer, when it is asked, then it separates its area fills`() {
    val layer = lineLayer(LineCartesianLayer.SeriesDrawingOrder.AreaFillsFirst)

    assertTrue(layer.canSeparateAreaFills(drawingContext(), lineModel()))
  }

  @Test
  fun `Given an AreaFillsFirst LineCartesianLayer with no area fill, when it is asked, then it declines to separate its area fills`() {
    val layer = lineLayer(LineCartesianLayer.SeriesDrawingOrder.AreaFillsFirst, areaFill = null)

    assertFalse(layer.canSeparateAreaFills(drawingContext(), lineModel()))
  }

  @Test
  fun `Given a difference animation is in progress, when an AreaFillsFirst LineCartesianLayer is asked, then it declines to separate its area fills`() =
    runBlocking {
      val layer = lineLayer(LineCartesianLayer.SeriesDrawingOrder.AreaFillsFirst)
      val model = lineModel()
      val ranges = MutableCartesianChartRanges()
      CartesianChart(layer).updateRanges(ranges, CartesianChartModel(model))
      val extraStore = MutableExtraStore()
      layer.prepareForTransformation(model, ranges, extraStore)

      // A model appearing interpolates `opacity` from 0, so splitting the phases would put the area
      // fills and the strokes in separate opacity groups.
      layer.transform(extraStore, fraction = 0.5f)
      assertFalse(layer.canSeparateAreaFills(drawingContext(extraStore), model))

      layer.transform(extraStore, fraction = 1f)
      assertTrue(layer.canSeparateAreaFills(drawingContext(extraStore), model))
    }

  @Suppress("DEPRECATION")
  @Test
  fun `Given the deprecated LineDrawingOrder is referenced, when its members are read, then they are the shared values`() {
    assertEquals(CartesianChart.DrawingOrder.UnderLayers, BaseAxis.LineDrawingOrder.UnderLayers)
    assertEquals(CartesianChart.DrawingOrder.OverLayers, BaseAxis.LineDrawingOrder.OverLayers)
  }

  @Test
  fun `Given a HorizontalLine differing only in drawing order, when compared, then they are NOT equal`() {
    val line = LineComponent(Fill.Black)
    val underLayers =
      HorizontalLine({ 0.0 }, line, drawingOrder = CartesianChart.DrawingOrder.UnderLayers)
    val overAreaFills =
      HorizontalLine({ 0.0 }, line, drawingOrder = CartesianChart.DrawingOrder.OverAreaFills)

    assertNotEquals(underLayers, overAreaFills)
    assertNotEquals(underLayers.hashCode(), overAreaFills.hashCode())
    assertFalse(underLayers.hasContentOverAreaFills)
    assertTrue(overAreaFills.hasContentOverAreaFills)
  }

  @Test
  fun `Given a HorizontalBox differing only in drawing order, when compared, then they are NOT equal`() {
    val box = ShapeComponent(Fill.Black)
    val overLayers = HorizontalBox({ 0.0..1.0 }, box)
    val overAreaFills =
      HorizontalBox({ 0.0..1.0 }, box, drawingOrder = CartesianChart.DrawingOrder.OverAreaFills)

    assertNotEquals(overLayers, overAreaFills)
    assertEquals(CartesianChart.DrawingOrder.OverLayers, overLayers.drawingOrder)
    assertTrue(overAreaFills.hasContentOverAreaFills)
  }
}
