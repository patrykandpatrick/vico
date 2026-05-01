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

import android.graphics.BlendMode
import android.graphics.Paint
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockkConstructor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class CartesianChartTest {

  @BeforeTest
  fun setUp() {
    MockKAnnotations.init(this, relaxUnitFun = true)
    mockkConstructor(Paint::class)
    justRun { anyConstructed<Paint>().setColor(any()) }
    justRun { anyConstructed<Paint>().setAlpha(any()) }
    justRun { anyConstructed<Paint>().setStrokeWidth(any()) }
    justRun { anyConstructed<Paint>().setStyle(any()) }
    justRun { anyConstructed<Paint>().setStrokeCap(any()) }
    justRun { anyConstructed<Paint>().setStrokeJoin(any()) }
    justRun { anyConstructed<Paint>().setAntiAlias(any()) }
    every { anyConstructed<Paint>().setXfermode(any()) } returns null
    justRun { anyConstructed<Paint>().setBlendMode(any<BlendMode>()) }
  }

  @Test
  fun `Given two the same CartesianChart instances are created, when they are compared, then they are NOT equal`() {
    val chart1 = getCartesianChart()
    val chart2 = getCartesianChart()

    assertNotSame(chart1, chart2)
    assertNotSame(chart1.hashCode(), chart2.hashCode())
  }

  @Test
  fun `Given CartesianChart is copied without any changes, when it is compared to the original, then they are equal`() {
    val chart = getCartesianChart()

    val copiedChart = chart.copy()

    assertEquals(chart, copiedChart)
    assertEquals(chart.hashCode(), copiedChart.hashCode())
  }

  @Test
  fun `Given CartesianChart is copied with changes, when it is compared to the original, then they are NOT equal`() {
    val chart = getCartesianChart()

    val copiedChart =
      chart.copy(layerPadding = { CartesianLayerPadding(10f.dp, 10f.dp, 10f.dp, 10f.dp) })

    assertNotEquals(chart, copiedChart)
    assertNotEquals(chart.hashCode(), copiedChart.hashCode())
  }

  @Suppress("DEPRECATION")
  @Test
  fun `Given x step lambda is provided, when chart is copied, then lambda arity resolves`() {
    val chart = getCartesianChart()

    chart.copy(getXStep = { model -> model.getXDeltaGcd() })
    chart.copy(getXStep = { model, _, _ -> model.getXDeltaGcd() })
  }

  @Test
  fun `Given x range is extended, when ranges are updated, then default x step accounts for range`() {
    val chart =
      CartesianChart(
        LineCartesianLayer(
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
            ),
          rangeProvider = CartesianLayerRangeProvider.fixed(minX = -0.5, maxX = 2.0),
        )
      )
    val ranges = MutableCartesianChartRanges()
    val model =
      CartesianChartModel(
        LineCartesianLayerModel(
          listOf(
            listOf(
              LineCartesianLayerModel.Entry(0, 0),
              LineCartesianLayerModel.Entry(1, 1),
              LineCartesianLayerModel.Entry(2, 2),
            )
          )
        )
      )

    chart.updateRanges(ranges, model)

    assertEquals(0.5, ranges.xStep)
  }

  @Test
  fun `Given only max x is extended, when ranges are updated, then default x step is unchanged`() {
    val chart =
      CartesianChart(
        LineCartesianLayer(
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
            ),
          rangeProvider = CartesianLayerRangeProvider.fixed(maxX = 2.5),
        )
      )
    val ranges = MutableCartesianChartRanges()
    val model =
      CartesianChartModel(
        LineCartesianLayerModel(
          listOf(
            listOf(
              LineCartesianLayerModel.Entry(0, 0),
              LineCartesianLayerModel.Entry(1, 1),
              LineCartesianLayerModel.Entry(2, 2),
            )
          )
        )
      )

    chart.updateRanges(ranges, model)

    assertEquals(1.0, ranges.xStep)
  }

  private companion object {
    fun getCartesianChart(): CartesianChart =
      CartesianChart(
        LineCartesianLayer(
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
            )
        ),
        ColumnCartesianLayer(
          columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill.Black))
        ),
        markerVisibilityListener = object : CartesianMarkerVisibilityListener {},
        fadingEdges = FadingEdges(),
      )
  }
}
