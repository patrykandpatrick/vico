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

package com.patrykandpatrick.vico.compose.cartesian.layer

import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer.StackKeyProvider
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ColumnStackLayoutTest {
  @Test
  fun `stacks are matched by equality and ordered by first series`() {
    var calls = 0
    val stackKeyProvider = StackKeyProvider { seriesKey, _, _ ->
      calls++
      when (seriesKey) {
        "a1",
        "a2" -> StackKey("A")
        "b1" -> StackKey("B")
        else -> seriesKey
      }
    }

    val layout = stackKeyProvider.getLayout(listOf("a1", "b1", "a2", "solo1", "solo2"))

    assertContentEquals(intArrayOf(0, 1, 0, 2, 3), layout.stackIndices)
    assertEquals(4, layout.stackCount)
    assertEquals(
      listOf(listOf(0, 2), listOf(1), listOf(3), listOf(4)),
      layout.seriesIndicesByStack(),
    )
    assertEquals(5, calls)
  }

  @Test
  fun `grouped and stacked give one stack per series and one shared stack`() {
    val keys = listOf("a", "b", "c")

    assertEquals(
      listOf(listOf(0), listOf(1), listOf(2)),
      StackKeyProvider.Grouped.getLayout(keys).seriesIndicesByStack(),
    )
    assertEquals(
      listOf(listOf(0, 1, 2)),
      StackKeyProvider.Stacked.getLayout(keys).seriesIndicesByStack(),
    )
  }

  @Test
  fun `the y range sums signs separately per stack and x value`() {
    val model = model(listOf(0 to 7), listOf(0 to -2), listOf(0 to 3), listOf(0 to 2))
    val layout = StackKeyProvider { _, seriesIndex, _ ->
      if (seriesIndex < 2) "A" else "B"
    }
      .layout(model)

    assertEquals(-2.0..7.0, model.getYRange(layout))
  }

  @Test
  fun `the y range handles sparse x values`() {
    val model = model(listOf(0 to 5, 2 to 1), listOf(1 to 3, 2 to 4), listOf(0 to 8))
    val layout = StackKeyProvider { _, seriesIndex, _ ->
      if (seriesIndex < 2) "A" else seriesIndex
    }
      .layout(model)

    assertEquals(0.0..8.0, model.getYRange(layout))
  }

  @Test
  fun `single-series stacks contribute their values and multi-series stacks their totals`() {
    val model = model(listOf(0 to 2, 1 to 3), listOf(0 to 4), listOf(0 to 5))
    val singleSeries = model(listOf(0 to 2, 1 to 3), listOf(0 to 4))
    val layout = StackKeyProvider { _, seriesIndex, _ ->
      if (seriesIndex == 0) seriesIndex else "B"
    }
      .layout(model)

    assertEquals(0.0..9.0, model.getYRange(layout))
    assertEquals(2.0..4.0, singleSeries.getYRange(StackKeyProvider.Grouped.layout(singleSeries)))
    assertEquals(model.minY..model.maxY, model.getYRange(StackKeyProvider.Grouped.layout(model)))
    assertEquals(
      model.minAggregateY..model.maxAggregateY,
      model.getYRange(StackKeyProvider.Stacked.layout(model)),
    )
  }

  @Test
  fun `the y range handles all-negative stacks`() {
    val model = model(listOf(0 to -1), listOf(0 to -2), listOf(0 to -4))
    val layout = StackKeyProvider { _, seriesIndex, _ ->
      if (seriesIndex == 2) seriesIndex else "A"
    }
      .layout(model)

    assertEquals(-4.0..0.0, model.getYRange(layout))
  }

  private data class StackKey(val value: String) {
    override fun toString(): String = "same display text"
  }

  private fun StackKeyProvider.getLayout(seriesKeys: List<Any>) =
    getLayout(seriesKeys, ExtraStore.Empty)

  private fun StackKeyProvider.layout(model: ColumnCartesianLayerModel) =
    getLayout(model.seriesKeys, model.extraStore)

  private fun ColumnStackLayout.seriesIndicesByStack() =
    List(stackCount) { stackIndex ->
      buildList { forEachSeriesIndex(stackIndex) { seriesIndex -> add(seriesIndex) } }
    }

  private fun model(vararg series: List<Pair<Int, Int>>): ColumnCartesianLayerModel =
    ColumnCartesianLayerModel(
      series.map { entries -> entries.map { (x, y) -> ColumnCartesianLayerModel.Entry(x, y) } }
    )
}
