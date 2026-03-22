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
}
