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

import android.animation.ValueAnimator
import android.graphics.RectF
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.views.cartesian.layer.MutableCartesianLayerDimensions
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ZoomHandlerTest {
  @MockK private lateinit var context: CartesianMeasuringContext
  @MockK private lateinit var layerDimensions: MutableCartesianLayerDimensions
  @MockK private lateinit var bounds: RectF
  @MockK private lateinit var ranges: CartesianChartRanges

  @BeforeTest
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    every { context.isLtr } returns true
    every { context.ranges } returns ranges
    every { ranges.xLength } returns 10.0
    every { ranges.xStep } returns 1.0
    every { bounds.width() } returns 100f
  }

  @Test
  fun `When maxZoom produces smaller factor than minZoom, then IllegalArgumentException is thrown`() {
    val minZoom = Zoom.fixed(2f)
    val maxZoom = Zoom.fixed(1f)
    val zoomHandler = ZoomHandler(minZoom = minZoom, maxZoom = maxZoom)

    val exception =
      assertFailsWith<IllegalArgumentException> {
        zoomHandler.update(context, layerDimensions, bounds, 0f)
      }

    assertTrue(exception.message!!.contains("maxZoom") && exception.message!!.contains("minZoom"))
  }

  @Test
  fun `When maxZoom produces equal factor as minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(1f)
    val zoomHandler = ZoomHandler(minZoom = minZoom, maxZoom = maxZoom)

    zoomHandler.update(context, layerDimensions, bounds, 0f)

    assertEquals(1f..1f, zoomHandler.valueRange)
  }

  @Test
  fun `When maxZoom produces greater factor than minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(2f)
    val zoomHandler = ZoomHandler(minZoom = minZoom, maxZoom = maxZoom)

    zoomHandler.update(context, layerDimensions, bounds, 0f)

    assertEquals(1f..2f, zoomHandler.valueRange)
  }

  @Test
  fun `When animateZoom is called during an ongoing animated zoom, then the ongoing zoom is canceled`() {
    val animator = mockk<ValueAnimator>(relaxed = true)
    val updateListener = slot<ValueAnimator.AnimatorUpdateListener>()
    justRun { animator.addUpdateListener(capture(updateListener)) }
    every { animator.animatedFraction } returns 1f
    val zoomHandler =
      ZoomHandler(
        initialZoom = Zoom.fixed(1f),
        minZoom = Zoom.fixed(1f),
        maxZoom = Zoom.fixed(4f),
        animator = animator,
      )
    zoomHandler.update(context, MutableCartesianLayerDimensions(xSpacing = 10f), bounds, 0f)

    zoomHandler.animateZoom(Zoom.fixed(4f))
    zoomHandler.animateZoom(Zoom.fixed(2f))
    updateListener.captured.onAnimationUpdate(animator)

    verify(exactly = 2) { animator.cancel() }
    verify(exactly = 2) { animator.start() }
    assertEquals(2f, zoomHandler.value)
  }
}
