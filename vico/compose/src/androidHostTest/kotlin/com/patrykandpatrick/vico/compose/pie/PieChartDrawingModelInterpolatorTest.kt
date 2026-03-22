@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

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

package com.patrykandpatrick.vico.compose.pie

import androidx.compose.ui.unit.dp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking

class PieChartDrawingModelInterpolatorTest {

  @Test
  fun `When no new model is set, transform returns null`() {
    val interpolator = defaultPieChartDrawingModelInterpolator()

    val transformedModel = runBlocking { interpolator.transform(0.5f) }

    assertNull(transformedModel)
  }

  @Test
  fun `When models are set, transform matches the compose interpolation result`() {
    val interpolator = defaultPieChartDrawingModelInterpolator()
    val oldModel =
      PieChartDrawingModel(
        listOf(PieChartDrawingModel.SliceInfo(90f), PieChartDrawingModel.SliceInfo(270f))
      )
    val newModel =
      PieChartDrawingModel(
        listOf(PieChartDrawingModel.SliceInfo(180f), PieChartDrawingModel.SliceInfo(180f))
      )

    interpolator.setModels(oldModel, newModel)
    val transformedModel = runBlocking { interpolator.transform(0.25f) }

    assertEquals(
      PieChartDrawingModel(
        listOf(PieChartDrawingModel.SliceInfo(112.5f), PieChartDrawingModel.SliceInfo(247.5f))
      ),
      transformedModel,
    )
  }

  @Test
  fun `PieChart uses the drawingModel interpolator for transformations`() {
    val interpolator = mockk<PieChartDrawingModelInterpolator>()
    val chart =
      PieChart(
        sliceProvider = PieChart.SliceProvider.series(PieChart.Slice()),
        spacing = 0.dp,
        outerSize = PieSize.Outer.Fill,
        innerSize = PieSize.Inner.Zero,
        startAngle = -90f,
        valueFormatter = PieValueFormatter.Value,
        legend = null,
        drawingModelInterpolator = interpolator,
      )
    val oldDrawingModel = PieChartModel.build(1f, 3f).toDrawingModel()
    val newModel = PieChartModel.build(2f, 2f)
    val newDrawingModel = newModel.toDrawingModel()
    val transformedModel =
      PieChartDrawingModel(
        listOf(PieChartDrawingModel.SliceInfo(135f), PieChartDrawingModel.SliceInfo(225f))
      )

    every { interpolator.setModels(oldDrawingModel, newDrawingModel) } returns Unit
    coEvery { interpolator.transform(0.25f) } returns transformedModel

    chart.prepareForTransformation(oldDrawingModel, newModel)
    val actualTransformedModel = runBlocking { chart.transform(0.25f) }

    assertEquals(transformedModel, actualTransformedModel)
    verify(exactly = 1) { interpolator.setModels(oldDrawingModel, newDrawingModel) }
    coVerify(exactly = 1) { interpolator.transform(0.25f) }
  }
}
