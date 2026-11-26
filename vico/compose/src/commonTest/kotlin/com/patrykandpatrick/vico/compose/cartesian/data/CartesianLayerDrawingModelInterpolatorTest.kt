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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CartesianLayerDrawingModelInterpolatorTest {
  private fun lineEntry(y: Float, x: Double = 0.0, seriesKey: Any = 0, seriesIndex: Int = 0) =
    LineCartesianLayerDrawingModel.Entry(
      y,
      LineCartesianLayerModel.Entry(x, y.toDouble(), seriesKey, seriesIndex),
    )

  private fun columnEntry(
    height: Float,
    x: Double = 0.0,
    seriesKey: Any = 0,
    seriesIndex: Int = 0,
  ) =
    ColumnCartesianLayerDrawingModel.Entry(
      height,
      ColumnCartesianLayerModel.Entry(x, height.toDouble(), seriesKey, seriesIndex),
    )

  @Test
  fun `Transition matches series by key`() {
    val oldModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to lineEntry(0.25f)), mapOf(0.0 to lineEntry(0.75f))),
        seriesKeys = listOf("first", "second"),
      )
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to lineEntry(1f))),
        seriesKeys = listOf("second"),
      )

    val transformedModel =
      CartesianLayerDrawingModelInterpolator.line()
        .createTransition(oldModel, newModel)
        .interpolate(0.5f)

    assertEquals(listOf("first", "second"), transformedModel.seriesKeys)
    assertFloatEquals(0.875f, transformedModel[1][0.0]?.y)
  }

  @Test
  fun `Removed series exit as the reverse of entering series, at their former positions`() {
    val oldModel =
      ColumnCartesianLayerDrawingModel(
        entries =
          listOf(
            mapOf(0.0 to columnEntry(0.2f, seriesKey = "a")),
            mapOf(0.0 to columnEntry(0.6f, seriesKey = "b", seriesIndex = 1)),
            mapOf(0.0 to columnEntry(0.4f, seriesKey = "c", seriesIndex = 2)),
          ),
        seriesKeys = listOf("a", "b", "c"),
      )
    val newModel =
      ColumnCartesianLayerDrawingModel(
        entries =
          listOf(
            mapOf(0.0 to columnEntry(0.2f, seriesKey = "a")),
            mapOf(0.0 to columnEntry(0.4f, seriesKey = "c", seriesIndex = 1)),
          ),
        seriesKeys = listOf("a", "c"),
      )
    val transition =
      CartesianLayerDrawingModelInterpolator.column().createTransition(oldModel, newModel)

    val start = transition.interpolate(0f)
    assertEquals(listOf("a", "b", "c"), start.seriesKeys)
    assertFloatEquals(0.6f, start[1][0.0]?.height)
    assertEquals("b", start[1][0.0]?.entry?.seriesKey)

    val middle = transition.interpolate(0.5f)
    assertFloatEquals(0.3f, middle[1][0.0]?.height)

    assertSame(newModel, transition.interpolate(1f))
  }

  @Test
  fun `A series that’s re-added mid-exit continues from its current state`() {
    val interpolator = CartesianLayerDrawingModelInterpolator.line()
    val full =
      LineCartesianLayerDrawingModel(
        entries =
          listOf(mapOf(0.0 to lineEntry(0.2f)), mapOf(0.0 to lineEntry(0.8f, seriesKey = 1))),
        seriesKeys = listOf(0, 1),
      )
    val reduced =
      LineCartesianLayerDrawingModel(entries = listOf(mapOf(0.0 to lineEntry(0.2f))), listOf(0))
    val midExit = interpolator.createTransition(full, reduced).interpolate(0.5f)
    assertFloatEquals(0.4f, midExit[1][0.0]?.y)

    val readded = interpolator.createTransition(midExit, full)
    assertFloatEquals(0.4f, readded.interpolate(0f)[1][0.0]?.y)
    assertFloatEquals(0.6f, readded.interpolate(0.5f)[1][0.0]?.y)
  }

  @Test
  fun `Line - first appearance starts at baseline and transparent`() {
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to lineEntry(0.8f))),
        seriesKeys = listOf(0),
        opacity = 0.6f,
      )

    val transformedModel =
      CartesianLayerDrawingModelInterpolator.line()
        .createTransition(null, newModel)
        .interpolate(0.5f)

    assertFloatEquals(0.4f, transformedModel.single()[0.0]?.y)
    assertFloatEquals(0.3f, transformedModel.opacity)
    assertFloatEquals(1f, transformedModel.sweepFraction)
  }

  @Test
  fun `Sweep - first appearance interpolates sweepFraction from zero`() {
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to lineEntry(0.8f))),
        seriesKeys = listOf(0),
        opacity = 1f,
        sweepFraction = 1f,
      )

    val transformedModel =
      CartesianLayerDrawingModelInterpolator.line(sweep = true)
        .createTransition(null, newModel)
        .interpolate(0.5f)

    assertFloatEquals(0.8f, transformedModel.single()[0.0]?.y)
    assertFloatEquals(1f, transformedModel.opacity)
    assertFloatEquals(0.5f, transformedModel.sweepFraction)
  }

  @Test
  fun `Sweep - later update interpolates normally`() {
    val oldModel =
      LineCartesianLayerDrawingModel(entries = listOf(mapOf(0.0 to lineEntry(0.4f))), listOf(0))
    val newModel =
      LineCartesianLayerDrawingModel(entries = listOf(mapOf(0.0 to lineEntry(0.8f))), listOf(0))

    val transformedModel =
      CartesianLayerDrawingModelInterpolator.line(sweep = true)
        .createTransition(oldModel, newModel)
        .interpolate(0.5f)

    assertFloatEquals(0.6f, transformedModel.single()[0.0]?.y)
    assertFloatEquals(1f, transformedModel.opacity)
    assertFloatEquals(1f, transformedModel.sweepFraction)
  }

  @Test
  fun `Column - newly inserted entries start at baseline`() {
    val oldModel =
      ColumnCartesianLayerDrawingModel(entries = listOf(mapOf(0.0 to columnEntry(0.2f))), listOf(0))
    val newModel =
      ColumnCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to columnEntry(0.6f), 1.0 to columnEntry(0.8f, x = 1.0))),
        listOf(0),
      )

    val transformedModel =
      CartesianLayerDrawingModelInterpolator.column()
        .createTransition(oldModel, newModel)
        .interpolate(0.5f)

    assertFloatEquals(0.4f, transformedModel.single()[0.0]?.height)
    assertFloatEquals(0.4f, transformedModel.single()[1.0]?.height)
  }

  @Test
  fun `Candlestick - first appearance starts at baseline and transparent`() {
    val entry = CandlestickCartesianLayerModel.build(listOf(2), listOf(6), listOf(1), listOf(8))
    val newModel =
      CandlestickCartesianLayerDrawingModel(
        entries =
          listOf(
            mapOf(
              0.0 to
                CandlestickCartesianLayerDrawingModel.Entry(
                  bodyBottomY = 0.2f,
                  bodyTopY = 0.6f,
                  bottomWickY = 0.1f,
                  topWickY = 0.8f,
                  entry = entry.series.single(),
                ),
            ),
          ),
        seriesKeys = listOf(0),
        opacity = 0.6f,
      )

    val transformedModel =
      CartesianLayerDrawingModelInterpolator.candlestick()
        .createTransition(null, newModel)
        .interpolate(0.5f)
    val transformedEntry = transformedModel.single()[0.0]

    assertFloatEquals(0.1f, transformedEntry?.bodyBottomY)
    assertFloatEquals(0.3f, transformedEntry?.bodyTopY)
    assertFloatEquals(0.05f, transformedEntry?.bottomWickY)
    assertFloatEquals(0.4f, transformedEntry?.topWickY)
    assertFloatEquals(0.3f, transformedModel.opacity)
  }

  @Test
  fun `mergeKeys keeps removed keys at their former positions`() {
    assertEquals(listOf("a", "b", "c"), mergeKeys(listOf("a", "b", "c"), listOf("a", "c")))
    assertEquals(listOf("x", "a", "c", "d"), mergeKeys(listOf("x", "a"), listOf("a", "c", "d")))
    assertEquals(listOf("b", "a"), mergeKeys(listOf("b"), listOf("a")))
  }

  private fun assertFloatEquals(expected: Float, actual: Float?) {
    assertEquals(expected, checkNotNull(actual), absoluteTolerance = 1e-6f)
  }
}
