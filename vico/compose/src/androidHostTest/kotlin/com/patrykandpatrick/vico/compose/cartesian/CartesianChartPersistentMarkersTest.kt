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

class CartesianChartPersistentMarkersTest {

  private val marker =
    object : CartesianMarker {
      override fun drawOverLayers(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
      ) {}
    }

  @Suppress("UNCHECKED_CAST")
  @Test
  fun `The exact order of persistent markers is preserved when set to CartesianChart`() {
    val map = LinkedHashMap<Double, CartesianMarker>()
    repeat(100) { index -> map[index.toDouble()] = marker }

    val sut = testCartesianChart(marker = marker, persistentMarkers = map)

    val sutPersistentMarkers =
      sut::class.java.getDeclaredField("persistentMarkers").apply { isAccessible = true }.get(sut)
        as Map<Double, CartesianMarker>

    assertEquals(map.keys.toList(), sutPersistentMarkers.keys.toList())
  }

  @Test
  fun `Integer keys are converted to x-values`() {
    val sut = testCartesianChart(marker = marker, persistentMarkers = mapOf(3 to marker))

    @Suppress("UNCHECKED_CAST")
    val sutPersistentMarkers =
      sut::class.java.getDeclaredField("persistentMarkers").apply { isAccessible = true }.get(sut)
        as Map<Double, CartesianMarker>

    assertEquals(listOf(3.0), sutPersistentMarkers.keys.toList())
  }
}
