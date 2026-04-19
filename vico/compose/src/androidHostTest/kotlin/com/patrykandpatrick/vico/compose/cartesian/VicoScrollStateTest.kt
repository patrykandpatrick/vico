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
import kotlin.test.assertFalse
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.jupiter.api.Timeout

@Timeout(1)
class VicoScrollStateTest {
  @MockK private lateinit var context: CartesianMeasuringContext
  @MockK private lateinit var ranges: CartesianChartRanges

  @BeforeTest
  fun setUp() {
    MockKAnnotations.init(this, relaxed = true)
    every { context.layoutDirection } returns LayoutDirection.Ltr
    every { context.ranges } returns ranges
    every { ranges.minX } returns 0.0
    every { ranges.maxX } returns 10.0
    every { ranges.xLength } returns 10.0
    every { ranges.xStep } returns 1.0
  }

  @Test
  fun `When animateScroll uses zero-duration animation, then it updates the value immediately`() =
    runBlocking(SuspendingFrameClock()) {
      val frameClock = coroutineContext[MonotonicFrameClock] as SuspendingFrameClock
      val sut = createScrollState()
      val targetValue = 30f

      sut.animateScroll(Scroll.Absolute.pixels(targetValue), tween(durationMillis = 0))

      assertEquals(targetValue, sut.value)
      assertEquals(0, frameClock.frameRequests)
    }

  @Test
  fun `When animateScroll uses non-zero-duration animation, then it does not update the value immediately`() =
    runBlocking(SuspendingFrameClock()) {
      val frameClock = coroutineContext[MonotonicFrameClock] as SuspendingFrameClock
      val sut = createScrollState()
      val targetValue = 30f

      val job =
        launch(start = CoroutineStart.UNDISPATCHED) {
          sut.animateScroll(Scroll.Absolute.pixels(targetValue), tween(durationMillis = 1))
        }

      assertEquals(0f, sut.value)
      assertFalse(job.isCompleted)
      assertEquals(1, frameClock.frameRequests)

      job.cancelAndJoin()
    }

  private fun createScrollState(): VicoScrollState =
    VicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.Start,
        autoScroll = Scroll.Absolute.Start,
        autoScrollCondition = AutoScrollCondition.Never,
        autoScrollAnimationSpec = tween(),
      )
      .also {
        it.update(
          context = context,
          bounds = Rect(0f, 0f, 100f, 100f),
          layerDimensions = MutableCartesianLayerDimensions(xSpacing = 20f),
        )
        it.maxValue = 100f
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
