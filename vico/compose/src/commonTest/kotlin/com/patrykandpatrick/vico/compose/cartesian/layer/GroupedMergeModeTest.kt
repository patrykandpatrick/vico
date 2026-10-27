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

import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer.MergeMode
import com.patrykandpatrick.vico.compose.common.Defaults
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GroupedMergeModeTest {
  private fun describe(mode: MergeMode): String =
    when (mode) {
      is MergeMode.Grouped -> "grouped"
      MergeMode.Stacked -> "stacked"
    }

  @Test
  fun `existing and new call forms resolve as intended`() {
    val plain = MergeMode.Grouped()
    val spaced = MergeMode.Grouped(10.dp)
    val named = MergeMode.Grouped(stackKey = { it })
    val trailing = MergeMode.Grouped(10.dp) { it }
    val explicitNull = MergeMode.Grouped(stackKey = null)
    val untypedReference = MergeMode::Grouped
    val referenced = untypedReference(4.dp)

    assertNull(plain.stackKey)
    assertNull(spaced.stackKey)
    assertNotNull(named.stackKey)
    assertNotNull(trailing.stackKey)
    assertNull(explicitNull.stackKey)
    assertNull(referenced.stackKey)
    assertEquals(10.dp, trailing.columnSpacing)
    assertEquals(
      listOf("grouped", "grouped", "grouped", "grouped", "grouped", "grouped", "stacked"),
      listOf(plain, spaced, named, trailing, explicitNull, referenced, MergeMode.Stacked)
        .map(::describe),
    )
  }

  @Test
  fun `Grouped without a selector keeps its equality and hash code`() {
    assertEquals(MergeMode.Grouped(), MergeMode.Grouped())
    assertEquals(MergeMode.Grouped(10.dp), MergeMode.Grouped(10.dp, null))
    assertEquals(Defaults.GROUPED_COLUMN_SPACING.dp.hashCode(), MergeMode.Grouped().hashCode())
    assertEquals(10.dp.hashCode(), MergeMode.Grouped(10.dp).hashCode())
  }

  @Test
  fun `the selector takes part in equality`() {
    val selector: (Any) -> Any? = { it }
    val other: (Any) -> Any? = { it }

    assertEquals(MergeMode.Grouped(stackKey = selector), MergeMode.Grouped(stackKey = selector))
    assertEquals(
      MergeMode.Grouped(stackKey = selector).hashCode(),
      MergeMode.Grouped(stackKey = selector).hashCode(),
    )
    assertNotEquals(MergeMode.Grouped(stackKey = selector), MergeMode.Grouped(stackKey = other))
    assertNotEquals(MergeMode.Grouped(stackKey = selector), MergeMode.Grouped())
  }

  @Test
  fun `getMinY and getMaxY without a selector use the model range`() {
    val model = model()

    assertEquals(model.minY, MergeMode.Grouped().getMinY(model))
    assertEquals(model.maxY, MergeMode.Grouped().getMaxY(model))
  }

  @Test
  fun `getMinY and getMaxY with a selector use stack totals`() {
    val mergeMode = MergeMode.Grouped { key -> if (key == "a" || key == "b") "ab" else "c" }
    val model = model()

    assertEquals(-5.0, mergeMode.getMinY(model))
    assertEquals(10.0, mergeMode.getMaxY(model))
  }

  private fun model() =
    ColumnCartesianLayerModel(
      listOf(
        listOf(ColumnCartesianLayerModel.Entry(0, 7), ColumnCartesianLayerModel.Entry(1, -2)),
        listOf(ColumnCartesianLayerModel.Entry(0, 3), ColumnCartesianLayerModel.Entry(1, -1)),
        listOf(ColumnCartesianLayerModel.Entry(0, -5), ColumnCartesianLayerModel.Entry(1, 2)),
      ),
      listOf("a", "b", "c"),
    )
}
