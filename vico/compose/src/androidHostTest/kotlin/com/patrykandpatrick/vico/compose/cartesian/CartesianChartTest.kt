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
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.MutableCartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockkConstructor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
  fun `Given x range is extended, when ranges are updated, then default x step accounts for range`() {
    val chart =
      testCartesianChart(
        LineCartesianLayer(
          model = lineModel,
          key = 0,
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
            ),
          rangeProvider = CartesianLayerRangeProvider.fixed(minX = -0.5, maxX = 2.0),
        )
      )
    val ranges = MutableCartesianChartRanges()
    chart.updateRanges(ranges)

    assertEquals(0.5, ranges.xStep)
  }

  @Test
  fun `Given only max x is extended, when ranges are updated, then default x step is unchanged`() {
    val chart =
      testCartesianChart(
        LineCartesianLayer(
          model = lineModel,
          key = 0,
          lineProvider =
            LineCartesianLayer.LineProvider.series(
              LineCartesianLayer.Line(LineCartesianLayer.LineFill.single(Fill.Black))
            ),
          rangeProvider = CartesianLayerRangeProvider.fixed(maxX = 2.5),
        )
      )
    val ranges = MutableCartesianChartRanges()
    chart.updateRanges(ranges)

    assertEquals(1.0, ranges.xStep)
  }

  @Test
  fun `Given several column stacks, when ranges are updated, then y range aggregates each stack independently`() {
    val chart =
      testCartesianChart(
        ColumnCartesianLayer(
          model = columnModel,
          key = 0,
          columnProvider = ColumnCartesianLayer.ColumnProvider.series(LineComponent(Fill.Black)),
          stackKeyProvider = { seriesKey, _ ->
            if (seriesKey == "incomeActual" || seriesKey == "incomeForecast") "income"
            else "expenses"
          },
        )
      )
    val ranges = MutableCartesianChartRanges()
    chart.updateRanges(ranges)

    assertEquals(-11.0, ranges.getYRange(null).minY)
    assertEquals(7.0, ranges.getYRange(null).maxY)
  }

  private companion object {
    val lineModel = LineCartesianLayerModel.build { series(0, 1, 2) }

    val columnModel = ColumnCartesianLayerModel.build {
      series(4, key = "incomeActual")
      series(3, key = "incomeForecast")
      series(-6, key = "rent")
      series(-5, key = "food")
    }
  }
}
