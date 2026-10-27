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

package com.patrykandpatrick.vico.compose.cartesian.data

import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class YPercentValueFormatterTest {
  private val context =
    mockk<CartesianMeasuringContext> {
      every { ranges } returns
        MutableCartesianChartRanges().apply {
          // The `null` range also absorbs the `End` range, so it gets the greater `maxY`.
          tryUpdate(minX = 0.0, maxX = 1.0, minY = 0.0, maxY = 400.0, axisPosition = null)
          tryUpdate(
            minX = 0.0,
            maxX = 1.0,
            minY = 0.0,
            maxY = 300.0,
            axisPosition = Axis.Position.Vertical.End,
          )
        }
    }

  @Test
  fun `Values are formatted as percentages of maxY`() {
    val formatter = CartesianValueFormatter.yPercent()
    assertEquals("25%", formatter.format(context, 100.0, null).toString())
    assertEquals("100%", formatter.format(context, 300.0, Axis.Position.Vertical.End).toString())
    assertEquals("−12.5%", formatter.format(context, -50.0, null).toString())
  }

  @Test
  fun `Percentages are rounded to decimalCount decimal digits`() {
    assertEquals(
      "33.33%",
      CartesianValueFormatter.yPercent()
        .format(context, 100.0, Axis.Position.Vertical.End)
        .toString(),
    )
    assertEquals(
      "33.3%",
      CartesianValueFormatter.yPercent(decimalCount = 1)
        .format(context, 100.0, Axis.Position.Vertical.End)
        .toString(),
    )
  }
}
