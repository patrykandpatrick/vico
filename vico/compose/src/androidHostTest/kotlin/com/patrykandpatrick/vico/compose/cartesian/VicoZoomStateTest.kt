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

import androidx.compose.animation.core.tween
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartRanges
import com.patrykandpatrick.vico.compose.cartesian.layer.MutableCartesianLayerDimensions
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlin.coroutines.CoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Timeout

@Timeout(1)
class VicoZoomStateTest {
  @MockK private lateinit var context: CartesianMeasuringContext
  @MockK private lateinit var ranges: CartesianChartRanges

  @BeforeTest
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    every { context.layoutDirection } returns LayoutDirection.Ltr
    every { context.ranges } returns ranges
    every { ranges.xLength } returns 10.0
    every { ranges.xStep } returns 1.0
  }

  @Test
  fun `When maxZoom produces smaller factor than minZoom, then IllegalArgumentException is thrown`() {
    val minZoom = Zoom.fixed(2f)
    val maxZoom = Zoom.fixed(1f)
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = minZoom,
        maxZoom = maxZoom,
      )

    val exception =
      assertFailsWith<IllegalArgumentException> {
        sut.update(context, MutableCartesianLayerDimensions(), Rect(0f, 0f, 10f, 10f), 0f)
      }

    assertTrue(exception.message!!.contains("maxZoom") && exception.message!!.contains("minZoom"))
  }

  @Test
  fun `When maxZoom produces equal factor as minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(1f)
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = minZoom,
        maxZoom = maxZoom,
      )

    sut.update(context, MutableCartesianLayerDimensions(), Rect(0f, 0f, 10f, 10f), 0f)

    assertEquals(1f..1f, sut.valueRange)
  }

  @Test
  fun `When maxZoom produces greater factor than minZoom, then no exception is thrown`() {
    val minZoom = Zoom.fixed(1f)
    val maxZoom = Zoom.fixed(2f)
    val sut =
      VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = minZoom,
        maxZoom = maxZoom,
      )

    sut.update(context, MutableCartesianLayerDimensions(), Rect(0f, 0f, 10f, 10f), 0f)

    assertEquals(1f..2f, sut.valueRange)
  }

  @Test
  fun `When animateZoom is called during an ongoing animated zoom, then the ongoing zoom is canceled`() =
    runBlocking(SuspendingFrameClock()) {
      val frameClock = coroutineContext[MonotonicFrameClock] as SuspendingFrameClock
      val sut = createZoomState()
      val scrollCollector =
        launch(start = CoroutineStart.UNDISPATCHED) { sut.pendingScroll.collect {} }

      val ongoingJob =
        launch(start = CoroutineStart.UNDISPATCHED) {
          sut.animateZoom(Zoom.fixed(4f), tween(durationMillis = 1))
        }

      assertEquals(1f, sut.value)
      assertFalse(ongoingJob.isCompleted)
      assertEquals(1, frameClock.frameRequests)

      sut.animateZoom(Zoom.fixed(2f), tween(durationMillis = 0))
      ongoingJob.join()

      assertTrue(ongoingJob.isCancelled)
      assertEquals(2f, sut.value)

      scrollCollector.cancelAndJoin()
    }

  private fun createZoomState(): VicoZoomState =
    VicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1f),
        minZoom = Zoom.fixed(1f),
        maxZoom = Zoom.fixed(4f),
      )
      .also {
        it.update(
          context = context,
          layerDimensions = MutableCartesianLayerDimensions(xSpacing = 10f),
          bounds = Rect(0f, 0f, 100f, 100f),
          scroll = 0f,
        )
      }

  private class SuspendingFrameClock : MonotonicFrameClock {
    var frameRequests: Int = 0

    override val key: CoroutineContext.Key<*>
      get() = MonotonicFrameClock.Key

    override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
      frameRequests++
      return suspendCancellableCoroutine {}
    }
  }
}
