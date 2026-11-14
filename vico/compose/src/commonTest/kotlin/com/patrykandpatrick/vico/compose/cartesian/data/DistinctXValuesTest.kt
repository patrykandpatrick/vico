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

class DistinctXValuesTest {
  private data class Entry(override val x: Double) : CartesianLayerModel.Entry

  private fun getSeries(vararg x: Number): List<CartesianLayerModel.Entry> = x.map { value ->
    Entry(value.toDouble())
  }

  @Test
  fun `Ensure duplicates are removed from a single series`() {
    assertEquals(listOf(0.0, 1.0, 2.0), listOf(getSeries(0, 1, 1, 2)).getDistinctXValues())
  }

  @Test
  fun `Ensure multiple series are merged in ascending order`() {
    assertEquals(
      listOf(0.0, 0.5, 1.0, 2.0, 3.0),
      listOf(getSeries(1, 2, 3), getSeries(0, 0.5, 1, 2)).getDistinctXValues(),
    )
  }

  @Test
  fun `Ensure empty array yields empty list`() {
    assertEquals(emptyList(), DoubleArray(0).toDistinctList())
  }

  @Test
  fun `Ensure distinct array is returned unchanged`() {
    assertEquals(listOf(-1.0, 0.0, 4.0), doubleArrayOf(-1.0, 0.0, 4.0).toDistinctList())
  }
}
