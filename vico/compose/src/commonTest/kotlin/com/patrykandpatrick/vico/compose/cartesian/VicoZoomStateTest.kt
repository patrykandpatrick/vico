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

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VicoZoomStateTest {
  private val context = FakeCartesianMeasuringContext()
  private val layerDimensions = FakeMutableCartesianLayerDimensions()
  private val bounds = Rect(0f, 0f, 100f, 100f)

  @Test
  fun `When maxZoom produces smaller factor than minZoom, then IllegalArgumentException is thrown`() {
    val minZoom = Zoom.fixed(2f)
    val maxZoom = Zoom.fixed(1f)
    val zoomState =
      VicoZoomState(zoomEnabled = true, initialZoom = minZoom, minZoom = minZoom, maxZoom = maxZoom)

    val exception =
      assertFailsWith<IllegalArgumentException> {
        zoomState.update(context, layerDimensions, bounds, 0f)
      }

    assertTrue(exception.message!!.contains("maxZoom") && exception.message!!.contains("minZoom"))
  }

  @Test
  fun `When maxZoom produces equal factor as minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(1f)
    val zoomState =
      VicoZoomState(zoomEnabled = true, initialZoom = minZoom, minZoom = minZoom, maxZoom = maxZoom)

    zoomState.update(context, layerDimensions, bounds, 0f)

    assertEquals(1f..1f, zoomState.valueRange)
  }

  @Test
  fun `When maxZoom produces greater factor than minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(2f)
    val zoomState =
      VicoZoomState(zoomEnabled = true, initialZoom = minZoom, minZoom = minZoom, maxZoom = maxZoom)

    zoomState.update(context, layerDimensions, bounds, 0f)

    assertEquals(1f..2f, zoomState.valueRange)
  }
}
