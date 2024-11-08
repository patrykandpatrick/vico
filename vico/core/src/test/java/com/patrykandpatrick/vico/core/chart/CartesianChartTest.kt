/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart

import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.CartesianChart.PersistentMarkerScope
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.data.MutableExtraStore
import java.util.LinkedHashMap
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

public class CartesianChartTest {

  private val marker =
    object : CartesianMarker {
      override fun drawOverLayers(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
      ) {}
    }

  @Suppress("UNCHECKED_CAST")
  @Test
  public fun `The exact order of persistent markers is preserved when set to CartesianChart`() {
    val map = LinkedHashMap<Double, CartesianMarker>()
    repeat(100) { index -> map[index.toDouble()] = marker }

    val sut = getSut { map.forEach { x, marker -> marker at x } }
    sut::class
      .java
      .getDeclaredMethod("updatePersistentMarkers", ExtraStore::class.java)
      .apply { isAccessible = true }
      .invoke(sut, MutableExtraStore())

    val sutPersistentMarkers =
      sut::class.java.getDeclaredField("persistentMarkerMap").apply { isAccessible = true }.get(sut)
        as Map<Double, CartesianMarker>

    map.keys.forEachIndexed { index, key ->
      assertEquals(key, sutPersistentMarkers.keys.elementAt(index))
    }
  }

  private fun getSut(
    persistentMarkers: (PersistentMarkerScope.(ExtraStore) -> Unit)?
  ): CartesianChart = CartesianChart(marker = marker, persistentMarkers = persistentMarkers)
}
