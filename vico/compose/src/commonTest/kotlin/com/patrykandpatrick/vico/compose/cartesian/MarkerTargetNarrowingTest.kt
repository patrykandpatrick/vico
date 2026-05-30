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

package com.patrykandpatrick.vico.compose.cartesian

import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MarkerTargetNarrowingTest {
  private data class Target(override val x: Double, override val canvasX: Float) :
    CartesianMarker.Target

  @Test
  fun `Given no targets when narrowing then none are returned and no series is selected`() {
    val (narrowed, seriesIndex) = narrowMarkerTargets(emptyList(), pointX = 0f)

    assertEquals(emptyList(), narrowed)
    assertNull(seriesIndex)
  }

  @Test
  fun `Given a single target when narrowing then it is returned and no series is selected`() {
    val targets = listOf(Target(x = 5.0, canvasX = 100f))

    val (narrowed, seriesIndex) = narrowMarkerTargets(targets, pointX = 100f)

    assertEquals(targets, narrowed)
    assertNull(seriesIndex)
  }

  @Test
  fun `Given co-located targets when narrowing then all are returned and no series is selected`() {
    val targets = listOf(Target(x = 5.0, canvasX = 100f), Target(x = 5.0, canvasX = 100f))

    val (narrowed, seriesIndex) = narrowMarkerTargets(targets, pointX = 100f)

    assertEquals(targets, narrowed)
    assertNull(seriesIndex)
  }

  /**
   * Regression test for [#1491](https://github.com/patrykandpatrick/vico/issues/1491): a column and
   * a line sharing an _x_ can have [CartesianMarker.Target.canvasX] values that differ by a
   * fraction of a pixel because of floating-point error. Both targets must still be returned.
   */
  @Test
  fun `Given targets a sub-pixel apart when narrowing then all are returned and no series is selected`() {
    val targets =
      listOf(Target(x = 15.0, canvasX = 1234.5677f), Target(x = 15.0, canvasX = 1234.5681f))

    val (narrowed, seriesIndex) = narrowMarkerTargets(targets, pointX = 1234.568f)

    assertEquals(targets, narrowed)
    assertNull(seriesIndex)
  }

  @Test
  fun `Given targets at distinct positions when narrowing then the closest one is selected`() {
    val targets = listOf(Target(x = 5.0, canvasX = 100f), Target(x = 5.0, canvasX = 130f))

    val (narrowed, seriesIndex) = narrowMarkerTargets(targets, pointX = 126f)

    assertEquals(listOf(targets[1]), narrowed)
    assertEquals(1, seriesIndex)
  }
}
