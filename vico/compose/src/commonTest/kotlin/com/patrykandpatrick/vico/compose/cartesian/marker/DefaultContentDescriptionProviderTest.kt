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

package com.patrykandpatrick.vico.compose.cartesian.marker

import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DefaultContentDescriptionProviderTest {
  private val provider = ContentDescriptionProvider.default

  @Test
  fun `should provide correct content description for single line target with single point`() {
    val target =
      createLineTarget(
        x = 1.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(1.0, 100.0),
              canvasY = 50f,
              color = Color.Red,
            )
          ),
      )

    val result = provider.getContentDescription(null, listOf(target))

    assertEquals("x: 1. y: 100.", result)
  }

  @Test
  fun `should provide correct content description for single line target with multiple points`() {
    val target =
      createLineTarget(
        x = 2.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 100.0),
              canvasY = 50f,
              color = Color.Red,
            ),
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 200.0),
              canvasY = 75f,
              color = Color.Green,
            ),
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 300.0),
              canvasY = 100f,
              color = Color.Blue,
            ),
          ),
      )

    val result = provider.getContentDescription(null, listOf(target))

    assertEquals("x: 2. y: 100. y: 200. y: 300.", result)
  }

  @Test
  fun `should provide correct content description for single column target with single column`() {
    val target =
      createColumnTarget(
        x = 3.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(3.0, 150.0),
              canvasY = 60f,
              color = Color.Magenta,
            )
          ),
      )

    val result = provider.getContentDescription(null, listOf(target))

    assertEquals("x: 3. y: 150.", result)
  }

  @Test
  fun `should provide correct content description for single column target with multiple columns`() {
    val target =
      createColumnTarget(
        x = 4.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(4.0, 50.0),
              canvasY = 25f,
              color = Color.Red,
            ),
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(4.0, 75.0),
              canvasY = 40f,
              color = Color.Green,
            ),
          ),
      )

    val result = provider.getContentDescription(null, listOf(target))

    assertEquals("x: 4. y: 50. y: 75.", result)
  }

  @Test
  fun `should provide correct content description for candlestick cartesian layer marker target`() {
    val target =
      CandlestickCartesianLayerMarkerTarget(
        x = 5.0,
        canvasX = 100f,
        entry =
          CandlestickCartesianLayerModel.Entry(
            x = 5.0,
            opening = 100.0,
            closing = 110.0,
            low = 95.0,
            high = 115.0,
            absoluteChange = CandlestickCartesianLayerModel.Change.Bullish,
            relativeChange = CandlestickCartesianLayerModel.Change.Bullish,
          ),
        modelKey = "candle",
        openingCanvasY = 50f,
        closingCanvasY = 55f,
        lowCanvasY = 48f,
        highCanvasY = 57f,
        openingColor = Color.Red,
        closingColor = Color.Green,
        lowColor = Color.Blue,
        highColor = Color.Yellow,
      )

    val result = provider.getContentDescription(null, listOf(target))

    assertEquals("x: 5. Opening: 100. Closing: 110. Low: 95. High: 115.", result)
  }

  @Test
  fun `should provide correct content description for multiple targets of different types`() {
    val lineTarget =
      createLineTarget(
        x = 1.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(1.0, 100.0),
              canvasY = 50f,
              color = Color.Red,
            )
          ),
      )

    val columnTarget =
      createColumnTarget(
        x = 2.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(2.0, 150.0),
              canvasY = 60f,
              color = Color.Magenta,
            )
          ),
      )

    val candlestickTarget =
      CandlestickCartesianLayerMarkerTarget(
        x = 3.0,
        canvasX = 100f,
        entry =
          CandlestickCartesianLayerModel.Entry(
            x = 3.0,
            opening = 200.0,
            closing = 210.0,
            low = 195.0,
            high = 215.0,
            absoluteChange = CandlestickCartesianLayerModel.Change.Bullish,
            relativeChange = CandlestickCartesianLayerModel.Change.Bullish,
          ),
        modelKey = "candle",
        openingCanvasY = 50f,
        closingCanvasY = 55f,
        lowCanvasY = 48f,
        highCanvasY = 57f,
        openingColor = Color.Red,
        closingColor = Color.Green,
        lowColor = Color.Blue,
        highColor = Color.Yellow,
      )

    val result =
      provider.getContentDescription(null, listOf(lineTarget, columnTarget, candlestickTarget))

    assertEquals(
      "x: 1. y: 100. x: 2. y: 150. x: 3. Opening: 200. Closing: 210. Low: 195. High: 215.",
      result,
    )
  }

  @Test
  fun `should provide correct content description for multiple line targets`() {
    val target1 =
      createLineTarget(
        x = 1.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(1.0, 100.0),
              canvasY = 50f,
              color = Color.Red,
            ),
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(1.0, 200.0),
              canvasY = 75f,
              color = Color.Green,
            ),
          ),
      )

    val target2 =
      createLineTarget(
        x = 2.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 300.0),
              canvasY = 100f,
              color = Color.Blue,
            )
          ),
      )

    val result = provider.getContentDescription(null, listOf(target1, target2))

    assertEquals("x: 1. y: 100. y: 200. x: 2. y: 300.", result)
  }

  @Test
  fun `should provide correct content description for multiple column targets`() {
    val target1 =
      createColumnTarget(
        x = 1.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(1.0, 50.0),
              canvasY = 25f,
              color = Color.Red,
            )
          ),
      )

    val target2 =
      createColumnTarget(
        x = 2.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(2.0, 75.0),
              canvasY = 40f,
              color = Color.Green,
            ),
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(2.0, 100.0),
              canvasY = 50f,
              color = Color.Blue,
            ),
          ),
      )

    val result = provider.getContentDescription(null, listOf(target1, target2))

    assertEquals("x: 1. y: 50. x: 2. y: 75. y: 100.", result)
  }

  @Test
  fun `should return empty string for empty targets list`() {
    val result = provider.getContentDescription(null, emptyList())

    assertEquals("", result)
  }

  @Test
  fun `should throw IllegalArgumentException for unexpected target implementation`() {
    val unexpectedTarget =
      object : CartesianMarker.Target {
        override val x: Double = 1.0
        override val canvasX: Float = 50f
      }

    assertFailsWith<IllegalArgumentException> {
      provider.getContentDescription(null, listOf(unexpectedTarget))
    }
  }

  private fun createLineTarget(
    x: Double,
    canvasX: Float = 50f,
    points: List<LineCartesianLayerMarkerTarget.Point>,
  ): LineCartesianLayerMarkerTarget =
    MutableLineCartesianLayerMarkerTarget(
      x = x,
      canvasX = canvasX,
      points = points.toMutableList(),
    )

  private fun createColumnTarget(
    x: Double,
    canvasX: Float = 50f,
    columns: List<ColumnCartesianLayerMarkerTarget.Column>,
  ): ColumnCartesianLayerMarkerTarget =
    MutableColumnCartesianLayerMarkerTarget(
      x = x,
      canvasX = canvasX,
      columns = columns.toMutableList(),
    )
}
