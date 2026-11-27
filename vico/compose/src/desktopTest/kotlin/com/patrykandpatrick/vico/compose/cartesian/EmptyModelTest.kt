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

import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EmptyModelTest {
  private val columnProvider =
    ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill(Color.Blue)))
  private val lineProvider =
    LineCartesianLayer.LineProvider.series(
      LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill(Color.Red)))
    )

  private fun emptyLine(
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
    verticalAxisPosition: Axis.Position.Vertical? = null,
  ) =
    LineCartesianLayer(
      LineCartesianLayerModel.build {},
      lineProvider,
      rangeProvider = rangeProvider,
      verticalAxisPosition = verticalAxisPosition,
      key = "empty",
    )

  private fun columns(
    vararg y: Number,
    rangeProvider: CartesianLayerRangeProvider = CartesianLayerRangeProvider.auto(),
  ) =
    ColumnCartesianLayer(
      ColumnCartesianLayerModel.build { series(*y) },
      columnProvider,
      rangeProvider = rangeProvider,
      key = "columns",
    )

  @Test
  fun `Empty models are valid`() {
    assertTrue(LineCartesianLayerModel.build {}.isEmpty)
    assertTrue(ColumnCartesianLayerModel.build {}.isEmpty)
    assertTrue(
      CandlestickCartesianLayerModel.build(emptyList(), emptyList(), emptyList(), emptyList())
        .isEmpty
    )
    val withEmptySeries = LineCartesianLayerModel.build {
      series(emptyList<Number>())
      series(1, 2)
    }
    assertEquals(0.0, withEmptySeries.minX)
    assertEquals(1.0, withEmptySeries.maxX)
    assertEquals(1.0, withEmptySeries.minY)
  }

  @Test
  fun `An empty chart uses the defaults of auto ranges`() {
    val ranges = CartesianChartPipeline.getRanges(testCartesianChart(emptyLine()))
    assertEquals(0.0, ranges.minX)
    assertEquals(0.0, ranges.maxX)
    assertEquals(1.0, ranges.xStep)
    assertEquals(0.0, ranges.getYRange(null).minY)
    assertEquals(1.0, ranges.getYRange(null).maxY)
  }

  @Test
  fun `An empty chart uses fixed ranges exactly`() {
    val ranges =
      CartesianChartPipeline.getRanges(
        testCartesianChart(
          emptyLine(
            CartesianLayerRangeProvider.fixed(minX = 0.0, maxX = 6.0, minY = 0.0, maxY = 100.0)
          )
        )
      )
    assertEquals(0.0..6.0, ranges.minX..ranges.maxX)
    assertEquals(0.0, ranges.getYRange(null).minY)
    assertEquals(100.0, ranges.getYRange(null).maxY)
  }

  @Test
  fun `An empty layer next to an auto layer with auto ranges changes nothing`() {
    for (axisPosition in listOf(null, Axis.Position.Vertical.End)) {
      val alone = CartesianChartPipeline.getRanges(testCartesianChart(columns(3, 7, 5)))
      val mixed =
        CartesianChartPipeline.getRanges(
          testCartesianChart(emptyLine(verticalAxisPosition = axisPosition), columns(3, 7, 5))
        )
      assertEquals(alone.minX..alone.maxX, mixed.minX..mixed.maxX)
      assertEquals(alone.xStep, mixed.xStep)
      assertEquals(alone.getYRange(null).minY, mixed.getYRange(null).minY)
      assertEquals(alone.getYRange(null).maxY, mixed.getYRange(null).maxY)
    }
  }

  @Test
  fun `An empty layer yields the ranges of the same layer with one point in the span`() {
    for (baseProvider in
      listOf(CartesianLayerRangeProvider.auto(), CartesianLayerRangeProvider.Intrinsic)) {
      for (provider in
        listOf(
          CartesianLayerRangeProvider.auto(),
          CartesianLayerRangeProvider.Intrinsic,
          CartesianLayerRangeProvider.fixed(maxY = 100.0),
        )) {
        fun line(model: LineCartesianLayerModel) =
          LineCartesianLayer(model, lineProvider, rangeProvider = provider, key = "line")
        val withEmpty =
          CartesianChartPipeline.getRanges(
            testCartesianChart(
              columns(3, 7, 5, rangeProvider = baseProvider),
              line(LineCartesianLayerModel.build {}),
            )
          )
        val withPoint =
          CartesianChartPipeline.getRanges(
            testCartesianChart(
              columns(3, 7, 5, rangeProvider = baseProvider),
              line(LineCartesianLayerModel.build { series(listOf(1), listOf(5)) }),
            )
          )
        val message = "$baseProvider, $provider"
        assertEquals(withPoint.minX..withPoint.maxX, withEmpty.minX..withEmpty.maxX, message)
        assertEquals(withPoint.xStep, withEmpty.xStep, message)
        assertEquals(withPoint.getYRange(null).minY, withEmpty.getYRange(null).minY, message)
        assertEquals(withPoint.getYRange(null).maxY, withEmpty.getYRange(null).maxY, message)
      }
    }
  }

  @Test
  fun `Empty layers are processed in layer order`() {
    class Recorder : CartesianLayerRangeProvider {
      var maxYInput: Double? = null

      override fun getMaxY(minY: Double, maxY: Double): Double {
        maxYInput = maxY
        return super.getMaxY(minY, maxY)
      }
    }
    fun recordedMaxYInput(recorderFirst: Boolean): Double? {
      val recorder = Recorder()
      val recording =
        LineCartesianLayer(
          LineCartesianLayerModel.build {},
          lineProvider,
          rangeProvider = recorder,
          key = "r",
        )
      val fixed = emptyLine(CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = 50.0))
      val layers = if (recorderFirst) arrayOf(recording, fixed) else arrayOf(fixed, recording)
      CartesianChartPipeline.getRanges(testCartesianChart(*layers))
      return recorder.maxYInput
    }
    assertEquals(0.0, recordedMaxYInput(recorderFirst = true))
    assertEquals(50.0, recordedMaxYInput(recorderFirst = false))
  }

  @Test
  fun `An empty layer’s explicit range is honored next to non-empty layers`() {
    val ranges =
      CartesianChartPipeline.getRanges(
        testCartesianChart(
          emptyLine(CartesianLayerRangeProvider.fixed(maxY = 100.0)),
          columns(3, 7, 5),
        )
      )
    assertEquals(100.0, ranges.getYRange(null).maxY)
    assertEquals(0.0..2.0, ranges.minX..ranges.maxX)
  }

  @Test
  fun `Empty layers have sensible dimensions and no marker targets`() {
    val chart =
      testCartesianChart(
        emptyLine(),
        ColumnCartesianLayer(ColumnCartesianLayerModel.build {}, columnProvider, key = "c"),
        CandlestickCartesianLayer(
          CandlestickCartesianLayerModel.build(emptyList(), emptyList(), emptyList(), emptyList()),
          CandlestickCartesianLayer.CandleProvider {
            CandlestickCartesianLayer.Candle(LineComponent(Fill(Color.Green)))
          },
          key = "k",
        ),
      )
    val ranges = CartesianChartPipeline.getRanges(chart)
    assertEquals(0.0, chart.model.width)
    assertEquals(1.0, chart.model.getXDeltaGcd())
    assertEquals(1.0, ranges.xStep)
    chart.layers.forEach { layer ->
      val drawingModel =
        (layer as com.patrykandpatrick.vico.compose.cartesian.layer.AnimatedCartesianLayer<*, *>)
          .createDrawingModel(ranges)
      assertTrue(drawingModel.toString().isNotEmpty())
    }
  }

  @Test
  fun `A constant series under Intrinsic yields a nonzero y-range`() {
    val layer =
      LineCartesianLayer(
        LineCartesianLayerModel.build { series(5, 5, 5) },
        lineProvider,
        rangeProvider = CartesianLayerRangeProvider.Intrinsic,
        key = "line",
      )
    val ranges = CartesianChartPipeline.getRanges(testCartesianChart(layer))
    assertEquals(5.0, ranges.getYRange(null).minY)
    assertEquals(6.0, ranges.getYRange(null).maxY)
    val drawingModel = layer.createDrawingModel(ranges)
    assertTrue(drawingModel.single().values.all { it.y.isFinite() })
  }

  @Test
  fun `An empty chart whose range provider returns equal bounds yields a nonzero y-range`() {
    val ranges =
      CartesianChartPipeline.getRanges(
        testCartesianChart(emptyLine(CartesianLayerRangeProvider.fixed(minY = 3.0, maxY = 3.0)))
      )
    assertEquals(1.0, ranges.getYRange(null).length)
  }

  @Test
  fun `Duplicate layers fail fast`() {
    assertFailsWith<IllegalArgumentException> {
      testCartesianChart(emptyLine(), emptyLine())
    }
    val layer =
      object :
        com.patrykandpatrick.vico.compose.cartesian.layer.BaseCartesianLayer<
          LineCartesianLayerModel
        >() {
        override val model = LineCartesianLayerModel.build {}
        override val markerTargets =
          emptyMap<
            Double,
            List<com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker.Target>,
          >()

        override fun drawInternal(context: CartesianDrawingContext) {}

        override fun updateDimensions(
          context: CartesianMeasuringContext,
          dimensions:
            com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions,
        ) {}

        override fun updateChartRanges(
          chartRanges: com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
        ) {}
      }
    assertFailsWith<IllegalArgumentException> { testCartesianChart(layer, layer) }
  }
}
