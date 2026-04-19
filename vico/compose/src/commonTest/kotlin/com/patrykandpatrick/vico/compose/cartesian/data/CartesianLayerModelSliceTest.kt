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

class CartesianLayerModelSliceTest {
  private data class Entry(override val x: Double) : CartesianLayerModel.Entry

  @Test
  fun `getSliceIndices keeps the visible x range and one extra point on either side`() {
    val entries = listOf(0, 10, 20, 30, 40).map { x -> Entry(x.toDouble()) }

    val visibleIndices = entries.getSliceIndices(visibleXRangeStart = 15.0, visibleXRangeEnd = 25.0)

    assertEquals(1..3, visibleIndices)
  }

  @Test
  fun `getSliceIndices keeps the surrounding points when the visible range is in a gap`() {
    val entries = listOf(0, 10, 20, 30, 40).map { x -> Entry(x.toDouble()) }

    val visibleIndices = entries.getSliceIndices(visibleXRangeStart = 11.0, visibleXRangeEnd = 19.0)

    assertEquals(1..2, visibleIndices)
  }

  @Test
  fun `getSliceIndices keeps the closest point when the visible range is before the series`() {
    val entries = listOf(10, 20, 30).map { x -> Entry(x.toDouble()) }

    val visibleIndices =
      entries.getSliceIndices(visibleXRangeStart = -20.0, visibleXRangeEnd = -10.0)

    assertEquals(0..0, visibleIndices)
  }

  @Test
  fun `getSliceIndices returns the full series when visible padding is null`() {
    val entries = listOf(0, 10, 20, 30, 40).map { x -> Entry(x.toDouble()) }

    val visibleIndices =
      entries.getSliceIndices(
        visibleXRangeStart = 15.0,
        visibleXRangeEnd = 25.0,
        visiblePadding = null,
      )

    assertEquals(entries.indices, visibleIndices)
  }
}
