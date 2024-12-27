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

package com.patrykandpatrick.vico.core.cartesian

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Xfermode
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.component.LineComponent
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

public class CartesianChartTest {

  @BeforeEach
  public fun setUp() {
    MockKAnnotations.init(this, relaxUnitFun = true)
    mockkConstructor(Paint::class)
    every { anyConstructed<Paint>().setColor(any()) } returns Unit
    every { anyConstructed<Paint>().setXfermode(any()) } returns Xfermode()
    every { anyConstructed<Paint>().style = any() } returns Unit
    every { anyConstructed<Paint>().strokeCap = any() } returns Unit
    mockkConstructor(RectF::class)
    every { anyConstructed<RectF>() == any() } returns true
    every { anyConstructed<RectF>().hashCode() } returns 0
  }

  @Test
  public fun `Given two the same CartesianChart instances are created, when they are compared, then they are NOT equal`() {
    val chart1 = getCartesianChart()
    val chart2 = getCartesianChart()

    Assertions.assertNotSame(chart1, chart2)
    Assertions.assertNotSame(chart1.hashCode(), chart2.hashCode())
  }

  @Test
  public fun `Given CartesianChart is copied without any changes, when it is compared to the original, then they are equal`() {
    val chart = getCartesianChart()

    val copiedChart = chart.copy()

    Assertions.assertEquals(chart, copiedChart)
    Assertions.assertEquals(chart.hashCode(), copiedChart.hashCode())
  }

  @Test
  public fun `Given CartesianChart is copied with changes, when it is compared to the original, then they are NOT equal`() {
    val chart = getCartesianChart()

    val copiedChart = chart.copy(layerPadding = { CartesianLayerPadding(10f, 10f, 10f, 10f) })

    Assertions.assertNotEquals(chart, copiedChart)
    Assertions.assertNotEquals(chart.hashCode(), copiedChart.hashCode())
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
        startAxis = VerticalAxis.start(LineComponent(Fill.Black)),
        bottomAxis = HorizontalAxis.bottom(LineComponent(Fill.Black)),
        markerVisibilityListener = object : CartesianMarkerVisibilityListener {},
        fadingEdges = FadingEdges(),
      )
  }
}
