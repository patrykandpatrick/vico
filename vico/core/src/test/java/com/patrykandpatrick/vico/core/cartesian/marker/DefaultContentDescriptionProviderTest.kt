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

package com.patrykandpatrick.vico.core.cartesian.marker

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.data.CandlestickCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DefaultContentDescriptionProviderTest {

  private val context = mockk<CartesianDrawingContext>()

  private val provider = ContentDescriptionProvider.default

  @Test
  fun `should provide correct content description for single line target with single point`() {
    val target =
      createLineTarget(
        x = 1.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(1.0, 100.0, 0),
              canvasY = 50f,
              color = 0xFF0000,
            )
          ),
      )

    val result = provider.getContentDescription(context, listOf(target))

    assertEquals("x: 1.0. y: 100.0.", result)
  }

  @Test
  fun `should provide correct content description for single line target with multiple points`() {
    val target =
      createLineTarget(
        x = 2.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 100.0, 0),
              canvasY = 50f,
              color = 0xFF0000,
            ),
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 200.0, 1),
              canvasY = 75f,
              color = 0x00FF00,
            ),
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 300.0, 2),
              canvasY = 100f,
              color = 0x0000FF,
            ),
          ),
      )

    val result = provider.getContentDescription(context, listOf(target))

    assertEquals("x: 2.0. y: 100.0. y: 200.0. y: 300.0.", result)
  }

  @Test
  fun `should provide correct content description for single column target with single column`() {
    val target =
      createColumnTarget(
        x = 3.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(3.0, 150.0, 0),
              canvasY = 60f,
              color = 0xFF00FF,
            )
          ),
      )

    val result = provider.getContentDescription(context, listOf(target))

    assertEquals("x: 3.0. y: 150.0.", result)
  }

  @Test
  fun `should provide correct content description for single column target with multiple columns`() {
    val target =
      createColumnTarget(
        x = 4.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(4.0, 50.0, 0),
              canvasY = 25f,
              color = 0xFF0000,
            ),
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(4.0, 75.0, 1),
              canvasY = 40f,
              color = 0x00FF00,
            ),
          ),
      )

    val result = provider.getContentDescription(context, listOf(target))

    assertEquals("x: 4.0. y: 50.0. y: 75.0.", result)
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
        openingCanvasY = 50f,
        closingCanvasY = 55f,
        lowCanvasY = 48f,
        highCanvasY = 57f,
        openingColor = 0xFF0000,
        closingColor = 0x00FF00,
        lowColor = 0x0000FF,
        highColor = 0xFFFF00,
      )

    val result = provider.getContentDescription(context, listOf(target))

    assertEquals("x: 5.0. Opening: 100.0. Closing: 110.0. Low: 95.0. High: 115.0.", result)
  }

  @Test
  fun `should provide correct content description for multiple targets of different types`() {
    val lineTarget =
      createLineTarget(
        x = 1.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(1.0, 100.0, 0),
              canvasY = 50f,
              color = 0xFF0000,
            )
          ),
      )

    val columnTarget =
      createColumnTarget(
        x = 2.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(2.0, 150.0, 0),
              canvasY = 60f,
              color = 0xFF00FF,
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
        openingCanvasY = 50f,
        closingCanvasY = 55f,
        lowCanvasY = 48f,
        highCanvasY = 57f,
        openingColor = 0xFF0000,
        closingColor = 0x00FF00,
        lowColor = 0x0000FF,
        highColor = 0xFFFF00,
      )

    val result =
      provider.getContentDescription(context, listOf(lineTarget, columnTarget, candlestickTarget))

    assertEquals(
      "x: 1.0. y: 100.0. x: 2.0. y: 150.0. x: 3.0. Opening: 200.0. Closing: 210.0. Low: 195.0. High: 215.0.",
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
              entry = LineCartesianLayerModel.Entry(1.0, 100.0, 0),
              canvasY = 50f,
              color = 0xFF0000,
            ),
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(1.0, 200.0, 1),
              canvasY = 75f,
              color = 0x00FF00,
            ),
          ),
      )

    val target2 =
      createLineTarget(
        x = 2.0,
        points =
          listOf(
            LineCartesianLayerMarkerTarget.Point(
              entry = LineCartesianLayerModel.Entry(2.0, 300.0, 0),
              canvasY = 100f,
              color = 0x0000FF,
            )
          ),
      )

    val result = provider.getContentDescription(context, listOf(target1, target2))

    assertEquals("x: 1.0. y: 100.0. y: 200.0. x: 2.0. y: 300.0.", result)
  }

  @Test
  fun `should provide correct content description for multiple column targets`() {
    val target1 =
      createColumnTarget(
        x = 1.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(1.0, 50.0, 0),
              canvasY = 25f,
              color = 0xFF0000,
            )
          ),
      )

    val target2 =
      createColumnTarget(
        x = 2.0,
        columns =
          listOf(
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(2.0, 75.0, 0),
              canvasY = 40f,
              color = 0x00FF00,
            ),
            ColumnCartesianLayerMarkerTarget.Column(
              entry = ColumnCartesianLayerModel.Entry(2.0, 100.0, 1),
              canvasY = 50f,
              color = 0x0000FF,
            ),
          ),
      )

    val result = provider.getContentDescription(context, listOf(target1, target2))

    assertEquals("x: 1.0. y: 50.0. x: 2.0. y: 75.0. y: 100.0.", result)
  }

  @Test
  fun `should return empty string for empty targets list`() {
    val result = provider.getContentDescription(context, emptyList())

    assertEquals("", result)
  }

  @Test
  fun `should throw IllegalArgumentException for unexpected target implementation`() {
    val unexpectedTarget =
      object : CartesianMarker.Target {
        override val x: Double = 1.0
        override val canvasX: Float = 50f
      }

    assertThrows<IllegalArgumentException> {
      provider.getContentDescription(context, listOf(unexpectedTarget))
    }
  }

  private fun createLineTarget(
    x: Double,
    canvasX: Float = 50f,
    points: List<LineCartesianLayerMarkerTarget.Point>,
  ): LineCartesianLayerMarkerTarget {
    return MutableLineCartesianLayerMarkerTarget(
      x = x,
      canvasX = canvasX,
      points = points.toMutableList(),
    )
  }

  private fun createColumnTarget(
    x: Double,
    canvasX: Float = 50f,
    columns: List<ColumnCartesianLayerMarkerTarget.Column>,
  ): ColumnCartesianLayerMarkerTarget {
    return MutableColumnCartesianLayerMarkerTarget(
      x = x,
      canvasX = canvasX,
      columns = columns.toMutableList(),
    )
  }
}
