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
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ColumnStackLayoutTest {
  private data class StackKey(val value: String) {
    override fun toString(): String = "same display text"
  }

  @Test
  fun `mapping uses equality and keeps null results separate`() {
    var calls = 0
    val keys = listOf("a1", "b1", "a2", "solo1", "solo2")
    val layout =
      createColumnStackLayout(keys) { key ->
        calls++
        when (key) {
          "a1",
          "a2" -> StackKey("A")
          "b1" -> StackKey("B")
          else -> null
        }
      }

    assertContentEquals(intArrayOf(0, 1, 0, 2, 3), layout.stackIndices)
    assertContentEquals(intArrayOf(0, 1, 3, 4), layout.firstSeriesIndices)
    assertEquals(4, layout.stackCount)
    assertEquals(0, layout.stackIndexBySeriesKey.getValue("a2"))
    assertEquals(5, calls)
  }

  @Test
  fun `stacked range sums signs separately per stack and x`() {
    val model = model(listOf(0 to 7), listOf(0 to -2), listOf(0 to 3), listOf(0 to 2))
    val layout =
      createColumnStackLayout(model.seriesKeys) { key -> if (key == 0 || key == 1) "A" else "B" }

    val range = model.getStackedYRange(layout)

    assertEquals(-2.0, range.start)
    assertEquals(7.0, range.endInclusive)
  }

  @Test
  fun `stacked range handles sparse x values`() {
    val model = model(listOf(0 to 5, 2 to 1), listOf(1 to 3, 2 to 4))
    val layout = createColumnStackLayout(model.seriesKeys) { "A" }

    val range = model.getStackedYRange(layout)

    assertEquals(0.0, range.start)
    assertEquals(5.0, range.endInclusive)
  }

  @Test
  fun `stacked range handles all-negative stacks`() {
    val model = model(listOf(0 to -1), listOf(0 to -2), listOf(0 to -4))
    val layout = createColumnStackLayout(model.seriesKeys) { key -> if (key == 2) null else "A" }

    val range = model.getStackedYRange(layout)

    assertEquals(-4.0, range.start)
    assertEquals(0.0, range.endInclusive)
  }

  @Test
  fun `resolver reuses the mapping for equal keys and the same selector`() {
    var calls = 0
    val selector: (Any) -> Any? = {
      calls++
      it
    }
    val resolver = ColumnStackResolver()

    val first = resolver.resolve(listOf("a", "b", "c"), selector)
    val second = resolver.resolve(listOf("a", "b", "c"), selector)

    assertSame(first, second)
    assertEquals(3, calls)
  }

  @Test
  fun `resolver rebuilds the mapping for new keys or a new selector`() {
    var calls = 0
    val selector: (Any) -> Any? = {
      calls++
      "stack"
    }
    val otherSelector: (Any) -> Any? = {
      calls++
      it
    }
    val resolver = ColumnStackResolver()

    resolver.resolve(listOf("a", "b"), selector)
    val reordered = resolver.resolve(listOf("b", "a"), selector)
    val reselected = resolver.resolve(listOf("b", "a"), otherSelector)

    assertEquals(6, calls)
    assertEquals(0, reordered.stackIndexBySeriesKey.getValue("a"))
    assertEquals(2, reselected.stackCount)
  }

  @Test
  fun `resolver does not confuse unequal keys with identical string representations`() {
    val resolver = ColumnStackResolver()
    val selector: (Any) -> Any? = { it }

    val first = resolver.resolve(listOf(StackKey("A"), StackKey("B")), selector)
    val second = resolver.resolve(listOf(StackKey("B"), StackKey("A")), selector)

    assertEquals(0, first.stackIndexBySeriesKey.getValue(StackKey("A")))
    assertEquals(1, second.stackIndexBySeriesKey.getValue(StackKey("A")))
  }

  @Test
  fun `alternating models always get their own layout`() {
    val resolver = ColumnStackResolver()
    val byKey: (Any) -> Any? = { it }
    val allTogether: (Any) -> Any? = { "all" }
    val keysA = listOf("x", "y", "z")
    val keysB = listOf("p", "q")

    repeat(3) {
      val a = resolver.resolve(keysA, byKey)
      val b = resolver.resolve(keysB, allTogether)
      assertContentEquals(intArrayOf(0, 1, 2), a.stackIndices)
      assertContentEquals(intArrayOf(0, 0), b.stackIndices)
      assertEquals(setOf<Any>("x", "y", "z"), a.stackIndexBySeriesKey.keys)
      assertEquals(setOf<Any>("p", "q"), b.stackIndexBySeriesKey.keys)
    }
  }

  private fun model(vararg series: List<Pair<Int, Int>>): ColumnCartesianLayerModel =
    ColumnCartesianLayerModel(
      series.map { entries -> entries.map { (x, y) -> ColumnCartesianLayerModel.Entry(x, y) } }
    )
}
