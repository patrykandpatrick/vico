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

package com.patrykandpatrick.vico.views.cartesian.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class CartesianLayerDrawingModelInterpolatorTest {
  @Test
  fun `Reveal - first appearance interpolates revealFraction from zero`() {
    val interpolator = CartesianLayerDrawingModelInterpolator.line(reveal = true)
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.8f))),
        opacity = 1f,
        revealFraction = 1f,
      )

    interpolator.setModels(null, newModel)
    val transformedModel = runBlocking { interpolator.transform(0.5f) }

    assertFloatEquals(0.8f, transformedModel?.single()?.get(0.0)?.y)
    assertFloatEquals(1f, transformedModel?.opacity)
    assertFloatEquals(0.5f, transformedModel?.revealFraction)
  }

  @Test
  fun `Reveal - later update interpolates normally`() {
    val interpolator = CartesianLayerDrawingModelInterpolator.line(reveal = true)
    val oldModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.4f))),
        opacity = 1f,
        revealFraction = 1f,
      )
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.8f))),
        opacity = 1f,
        revealFraction = 1f,
      )

    interpolator.setModels(oldModel, newModel)
    val transformedModel = runBlocking { interpolator.transform(0.5f) }

    assertFloatEquals(0.6f, transformedModel?.single()?.get(0.0)?.y)
    assertFloatEquals(1f, transformedModel?.opacity)
    assertFloatEquals(1f, transformedModel?.revealFraction)
  }

  @Test
  fun `Transform matches series by key`() {
    val interpolator = CartesianLayerDrawingModelInterpolator.line()
    val oldModel =
      LineCartesianLayerDrawingModel(
        entries =
          listOf(
            mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.25f)),
            mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.75f)),
          ),
        seriesKeys = listOf("first", "second"),
      )
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(1f))),
        seriesKeys = listOf("second"),
      )

    interpolator.setModels(oldModel, newModel)
    val transformedModel = runBlocking { interpolator.transform(0.5f) }

    assertEquals(listOf("second"), transformedModel?.seriesKeys)
    assertFloatEquals(0.875f, transformedModel?.single()?.get(0.0)?.y)
  }

  @Test
  fun `Line - first appearance starts at baseline and transparent`() {
    val interpolator = CartesianLayerDrawingModelInterpolator.line()
    val newModel =
      LineCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to LineCartesianLayerDrawingModel.Entry(0.8f))),
        opacity = 0.6f,
      )

    interpolator.setModels(null, newModel)
    val transformedModel = runBlocking { interpolator.transform(0.5f) }

    assertFloatEquals(0.4f, transformedModel?.single()?.get(0.0)?.y)
    assertFloatEquals(0.3f, transformedModel?.opacity)
    assertFloatEquals(1f, transformedModel?.revealFraction)
  }

  @Test
  fun `Column - newly inserted entries start at baseline`() {
    val interpolator = CartesianLayerDrawingModelInterpolator.column()
    val oldModel =
      ColumnCartesianLayerDrawingModel(
        entries = listOf(mapOf(0.0 to ColumnCartesianLayerDrawingModel.Entry(0.2f)))
      )
    val newModel =
      ColumnCartesianLayerDrawingModel(
        entries =
          listOf(
            mapOf(
              0.0 to ColumnCartesianLayerDrawingModel.Entry(0.6f),
              1.0 to ColumnCartesianLayerDrawingModel.Entry(0.8f),
            )
          )
      )

    interpolator.setModels(oldModel, newModel)
    val transformedModel = runBlocking { interpolator.transform(0.5f) }

    assertFloatEquals(0.4f, transformedModel?.single()?.get(0.0)?.height)
    assertFloatEquals(0.4f, transformedModel?.single()?.get(1.0)?.height)
  }

  @Test
  fun `Candlestick - first appearance starts at baseline and transparent`() {
    val interpolator = CartesianLayerDrawingModelInterpolator.candlestick()
    val newModel =
      CandlestickCartesianLayerDrawingModel(
        entries =
          mapOf(
            0.0 to
              CandlestickCartesianLayerDrawingModel.Entry(
                bodyBottomY = 0.2f,
                bodyTopY = 0.6f,
                bottomWickY = 0.1f,
                topWickY = 0.8f,
              )
          ),
        opacity = 0.6f,
      )

    interpolator.setModels(null, newModel)
    val transformedModel = runBlocking { interpolator.transform(0.5f) }
    val entry = transformedModel?.entries?.get(0.0)

    assertFloatEquals(0.1f, entry?.bodyBottomY)
    assertFloatEquals(0.3f, entry?.bodyTopY)
    assertFloatEquals(0.05f, entry?.bottomWickY)
    assertFloatEquals(0.4f, entry?.topWickY)
    assertFloatEquals(0.3f, transformedModel?.opacity)
  }

  private fun assertFloatEquals(expected: Float, actual: Float?) {
    assertEquals(expected, checkNotNull(actual), absoluteTolerance = 1e-6f)
  }
}
