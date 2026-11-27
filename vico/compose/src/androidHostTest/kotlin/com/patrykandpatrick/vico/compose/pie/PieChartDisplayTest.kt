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

import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PieChartDisplayTest {
  private fun chart(model: PieChartModel, spacing: Float = 0f) =
    PieChart(
      model,
      PieChart.SliceProvider.series(PieChart.Slice()),
      androidx.compose.ui.unit.Dp(spacing),
      PieSize.Outer.Fill,
      PieSize.Inner.Zero,
      PieValueFormatter.Value,
      null,
    )

  @Test
  fun `Interpolation matches the expected intermediate angles`() {
    val oldModel =
      PieChartDrawingModel(
        listOf(
          PieChartDrawingModel.SliceInfo(90f, key = 0),
          PieChartDrawingModel.SliceInfo(270f, key = 1),
        )
      )
    val newModel =
      PieChartDrawingModel(
        listOf(
          PieChartDrawingModel.SliceInfo(180f, key = 0),
          PieChartDrawingModel.SliceInfo(180f, key = 1),
        )
      )

    assertEquals(
      PieChartDrawingModel(
        listOf(
          PieChartDrawingModel.SliceInfo(112.5f, key = 0),
          PieChartDrawingModel.SliceInfo(247.5f, key = 1),
        )
      ),
      newModel.transform(oldModel, 0.25f),
    )
  }

  @Test
  fun `Single slice animation can reach full circle`() {
    val newModel = PieChartDrawingModel(listOf(PieChartDrawingModel.SliceInfo(360f)))
    assertEquals(360f, newModel.transform(null, 1f).slices.single().degrees)
  }

  @Test
  fun `The first chart is displayed immediately`() {
    val chart = chart(PieChartModel.build(1, 3))
    val display = preparePieChartDisplay(chart, null, animationSpec = null)!!
    assertSame(chart, display.chart)
    assertEquals(listOf(90f, 270f), display.drawingModel.slices.map { it.degrees })
  }

  @Test
  fun `A settings-only update keeps the drawing model and the animation`() {
    val model = PieChartModel.build(1, 3)
    val first =
      preparePieChartDisplay(
        chart(model),
        null,
        animationSpec = androidx.compose.animation.core.tween(),
      )!!
    val restyled = chart(model, spacing = 2f)
    val display =
      preparePieChartDisplay(
        restyled,
        first,
        animationSpec = androidx.compose.animation.core.tween(),
      )!!
    assertSame(restyled, display.chart)
    assertSame(first.drawingModel, display.drawingModel)
    assertSame(first.targetDrawingModel, display.targetDrawingModel)
  }

  @Test
  fun `A data update animates from the drawing model on screen`() {
    val first =
      preparePieChartDisplay(chart(PieChartModel.build(1, 1)), null, animationSpec = null)!!
    val display =
      preparePieChartDisplay(
        chart(PieChartModel.build(1, 3)),
        first,
        animationSpec = androidx.compose.animation.core.tween(),
      )!!
    assertTrue(display.animation != null)
    assertEquals(listOf(180f, 180f), display.drawingModel.slices.map { it.degrees })
    assertEquals(listOf(90f, 270f), display.targetDrawingModel.slices.map { it.degrees })
  }

  @Test
  fun `The same chart isn’t prepared twice`() {
    val chart = chart(PieChartModel.build(1))
    val display = preparePieChartDisplay(chart, null, animationSpec = null)!!
    assertNull(preparePieChartDisplay(chart, display, animationSpec = null))
  }
}
