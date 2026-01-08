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

package com.patrykandpatrick.vico.views.cartesian

import android.graphics.RectF
import com.patrykandpatrick.vico.views.cartesian.layer.MutableCartesianLayerDimensions
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ZoomHandlerTest {
  @MockK private lateinit var context: CartesianMeasuringContext
  @MockK private lateinit var layerDimensions: MutableCartesianLayerDimensions
  @MockK private lateinit var bounds: RectF

  @BeforeEach
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
  }

  @Test
  fun `When maxZoom produces smaller factor than minZoom, then IllegalArgumentException is thrown`() {
    val minZoom = Zoom.fixed(2f)
    val maxZoom = Zoom.fixed(1f)
    val zoomHandler = ZoomHandler(minZoom = minZoom, maxZoom = maxZoom)

    val exception =
      Assertions.assertThrows(IllegalArgumentException::class.java) {
        zoomHandler.update(context, layerDimensions, bounds, 0f)
      }

    Assertions.assertTrue(
      exception.message!!.contains("maxZoom") && exception.message!!.contains("minZoom")
    )
  }

  @Test
  fun `When maxZoom produces equal factor as minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(1f)
    val zoomHandler = ZoomHandler(minZoom = minZoom, maxZoom = maxZoom)

    zoomHandler.update(context, layerDimensions, bounds, 0f)

    Assertions.assertEquals(1f..1f, zoomHandler.valueRange)
  }

  @Test
  fun `When maxZoom produces greater factor than minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(2f)
    val zoomHandler = ZoomHandler(minZoom = minZoom, maxZoom = maxZoom)

    zoomHandler.update(context, layerDimensions, bounds, 0f)

    Assertions.assertEquals(1f..2f, zoomHandler.valueRange)
  }
}
