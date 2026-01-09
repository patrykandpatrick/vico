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

import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class CartesianChartTest {

  @Test
  fun `Given two the same CartesianChart instances are created, when they are compared, then they are NOT equal`() {
    val chart1 = getCartesianChart()
    val chart2 = getCartesianChart()

    assertNotSame(chart1, chart2)
    assertNotSame(chart1.hashCode(), chart2.hashCode())
  }

  @Test
  fun `Given CartesianChart is copied without any changes, when it is compared to the original, then they are equal`() {
    val chart = getCartesianChart()

    val copiedChart = chart.copy()

    assertEquals(chart, copiedChart)
    assertEquals(chart.hashCode(), copiedChart.hashCode())
  }

  @Test
  fun `Given CartesianChart is copied with changes, when it is compared to the original, then they are NOT equal`() {
    val chart = getCartesianChart()

    val copiedChart =
      chart.copy(layerPadding = { CartesianLayerPadding(10.dp, 10.dp, 10.dp, 10.dp) })

    assertNotEquals(chart, copiedChart)
    assertNotEquals(chart.hashCode(), copiedChart.hashCode())
  }

  private companion object {
    fun getCartesianChart(): CartesianChart =
      CartesianChart(
        FakeLineCartesianLayer(),
        FakeColumnCartesianLayer(),
        markerVisibilityListener = object : CartesianMarkerVisibilityListener {},
      )
  }
}
