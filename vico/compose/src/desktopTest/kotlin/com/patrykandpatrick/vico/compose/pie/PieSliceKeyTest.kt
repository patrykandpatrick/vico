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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class PieSliceKeyTest {
  private val styles =
    mapOf<Any, PieChart.Slice>(
      "a" to PieChart.Slice(Fill(Color.Red)),
      "b" to PieChart.Slice(Fill(Color.Green)),
      "c" to PieChart.Slice(Fill(Color.Blue)),
    )

  private fun chart(model: PieChartModel) =
    PieChart(
      model,
      { entry, _ -> styles.getValue(entry.key) },
      0.dp,
      PieSize.Outer.Fill,
      PieSize.Inner.Zero,
      PieValueFormatter.Value,
      null,
    )

  private fun drawingModel(vararg entries: Pair<String, Number>) =
    chart(PieChartModel.build(mapOf(*entries))).createDrawingModel()

  private fun PieChartDrawingModel.degreesByKey() = slices.associate { it.key to it.degrees }

  private fun assertDegrees(expected: Map<Any, Float>, actual: PieChartDrawingModel) {
    assertEquals(expected.keys.toList(), actual.slices.map { it.key })
    expected.forEach { (key, degrees) ->
      assertEquals(degrees, actual.degreesByKey().getValue(key), absoluteTolerance = 1e-3f, "$key")
    }
    assertEquals(
      360f,
      actual.slices.sumOf { it.degrees.toDouble() }.toFloat(),
      absoluteTolerance = 1e-3f,
    )
  }

  @Test
  fun `A removed middle slice exits in place while the others grow`() {
    val old = drawingModel("a" to 1, "b" to 1, "c" to 1)
    val new = drawingModel("a" to 1, "c" to 1)
    val middle = new.transform(old, 0.5f)
    assertDegrees(mapOf("a" to 150f, "b" to 60f, "c" to 150f), middle)
    val exiting = middle.slices[1]
    assertEquals(0.5f, exiting.sliceOpacity, absoluteTolerance = 1e-3f)
    // The exiting slice keeps the style it had.
    assertSame(styles.getValue("b"), exiting.slice)
    assertSame(new, new.transform(old, 1f))
  }

  @Test
  fun `An inserted middle slice enters in place`() {
    val old = drawingModel("a" to 1, "c" to 1)
    val new = drawingModel("a" to 1, "b" to 1, "c" to 1)
    val middle = new.transform(old, 0.5f)
    assertDegrees(mapOf("a" to 150f, "b" to 60f, "c" to 150f), middle)
    assertEquals(0.5f, middle.slices[1].sliceOpacity, absoluteTolerance = 1e-3f)
  }

  @Test
  fun `Reordered slices keep their identities and take the new order`() {
    val old = drawingModel("a" to 1, "b" to 2, "c" to 3)
    val new = drawingModel("c" to 3, "a" to 1, "b" to 2)
    val middle = new.transform(old, 0.5f)
    assertDegrees(mapOf("c" to 180f, "a" to 60f, "b" to 120f), middle)
  }

  @Test
  fun `By default, slices are matched by position`() {
    val old = PieChartModel.build(60, 20, 20)
    val new = PieChartModel.build(60, 20)
    assertEquals(listOf<Any>(0, 1, 2), old.entries.map { it.key })
    val positional =
      PieChart(
        new,
        PieChart.SliceProvider.series(PieChart.Slice()),
        0.dp,
        PieSize.Outer.Fill,
        PieSize.Inner.Zero,
        PieValueFormatter.Value,
        null,
      )
    val oldChart =
      PieChart(
        old,
        PieChart.SliceProvider.series(PieChart.Slice()),
        0.dp,
        PieSize.Outer.Fill,
        PieSize.Inner.Zero,
        PieValueFormatter.Value,
        null,
      )
    val middle = positional.createDrawingModel().transform(oldChart.createDrawingModel(), 0.5f)
    // The last slice exits, and the others grow (216 to 270 and 72 to 90 degrees).
    assertEquals(listOf<Any>(0, 1, 2), middle.slices.map { it.key })
    assertEquals(243f, middle.slices[0].degrees, absoluteTolerance = 1e-3f)
    assertEquals(81f, middle.slices[1].degrees, absoluteTolerance = 1e-3f)
    assertEquals(36f, middle.slices[2].degrees, absoluteTolerance = 1e-3f)
  }

  @Test
  fun `A slice re-added mid-exit continues from its current state`() {
    val full = drawingModel("a" to 1, "b" to 1, "c" to 1)
    val midExit = drawingModel("a" to 1, "c" to 1).transform(full, 0.5f)
    fun PieChartDrawingModel.state() = slices.map {
      listOf(it.key, it.degrees, it.sliceOpacity, it.labelOpacity)
    }
    // No jump at the start of the new transition.
    assertEquals(midExit.state(), full.transform(midExit, 0f).state())
    val halfway = full.transform(midExit, 0.5f).slices[1]
    assertEquals(90f, halfway.degrees, absoluteTolerance = 1e-3f)
    assertEquals(0.75f, halfway.sliceOpacity, absoluteTolerance = 1e-3f)
  }

  @Test
  fun `Going to an empty model animates every slice out`() {
    val old = drawingModel("a" to 1, "b" to 1)
    val empty = chart(PieChartModel.build(emptyMap<Any, Number>())).createDrawingModel()
    val middle = empty.transform(old, 0.5f)
    assertEquals(listOf<Any>("a", "b"), middle.slices.map { it.key })
    assertEquals(90f, middle.slices[0].degrees, absoluteTolerance = 1e-3f)
    assertEquals(emptyList(), empty.transform(old, 1f).slices)
  }

  @Test
  fun `Keys must be unique`() {
    assertFailsWith<IllegalArgumentException> {
      PieChartModel(listOf(PieChartModel.Entry(1f, "a"), PieChartModel.Entry(2f, "a")))
    }
  }
}
